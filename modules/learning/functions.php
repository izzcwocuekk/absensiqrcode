<?php
declare(strict_types=1);

function learning_tables_ready(PDO $pdo): bool
{
    try { return (bool) $pdo->query("SHOW TABLES LIKE 'attendance_sessions'")->fetchColumn(); } catch (Throwable) { return false; }
}

function duty_roster_ready(PDO $pdo): bool
{
    try { return (bool) $pdo->query("SHOW TABLES LIKE 'teacher_duty_rosters'")->fetchColumn(); } catch (Throwable) { return false; }
}

function teacher_duty_rosters(PDO $pdo, int $teacherId = 0, ?string $date = null): array
{
    if (!duty_roster_ready($pdo)) return [];
    $where = ['d.status = "ACTIVE"']; $params = [];
    if ($teacherId > 0) { $where[] = 'd.teacher_id = :teacher_id'; $params[':teacher_id'] = $teacherId; }
    if ($date !== null && preg_match('/^\d{4}-\d{2}-\d{2}$/', $date)) {
        $day = learning_day_name(strtotime($date));
        $where[] = '(d.duty_date = :duty_date OR (d.duty_date IS NULL AND d.day = :duty_day))';
        $params[':duty_date'] = $date; $params[':duty_day'] = $day;
    }
    $q = $pdo->prepare('SELECT d.*, t.nama AS nama_guru, t.nip
        FROM teacher_duty_rosters d JOIN teachers t ON t.id = d.teacher_id
        WHERE ' . implode(' AND ', $where) . '
        ORDER BY COALESCE(d.duty_date, "9999-12-31"), d.start_time, t.nama');
    $q->execute($params);
    return $q->fetchAll();
}

function learning_day_name(?int $timestamp = null): string
{
    return ['Sunday'=>'Minggu','Monday'=>'Senin','Tuesday'=>'Selasa','Wednesday'=>'Rabu','Thursday'=>'Kamis','Friday'=>'Jumat','Saturday'=>'Sabtu'][date('l', $timestamp ?? time())] ?? 'Senin';
}

function current_teacher(PDO $pdo, array $user): ?array
{
    if (!learning_tables_ready($pdo)) return null;
    $q = $pdo->prepare('SELECT t.*, u.username FROM teachers t LEFT JOIN users u ON u.id=t.user_id WHERE t.user_id=:uid ORDER BY t.id LIMIT 1');
    $q->execute([':uid'=>(int)($user['id'] ?? 0)]);
    $teacher = $q->fetch();
    if (!$teacher && ($user['role'] ?? '') === 'guru' && ($user['username'] ?? '') === 'guru') {
        $q = $pdo->query('SELECT t.*, u.username FROM teachers t LEFT JOIN users u ON u.id=t.user_id ORDER BY t.id LIMIT 1');
        $teacher = $q->fetch() ?: null;
    }
    return $teacher ?: null;
}

function teacher_schedule(PDO $pdo, int $teacherId, string $day, string $date): array
{
    $q = $pdo->prepare('SELECT sc.*, c.nama_kelas, s.nama_mata_pelajaran, t.nama AS nama_guru,
        (SELECT id FROM attendance_sessions x WHERE x.schedule_id=sc.id AND x.date=:date1 LIMIT 1) AS session_id,
        (SELECT status FROM attendance_sessions x WHERE x.schedule_id=sc.id AND x.date=:date2 LIMIT 1) AS session_status
        FROM schedules sc JOIN classes c ON c.id=sc.class_id JOIN subjects s ON s.id=sc.subject_id JOIN teachers t ON t.id=sc.teacher_id
        WHERE sc.teacher_id=:tid AND sc.day=:day ORDER BY sc.lesson_number, sc.start_time');
    $q->execute([':tid'=>$teacherId,':day'=>$day,':date1'=>$date,':date2'=>$date]);
    return $q->fetchAll();
}

function resolve_student(PDO $pdo, string $code): ?array
{
    $q = $pdo->prepare('SELECT s.*, c.nama_kelas FROM students s LEFT JOIN classes c ON c.id=s.class_id
        LEFT JOIN qr_codes q ON q.student_id=s.student_id
        WHERE s.student_id=:a OR s.qr_code=:b OR s.qr_token=:c OR s.nis=:d OR q.qr_code=:e LIMIT 1');
    $q->execute([':a'=>$code,':b'=>$code,':c'=>$code,':d'=>$code,':e'=>$code]);
    return $q->fetch() ?: null;
}

function geo_distance_m(float $lat1, float $lon1, float $lat2, float $lon2): float
{
    $r=6371000.0; $p=pi()/180; $a=0.5-cos(($lat2-$lat1)*$p)/2+cos($lat1*$p)*cos($lat2*$p)*(1-cos(($lon2-$lon1)*$p))/2; return 2*$r*asin(min(1,sqrt($a)));
}

function open_learning_session(PDO $pdo, int $scheduleId, int $userId, string $date, ?float $lat, ?float $lng): array
{
    $q=$pdo->prepare('SELECT sc.*, t.id teacher_id, c.nama_kelas, s.nama_mata_pelajaran FROM schedules sc JOIN teachers t ON t.id=sc.teacher_id JOIN subjects s ON s.id=sc.subject_id JOIN classes c ON c.id=sc.class_id WHERE sc.id=:id AND (t.user_id=:uid1 OR :uid2 IN (SELECT id FROM users WHERE role="admin")) LIMIT 1');
    $q->execute([':id'=>$scheduleId,':uid1'=>$userId,':uid2'=>$userId]); $schedule=$q->fetch();
    if (!$schedule) return ['ok'=>false,'message'=>'Jadwal tidak ditemukan atau bukan tanggung jawab Anda.'];
    if ($date !== date('Y-m-d')) return ['ok'=>false,'message'=>'Sesi hanya dapat dibuka untuk tanggal hari ini.'];
    if ($schedule['day'] !== learning_day_name()) return ['ok'=>false,'message'=>'Jadwal ini tidak berlangsung pada hari ini.'];
    $settings=get_attendance_settings(); $verified=0;
    $schoolLat=$settings['school_latitude'] ?? null; $schoolLng=$settings['school_longitude'] ?? null; $radius=(int)($settings['location_radius_m'] ?? 150);
    if ($schoolLat !== null && $schoolLng !== null) {
        if ($lat === null || $lng === null) return ['ok'=>false,'message'=>'Lokasi perangkat diperlukan untuk membuka sesi.'];
        $distance=geo_distance_m((float)$schoolLat,(float)$schoolLng,$lat,$lng);
        if ($distance > $radius && (int)($settings['require_school_location'] ?? 0)===1) return ['ok'=>false,'message'=>'Anda berada di luar radius sekolah (' . round($distance) . ' m).'];
        $verified=$distance <= $radius ? 1 : 0;
    }
    try {
        $ins=$pdo->prepare('INSERT INTO attendance_sessions (schedule_id,teacher_id,class_id,subject_id,date,lesson_number,opened_at,status,latitude,longitude,location_verified) VALUES (?,?,?,?,?,?,NOW(),"OPEN",?,?,?)');
        $ins->execute([$scheduleId,$schedule['teacher_id'],$schedule['class_id'],$schedule['subject_id'],$date,$schedule['lesson_number'],$lat,$lng,$verified]);
        $id=(int)$pdo->lastInsertId();
    } catch (PDOException $e) {
        if ((int)$e->errorInfo[1]===1062) { $x=$pdo->prepare('SELECT id FROM attendance_sessions WHERE schedule_id=? AND date=?'); $x->execute([$scheduleId,$date]); $id=(int)$x->fetchColumn(); }
        else throw $e;
    }
    return ['ok'=>true,'id'=>$id,'schedule'=>$schedule,'location_verified'=>$verified];
}

function session_detail(PDO $pdo, int $sessionId, int $userId): ?array
{
    $q=$pdo->prepare('SELECT x.*, c.nama_kelas, s.nama_mata_pelajaran, t.nama nama_guru FROM attendance_sessions x JOIN classes c ON c.id=x.class_id JOIN subjects s ON s.id=x.subject_id JOIN teachers t ON t.id=x.teacher_id WHERE x.id=:id AND (t.user_id=:uid1 OR :uid2 IN (SELECT id FROM users WHERE role="admin")) LIMIT 1');
    $q->execute([':id'=>$sessionId,':uid1'=>$userId,':uid2'=>$userId]); return $q->fetch() ?: null;
}

function record_session_attendance(PDO $pdo, int $sessionId, string $code, string $status='HADIR', ?string $notes=null): array
{
    $session=session_detail($pdo,$sessionId,(int)($_SESSION['user']['id'] ?? 0));
    if (!$session) return ['ok'=>false,'code'=>'SESSION_FORBIDDEN','message'=>'Sesi tidak ditemukan atau akses ditolak.'];
    if ($session['status'] !== 'OPEN') return ['ok'=>false,'code'=>'SESSION_CLOSED','message'=>'Sesi sudah ditutup.'];
    if ($session['date'] !== date('Y-m-d')) return ['ok'=>false,'code'=>'DATE_MISMATCH','message'=>'Tanggal sesi tidak sesuai hari ini.'];
    $student=resolve_student($pdo,trim($code));
    if (!$student || (int)$student['is_active']===0) return ['ok'=>false,'code'=>'INVALID_STUDENT','message'=>'Siswa tidak terdaftar atau tidak aktif.'];
    if ((int)$student['class_id'] !== (int)$session['class_id']) return ['ok'=>false,'code'=>'CLASS_MISMATCH','message'=>'Siswa bukan anggota kelas sesi ini.','student'=>$student];
    $status=strtoupper($status); if (!in_array($status,['HADIR','IZIN','SAKIT','ALPA'],true)) $status='HADIR';
    $check=$pdo->prepare('SELECT ar.*, s.nama, s.student_id, s.nis FROM attendance_records ar JOIN students s ON s.id=ar.student_id WHERE ar.session_id=? AND ar.student_id=?');
    $check->execute([$sessionId,(int)$student['id']]); if ($old=$check->fetch()) return ['ok'=>false,'code'=>'DUPLICATE','message'=>'Tidak dapat melakukan absensi ulang. Siswa sudah tercatat.','student'=>$student,'record'=>$old];
    try { $ins=$pdo->prepare('INSERT INTO attendance_records (session_id,student_id,status,check_in_time,notes) VALUES (?,?,?,NOW(),?)'); $ins->execute([$sessionId,(int)$student['id'],$status,$notes]); }
    catch(PDOException $e) { if((int)$e->errorInfo[1]===1062) return ['ok'=>false,'code'=>'DUPLICATE','message'=>'Tidak dapat melakukan absensi ulang. Siswa sudah tercatat.','student'=>$student]; throw $e; }
    return ['ok'=>true,'code'=>'SUCCESS','message'=>'Absensi berhasil dicatat.','student'=>$student,'status'=>$status,'time'=>date('H:i')];
}
