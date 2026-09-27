<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';

require_role('siswa');
header('Content-Type: application/json; charset=utf-8');
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['ok' => false, 'code' => 'METHOD_NOT_ALLOWED', 'message' => 'Metode request tidak diizinkan.'], JSON_UNESCAPED_UNICODE);
    exit;
}
require_csrf();

$student = current_student(db(), current_user() ?? []);
if (!$student) {
    echo json_encode(['ok' => false, 'code' => 'STUDENT_NOT_LINKED', 'message' => 'Akun siswa belum terhubung ke data siswa.'], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $result = record_student_session_attendance(db(), (string) ($_POST['session_token'] ?? ''), $student);
    echo json_encode($result, JSON_UNESCAPED_UNICODE);
} catch (Throwable) {
    http_response_code(500);
    echo json_encode(['ok' => false, 'code' => 'SERVER_ERROR', 'message' => 'Absensi tidak dapat disimpan.'], JSON_UNESCAPED_UNICODE);
}
