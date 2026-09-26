<?php
declare(strict_types=1);

function laporan_filter(): array
{
    $awal=date('Y-m-01'); $akhir=date('Y-m-t');
    $dari=(string)($_GET['dari']??$awal); $sampai=(string)($_GET['sampai']??$akhir);
    if(!preg_match('/^\d{4}-\d{2}-\d{2}$/',$dari))$dari=$awal;
    if(!preg_match('/^\d{4}-\d{2}-\d{2}$/',$sampai))$sampai=$akhir;
    if($dari>$sampai)[$dari,$sampai]=[$sampai,$dari];
    return ['dari'=>$dari,'sampai'=>$sampai,'kelas_id'=>(int)($_GET['kelas_id']??0),'teacher_id'=>(int)($_GET['teacher_id']??0),'subject_id'=>(int)($_GET['subject_id']??0),'lesson_number'=>(int)($_GET['lesson_number']??0),'status'=>strtoupper(trim((string)($_GET['status']??'')))];
}

function laporan_data(PDO $pdo,string $dari,string $sampai,int $kelasId,int $teacherId=0,int $subjectId=0,int $lessonNumber=0,string $status=''):array
{
    $ringkas=['Hadir'=>0,'Terlambat'=>0,'Izin'=>0,'Sakit'=>0,'Alfa'=>0];
    $rows=[]; $has=(bool)$pdo->query("SHOW TABLES LIKE 'attendance_records'")->fetchColumn();
    if($has){
        $where=['x.date BETWEEN :dari AND :sampai']; $p=[':dari'=>$dari,':sampai'=>$sampai];
        if($kelasId>0){$where[]='x.class_id=:kelas';$p[':kelas']=$kelasId;}
        if($teacherId>0){$where[]='x.teacher_id=:guru';$p[':guru']=$teacherId;}
        if($subjectId>0){$where[]='x.subject_id=:mapel';$p[':mapel']=$subjectId;}
        if($lessonNumber>0){$where[]='x.lesson_number=:les';$p[':les']=$lessonNumber;}
        if($status!==''){$where[]='ar.status=:status';$p[':status']=$status;}
        $q=$pdo->prepare('SELECT x.date tanggal,ar.check_in_time jam_masuk,ar.status,ar.notes keterangan,st.student_id,st.nis,st.nama,c.nama_kelas,t.nama nama_guru,sub.nama_mata_pelajaran,x.lesson_number FROM attendance_records ar JOIN attendance_sessions x ON x.id=ar.session_id JOIN students st ON st.id=ar.student_id JOIN classes c ON c.id=x.class_id JOIN teachers t ON t.id=x.teacher_id JOIN subjects sub ON sub.id=x.subject_id WHERE '.implode(' AND ',$where).' ORDER BY x.date DESC,st.nama LIMIT 5000');
        $q->execute($p); $rows=$q->fetchAll();
        if($rows===[]){
            $legacyWhere='a.tanggal BETWEEN :dari AND :sampai'; $legacyParams=[':dari'=>$dari,':sampai'=>$sampai];
            if($kelasId>0){$legacyWhere.=' AND s.class_id=:kelas';$legacyParams[':kelas']=$kelasId;}
            $legacy=$pdo->prepare("SELECT a.tanggal,a.jam_masuk,a.status,a.keterangan,s.student_id,s.nis,s.nama,c.nama_kelas FROM attendances a JOIN students s ON (s.student_id=a.student_id OR s.id=a.student_id) LEFT JOIN classes c ON c.id=s.class_id WHERE $legacyWhere ORDER BY a.tanggal DESC,s.nama LIMIT 5000"); $legacy->execute($legacyParams); $rows=$legacy->fetchAll();
        }
    } else {
        $where='a.tanggal BETWEEN :dari AND :sampai'; $p=[':dari'=>$dari,':sampai'=>$sampai];
        if($kelasId>0){$where.=' AND s.class_id=:kelas';$p[':kelas']=$kelasId;}
        if($status!==''){$where.=' AND a.status=:status';$p[':status']=ucfirst(strtolower($status));}
        $q=$pdo->prepare("SELECT a.tanggal,a.jam_masuk,a.status,a.keterangan,s.student_id,s.nis,s.nama,c.nama_kelas FROM attendances a JOIN students s ON (s.student_id=a.student_id OR s.id=a.student_id) LEFT JOIN classes c ON c.id=s.class_id WHERE $where ORDER BY a.tanggal DESC,s.nama LIMIT 5000"); $q->execute($p); $rows=$q->fetchAll();
    }
    foreach($rows as $r){$st=strtoupper((string)$r['status']);$st=$st==='ALPA'?'Alfa':ucfirst(strtolower($st));if(isset($ringkas[$st]))$ringkas[$st]++;}
    return [$rows,$ringkas];
}
