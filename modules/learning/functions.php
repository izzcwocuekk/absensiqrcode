<?php
declare(strict_types=1);

/** Shared logic for schedule-based attendance. */

function learning_tables_ready(PDO $pdo): bool
{
    try { return (bool) $pdo->query("SHOW TABLES LIKE 'attendance_sessions'")->fetchColumn(); } catch (Throwable) { return false; }
}

function duty_roster_ready(PDO $pdo): bool
{
    try { return (bool) $pdo->query("SHOW TABLES LIKE 'teacher_duty_rosters'")->fetchColumn(); } catch (Throwable) { return false; }
}

function learning_column_exists(PDO $pdo, string $table, string $column): bool
{
    static $cache = [];
    $key = $table . '.' . $column;
    if (array_key_exists($key, $cache)) return $cache[$key];
    $q = $pdo->prepare('SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?');
    $q->execute([$table, $column]);
    return $cache[$key] = (bool) $q->fetchColumn();
}

function learning_table_exists(PDO $pdo, string $table): bool
{
    static $cache = [];
    if (array_key_exists($table, $cache)) return $cache[$table];
    $q = $pdo->prepare('SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?');
    $q->execute([$table]);
    return $cache[$table] = (bool) $q->fetchColumn();
}

function learning_student_columns(PDO $pdo): array
{
    return ['student_id' => learning_column_exists($pdo, 'students', 'student_id'), 'qr_code' => learning_column_exists($pdo, 'students', 'qr_code')];
}

function learning_normalize_student(PDO $pdo, array $student): array
{
    if (empty($student['student_id'])) $student['student_id'] = 'STU' . str_pad((string) ($student['id'] ?? ''), 3, '0', STR_PAD_LEFT);
    if (empty($student['qr_code'])) $student['qr_code'] = (string) ($student['qr_token'] ?? $student['student_id']);
    return $student;
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
    $q = $pdo->prepare('SELECT d.*, t.nama AS nama_guru, t.nip FROM teacher_duty_rosters d JOIN teachers t ON t.id=d.teacher_id WHERE ' . implode(' AND ', $where) . ' ORDER BY COALESCE(d.duty_date, "9999-12-31"), d.start_time, t.nama');
    $q->execute($params); return $q->fetchAll();
}

function learning_day_name(?int $timestamp = null): string
{
    return ['Sunday'=>'Minggu','Monday'=>'Senin','Tuesday'=>'Selasa','Wednesday'=>'Rabu','Thursday'=>'Kamis','Friday'=>'Jumat','Saturday'=>'Sabtu'][date('l', $timestamp ?? time())] ?? 'Senin';
}

function current_teacher(PDO $pdo, array $user): ?array
{
    if (!learning_tables_ready($pdo)) return null;
    $q = $pdo->prepare('SELECT t.*, u.username FROM teachers t LEFT JOIN users u ON u.id=t.user_id WHERE t.user_id=:uid ORDER BY t.id LIMIT 1');
    $q->execute([':uid' => (int) ($user['id'] ?? 0)]); $teacher = $q->fetch();
    // Akun demo lama tetap bisa dipakai pada instalasi cepat yang belum
    // menghubungkan user guru ke baris teacher tertentu.
    if (!$teacher && ($user['role'] ?? '') === 'guru' && ($user['username'] ?? '') === 'guru') {
        $teacher = $pdo->query('SELECT t.*, u.username FROM teachers t LEFT JOIN users u ON u.id=t.user_id ORDER BY t.id LIMIT 1')->fetch() ?: null;
    }
    return $teacher ?: null;
}

function current_student(PDO $pdo, array $user): ?array
{
    if (!learning_table_exists($pdo, 'students')) return null;
    $cols = learning_student_columns($pdo); $where = []; $params = [];
    if ($cols['student_id'] && !empty($user['student_id'])) { $where[] = 's.student_id=:student_id'; $params[':student_id'] = (string) $user['student_id']; }
    if (!empty($user['username'])) { $where[] = 's.nis=:username'; $params[':username'] = (string) $user['username']; }
    if ($where === []) return null;
    $q = $pdo->prepare('SELECT s.*, c.nama_kelas, c.jurusan FROM students s LEFT JOIN classes c ON c.id=s.class_id WHERE (' . implode(' OR ', $where) . ') AND s.is_active=1 LIMIT 1');
    $q->execute($params); $student = $q->fetch();
    if (!$student && ($user['username'] ?? '') === 'siswa') {
        // Kompatibilitas akun demo schema dasar (username siswa belum
        // memiliki kolom student_id atau username berupa NIS).
        $student = $pdo->query('SELECT s.*, c.nama_kelas, c.jurusan FROM students s LEFT JOIN classes c ON c.id=s.class_id WHERE s.is_active=1 ORDER BY s.id LIMIT 1')->fetch() ?: null;
    }
    return $student ? learning_normalize_student($pdo, $student) : null;
}

