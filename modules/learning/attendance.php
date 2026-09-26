<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';
require_staff(); require_csrf();
if ($_SERVER['REQUEST_METHOD'] !== 'POST') { http_response_code(405); exit; }
$user=current_user(); $sessionId=(int)($_POST['session_id']??0); $session=session_detail(db(),$sessionId,(int)$user['id']);
$isAjax=str_contains((string)($_SERVER['HTTP_ACCEPT']??''),'application/json') || strtolower((string)($_SERVER['HTTP_X_REQUESTED_WITH']??''))==='xmlhttprequest';
if (!$session) $result=['ok'=>false,'code'=>'SESSION_FORBIDDEN','message'=>'Sesi tidak ditemukan atau akses ditolak.'];
elseif (($_POST['action']??'scan')==='close') { db()->prepare('UPDATE attendance_sessions SET status="CLOSED", closed_at=NOW() WHERE id=?')->execute([$sessionId]); $result=['ok'=>true,'message'=>'Sesi ditutup.']; }
elseif (($_POST['action']??'scan')==='manual') {
    if ($session['status'] !== 'OPEN' || $session['date'] !== date('Y-m-d')) { $result=['ok'=>false,'message'=>'Sesi sudah ditutup atau tanggal sesi tidak sesuai.']; }
    else {
    $studentId=(int)($_POST['student_db_id']??0); $status=strtoupper((string)($_POST['status']??'ALPA')); $notes=trim((string)($_POST['notes']??''));
    $valid=['HADIR','IZIN','SAKIT','ALPA'];
    $q=db()->prepare('SELECT id FROM students WHERE id=? AND class_id=? AND is_active=1'); $q->execute([$studentId,(int)$session['class_id']]);
    if(!$q->fetchColumn() || !in_array($status,$valid,true)) $result=['ok'=>false,'message'=>'Siswa atau status tidak valid untuk sesi ini.'];
    else { $q=db()->prepare('INSERT INTO attendance_records (session_id,student_id,status,check_in_time,notes) VALUES (?,?,?,NOW(),?) ON DUPLICATE KEY UPDATE status=VALUES(status),notes=VALUES(notes),updated_at=CURRENT_TIMESTAMP'); $q->execute([$sessionId,$studentId,$status,$notes?:null]); $result=['ok'=>true,'message'=>'Status absensi manual disimpan.']; }
    }
}
else $result=record_session_attendance(db(),$sessionId,(string)($_POST['kode']??''),(string)($_POST['status']??'HADIR'),trim((string)($_POST['notes']??'')) ?: null);
if ($isAjax) { header('Content-Type: application/json; charset=utf-8'); echo json_encode($result,JSON_UNESCAPED_UNICODE); exit; }
set_flash_message(($result['ok']??false)?'success':'warning',$result['message']??'Permintaan selesai.'); redirect('modules/learning/session.php?id='.$sessionId);
