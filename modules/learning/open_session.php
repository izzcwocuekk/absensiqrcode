<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';
require_staff(); require_csrf();
if ($_SERVER['REQUEST_METHOD'] !== 'POST') { http_response_code(405); exit; }
$result=open_learning_session(db(),(int)($_POST['schedule_id']??0),(int)(current_user()['id']??0),date('Y-m-d'),isset($_POST['latitude'])&&$_POST['latitude']!==''?(float)$_POST['latitude']:null,isset($_POST['longitude'])&&$_POST['longitude']!==''?(float)$_POST['longitude']:null);
header('Content-Type: application/json; charset=utf-8'); echo json_encode($result,JSON_UNESCAPED_UNICODE);
