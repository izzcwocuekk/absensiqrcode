<?php
declare(strict_types=1);

/**
 * Endpoint gambar PNG QR per siswa.
 * URL: modules/siswa/qr.php?code={qr_token}&download=1
 */

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

$rawCode = (string) ($_GET['code'] ?? $_GET['id'] ?? $_GET['student_id'] ?? '');
$code = preg_replace('/[^A-Za-z0-9_.-]/', '', trim($rawCode));

// Jika dipanggil tanpa parameter, fallback ke student_id siswa yang sedang login
if ($code === '' && !empty($_SESSION['user'])) {
    $u = $_SESSION['user'];
    $code = !empty($u['student_id']) ? (string) $u['student_id'] : (string) ($u['username'] ?? '');
}

if ($code === '') {
    $code = 'QR-STU009';
}

$stmt = db()->prepare('
    SELECT s.id, s.student_id, s.nis, s.nama, s.qr_code, s.qr_token 
    FROM students s 
    LEFT JOIN qr_codes q ON q.student_id = s.student_id
    WHERE s.qr_code = :c1 
       OR s.qr_token = :c2 
       OR s.student_id = :c3 
       OR s.nis = :c4 
       OR q.qr_code = :c5
       OR s.student_id = (SELECT student_id FROM users WHERE username = :c6 LIMIT 1)
    LIMIT 1
');
$stmt->execute([
    ':c1' => $code,
    ':c2' => $code,
    ':c3' => $code,
    ':c4' => $code,
    ':c5' => $code,
    ':c6' => $code,
]);
$siswa = $stmt->fetch();

$targetCode = $code;
if ($siswa) {
    $targetCode = !empty($siswa['qr_code']) ? (string) $siswa['qr_code'] : ('QR-' . $siswa['student_id']);
}

// Coba ambil atau buat file PNG, jika gagal generate langsung via memory
$pngData = null;
try {
    $path = qrcode_ensure_file($targetCode);
    if (is_file($path)) {
        $pngData = file_get_contents($path);
    }
} catch (Throwable $e) {
    // Abaikan error file, fallback ke memori GD langsung
}

if ($pngData === null || $pngData === false || strlen($pngData) === 0) {
    try {
        $qrObj = qrcode_make($targetCode);
        $pngData = qrcode_png_gd($qrObj, 6, 2);
    } catch (Throwable $e) {
        http_response_code(500);
        exit('Gagal membuat gambar QR.');
    }
}

// Bersihkan output buffer jika ada peringatan sebelumnya
if (ob_get_level() > 0) {
    ob_clean();
}

header('Content-Type: image/png');
header('Content-Length: ' . (string) strlen($pngData));

if (isset($_GET['download']) && $_GET['download'] === '1') {
    $identifier = !empty($siswa['student_id']) ? $siswa['student_id'] : ($siswa['nis'] ?? $targetCode);
    $filename = 'QR_' . preg_replace('/[^A-Za-z0-9_-]/', '_', (string) $identifier) . '.png';
    header('Content-Disposition: attachment; filename="' . $filename . '"');
} else {
    header('Content-Disposition: inline');
    header('Cache-Control: public, max-age=86400');
}

echo $pngData;
exit;
