<?php
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

$_GET['code'] = 'QR-STU001';
// Simulate running qr.php logic
$code = preg_replace('/[^A-Za-z0-9_-]/', '', (string) ($_GET['code'] ?? ''));

$stmt = db()->prepare('
    SELECT s.id, s.student_id, s.nis, s.nama, s.qr_code, s.qr_token 
    FROM students s 
    LEFT JOIN qr_codes q ON q.student_id = s.student_id
    WHERE s.qr_code = :c1 OR s.qr_token = :c2 OR s.student_id = :c3 OR s.nis = :c4 OR q.qr_code = :c5
    LIMIT 1
');
$stmt->execute([
    ':c1' => $code,
    ':c2' => $code,
    ':c3' => $code,
    ':c4' => $code,
    ':c5' => $code,
]);
$siswa = $stmt->fetch();
echo "Found student: " . ($siswa['nama'] ?? 'NONE') . "\n";

$targetCode = !empty($siswa['qr_code']) ? (string) $siswa['qr_code'] : $code;
$path = qrcode_ensure_file($targetCode);
echo "Path: " . $path . "\n";
echo "File exists: " . (is_file($path) ? 'YES' : 'NO') . " (" . filesize($path) . " bytes)\n";
