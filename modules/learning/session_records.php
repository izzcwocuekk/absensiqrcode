<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config/config.php'; require_once ROOT_PATH . '/config/database.php'; require_once ROOT_PATH . '/modules/auth/guard.php'; require_once ROOT_PATH . '/modules/learning/functions.php'; require_staff();
$s=session_detail(db(),(int)($_GET['id']??0),(int)(current_user()['id']??0)); header('Content-Type: application/json; charset=utf-8');
if(!$s){http_response_code(403);echo '[]';exit;}$q=db()->prepare('SELECT ar.status,ar.check_in_time,st.nama,st.student_id,st.nis FROM attendance_records ar JOIN students st ON st.id=ar.student_id WHERE ar.session_id=? ORDER BY st.nama');$q->execute([(int)$s['id']]);echo json_encode($q->fetchAll(),JSON_UNESCAPED_UNICODE);
