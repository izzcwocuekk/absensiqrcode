<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';

require_staff();
$pdo = db();
$session = session_detail($pdo, (int) ($_GET['id'] ?? 0), (int) (current_user()['id'] ?? 0));
header('Content-Type: application/json; charset=utf-8');
if (!$session) { http_response_code(403); echo '[]'; exit; }
$q = $pdo->prepare('SELECT ar.status, ar.check_in_time, st.id, st.nis, st.nama FROM attendance_records ar JOIN students st ON st.id=ar.student_id WHERE ar.session_id=? ORDER BY st.nama');
$q->execute([(int) $session['id']]);
$rows = $q->fetchAll();
foreach ($rows as &$row) $row = learning_normalize_student($pdo, $row);
unset($row);
echo json_encode($rows, JSON_UNESCAPED_UNICODE);