function teacher_schedule(PDO $pdo, int $teacherId, string $day, string $date): array
{
    $q = $pdo->prepare('SELECT sc.*, c.nama_kelas, s.nama_mata_pelajaran, t.nama AS nama_guru,
        (SELECT id FROM attendance_sessions x WHERE x.schedule_id=sc.id AND x.date=:date1 LIMIT 1) AS session_id,
        (SELECT status FROM attendance_sessions x WHERE x.schedule_id=sc.id AND x.date=:date2 LIMIT 1) AS session_status,
        (SELECT opened_at FROM attendance_sessions x WHERE x.schedule_id=sc.id AND x.date=:date4 LIMIT 1) AS teacher_check_in_at,
        (SELECT COUNT(*) FROM students st WHERE st.class_id=sc.class_id AND st.is_active=1) AS total_students,
        (SELECT COUNT(*) FROM attendance_records ar JOIN attendance_sessions ax ON ax.id=ar.session_id WHERE ax.schedule_id=sc.id AND ax.date=:date3) AS present_students
        FROM schedules sc JOIN classes c ON c.id=sc.class_id JOIN subjects s ON s.id=sc.subject_id JOIN teachers t ON t.id=sc.teacher_id
        WHERE sc.teacher_id=:tid AND sc.day=:day ORDER BY sc.lesson_number, sc.start_time');
    $q->execute([':tid'=>$teacherId, ':day'=>$day, ':date1'=>$date, ':date2'=>$date, ':date3'=>$date, ':date4'=>$date]); return $q->fetchAll();
}

function resolve_student(PDO $pdo, string $code): ?array
{
    $code = trim($code); if ($code === '') return null;
    $cols = learning_student_columns($pdo); $conditions = ['s.id=:id', 's.qr_token=:token', 's.nis=:nis'];
    $params = [':id'=>ctype_digit($code) ? (int)$code : 0, ':token'=>$code, ':nis'=>$code];
    if ($cols['student_id']) { $conditions[]='s.student_id=:student_id'; $params[':student_id']=$code; }
    if ($cols['qr_code']) { $conditions[]='s.qr_code=:qr_code'; $params[':qr_code']=$code; }
    if (learning_table_exists($pdo, 'qr_codes')) {
        $identity = $cols['student_id'] ? 's.student_id' : 'CAST(s.id AS CHAR)';
        $conditions[] = 'EXISTS (SELECT 1 FROM qr_codes q WHERE q.student_id=' . $identity . ' AND q.qr_code=:legacy_qr)';
        $params[':legacy_qr'] = $code;
    }
    $q = $pdo->prepare('SELECT s.*, c.nama_kelas, c.jurusan FROM students s LEFT JOIN classes c ON c.id=s.class_id WHERE ' . implode(' OR ', $conditions) . ' LIMIT 1');
    $q->execute($params); $student = $q->fetch(); return $student ? learning_normalize_student($pdo, $student) : null;
}

function geo_distance_m(float $lat1, float $lon1, float $lat2, float $lon2): float
{
    $r=6371000.0; $p=pi()/180; $a=0.5-cos(($lat2-$lat1)*$p)/2+cos($lat1*$p)*cos($lat2*$p)*(1-cos(($lon2-$lon1)*$p))/2; return 2*$r*asin(min(1,sqrt($a)));
}

function new_session_token(): string { return bin2hex(random_bytes(32)); }

function ensure_session_token(PDO $pdo, int $sessionId): ?string
{
    if (!learning_column_exists($pdo, 'attendance_sessions', 'session_token')) return null;
    $q=$pdo->prepare('SELECT session_token FROM attendance_sessions WHERE id=? LIMIT 1'); $q->execute([$sessionId]);
    $token=(string)($q->fetchColumn() ?: ''); if ($token!=='') return $token;
    $token=new_session_token(); $pdo->prepare('UPDATE attendance_sessions SET session_token=? WHERE id=? AND (session_token IS NULL OR session_token="")')->execute([$token,$sessionId]);
    $q->execute([$sessionId]); return (string)($q->fetchColumn() ?: $token);
}

function open_learning_session(PDO $pdo, int $scheduleId, int $userId, string $date, ?float $lat, ?float $lng): array
{
    $q=$pdo->prepare('SELECT sc.*, t.id teacher_id, c.nama_kelas, s.nama_mata_pelajaran FROM schedules sc JOIN teachers t ON t.id=sc.teacher_id JOIN subjects s ON s.id=sc.subject_id JOIN classes c ON c.id=sc.class_id WHERE sc.id=:id AND (t.user_id=:uid1 OR EXISTS (SELECT 1 FROM users u WHERE u.id=:uid2 AND u.role="admin")) LIMIT 1');
    $q->execute([':id'=>$scheduleId,':uid1'=>$userId,':uid2'=>$userId]); $schedule=$q->fetch();
    if (!$schedule) return ['ok'=>false,'message'=>'Jadwal tidak ditemukan atau bukan tanggung jawab Anda.'];
    if ($date!==date('Y-m-d')) return ['ok'=>false,'message'=>'Sesi hanya dapat dibuka untuk tanggal hari ini.'];
    if ($schedule['day']!==learning_day_name()) return ['ok'=>false,'message'=>'Jadwal ini tidak berlangsung pada hari ini.'];
    $settings=get_attendance_settings(); $verified=0; $schoolLat=$settings['school_latitude']??null; $schoolLng=$settings['school_longitude']??null; $radius=(int)($settings['location_radius_m']??150);
    if ($schoolLat!==null&&$schoolLng!==null) { if($lat===null||$lng===null)return ['ok'=>false,'message'=>'Lokasi perangkat diperlukan untuk membuka sesi.']; $distance=geo_distance_m((float)$schoolLat,(float)$schoolLng,$lat,$lng); if($distance>$radius&&(int)($settings['require_school_location']??0)===1)return ['ok'=>false,'message'=>'Anda berada di luar radius sekolah ('.round($distance).' m).']; $verified=$distance<=$radius?1:0; }
    $token=new_session_token();
    try { $ins=$pdo->prepare('INSERT INTO attendance_sessions (session_token,schedule_id,teacher_id,class_id,subject_id,date,lesson_number,opened_at,status,latitude,longitude,location_verified) VALUES (?,?,?,?,?,?,?,NOW(),"OPEN",?,?,?)'); $ins->execute([$token,$scheduleId,$schedule['teacher_id'],$schedule['class_id'],$schedule['subject_id'],$date,$schedule['lesson_number'],$lat,$lng,$verified]); $id=(int)$pdo->lastInsertId(); }
    catch(PDOException $e){ if((int)($e->errorInfo[1]??0)===1062){$x=$pdo->prepare('SELECT id FROM attendance_sessions WHERE schedule_id=? AND date=?');$x->execute([$scheduleId,$date]);$id=(int)$x->fetchColumn();}else throw $e; }
    $token=ensure_session_token($pdo,$id)?:$token; return ['ok'=>true,'id'=>$id,'session_token'=>$token,'schedule'=>$schedule,'location_verified'=>$verified];
}

function session_detail(PDO $pdo, int $sessionId, int $userId): ?array
{
    $q=$pdo->prepare('SELECT x.*, sc.start_time, sc.end_time, c.nama_kelas, s.nama_mata_pelajaran, t.nama nama_guru FROM attendance_sessions x JOIN schedules sc ON sc.id=x.schedule_id JOIN classes c ON c.id=x.class_id JOIN subjects s ON s.id=x.subject_id JOIN teachers t ON t.id=x.teacher_id WHERE x.id=:id AND (t.user_id=:uid1 OR EXISTS (SELECT 1 FROM users u WHERE u.id=:uid2 AND u.role="admin")) LIMIT 1');
    $q->execute([':id'=>$sessionId,':uid1'=>$userId,':uid2'=>$userId]); $session=$q->fetch(); if(!$session)return null;
    if(empty($session['session_token']))$session['session_token']=ensure_session_token($pdo,$sessionId); return $session;
}

function record_session_attendance(PDO $pdo, int $sessionId, string $code, string $status='HADIR', ?string $notes=null): array
{
    $session=session_detail($pdo,$sessionId,(int)($_SESSION['user']['id']??0)); if(!$session)return ['ok'=>false,'code'=>'SESSION_FORBIDDEN','message'=>'Sesi tidak ditemukan atau akses ditolak.'];
    if($session['status']!=='OPEN')return ['ok'=>false,'code'=>'SESSION_CLOSED','message'=>'Sesi sudah ditutup.']; if($session['date']!==date('Y-m-d'))return ['ok'=>false,'code'=>'DATE_MISMATCH','message'=>'Tanggal sesi tidak sesuai hari ini.'];
    $student=resolve_student($pdo,trim($code)); if(!$student||(int)$student['is_active']===0)return ['ok'=>false,'code'=>'INVALID_STUDENT','message'=>'Siswa tidak terdaftar atau tidak aktif.'];
    if((int)$student['class_id']!==(int)$session['class_id'])return ['ok'=>false,'code'=>'CLASS_MISMATCH','message'=>'Siswa bukan anggota kelas sesi ini.','student'=>$student];
    $status=strtoupper($status);if(!in_array($status,['HADIR','IZIN','SAKIT','ALPA'],true))$status='HADIR';
    $check=$pdo->prepare('SELECT ar.*,s.nama,s.nis FROM attendance_records ar JOIN students s ON s.id=ar.student_id WHERE ar.session_id=? AND ar.student_id=?');$check->execute([$sessionId,(int)$student['id']]);if($old=$check->fetch())return ['ok'=>false,'code'=>'DUPLICATE','message'=>'Tidak dapat melakukan absensi ulang. Siswa sudah tercatat.','student'=>$student,'record'=>$old];
    try{$ins=$pdo->prepare('INSERT INTO attendance_records (session_id,student_id,status,check_in_time,notes) VALUES (?,?,?,NOW(),?)');$ins->execute([$sessionId,(int)$student['id'],$status,$notes]);}catch(PDOException $e){if((int)($e->errorInfo[1]??0)===1062)return ['ok'=>false,'code'=>'DUPLICATE','message'=>'Tidak dapat melakukan absensi ulang. Siswa sudah tercatat.','student'=>$student];throw $e;}
    return ['ok'=>true,'code'=>'SUCCESS','message'=>'Absensi berhasil dicatat.','student'=>$student,'status'=>$status,'time'=>date('H:i')];
}

function record_student_session_attendance(PDO $pdo, string $sessionToken, array $student): array
{
    $sessionToken=trim($sessionToken);if($sessionToken==='')return ['ok'=>false,'code'=>'EMPTY_SESSION','message'=>'QR sesi kosong.'];
    $q=$pdo->prepare('SELECT x.*,c.nama_kelas,s.nama_mata_pelajaran,t.nama nama_guru FROM attendance_sessions x JOIN classes c ON c.id=x.class_id JOIN subjects s ON s.id=x.subject_id JOIN teachers t ON t.id=x.teacher_id WHERE x.session_token=? LIMIT 1');$q->execute([$sessionToken]);$session=$q->fetch();
    if(!$session)return ['ok'=>false,'code'=>'INVALID_SESSION','message'=>'QR sesi tidak valid.'];if($session['status']!=='OPEN')return ['ok'=>false,'code'=>'SESSION_CLOSED','message'=>'Sesi absensi sudah ditutup.'];if($session['date']!==date('Y-m-d'))return ['ok'=>false,'code'=>'DATE_MISMATCH','message'=>'QR sesi bukan untuk hari ini.'];
    if((int)$student['class_id']!==(int)$session['class_id'])return ['ok'=>false,'code'=>'CLASS_MISMATCH','message'=>'Kelas siswa tidak sesuai dengan sesi ini.','session'=>$session];
    $check=$pdo->prepare('SELECT id,status,check_in_time FROM attendance_records WHERE session_id=? AND student_id=? LIMIT 1');$check->execute([(int)$session['id'],(int)$student['id']]);if($old=$check->fetch())return ['ok'=>false,'code'=>'DUPLICATE','message'=>'Absensi untuk sesi ini sudah tercatat.','session'=>$session,'record'=>$old];
    try{$pdo->prepare("INSERT INTO attendance_records (session_id,student_id,status,check_in_time) VALUES (?, ?, 'HADIR', NOW())")->execute([(int)$session['id'],(int)$student['id']]);}catch(PDOException $e){if((int)($e->errorInfo[1]??0)===1062)return ['ok'=>false,'code'=>'DUPLICATE','message'=>'Absensi untuk sesi ini sudah tercatat.','session'=>$session];throw $e;}
    return ['ok'=>true,'code'=>'SUCCESS','message'=>'Kehadiran berhasil dicatat.','session'=>$session,'student'=>$student,'time'=>date('H:i')];
}
