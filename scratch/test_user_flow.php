<?php
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';

echo "=== TEST USER FLOW AS FAIZ (STU009) ===\n";

$_SESSION['user'] = [
    'id' => 4,
    'username' => 'faiz.rpl1',
    'nama' => 'FAIZ DHABIT HARFANDA MANURUNG',
    'role' => 'siswa',
    'student_id' => 'STU009'
];

// 1. Test index.php
ob_start();
include ROOT_PATH . '/index.php';
$outIndex = ob_get_clean();

echo "index.php rendered: " . strlen($outIndex) . " bytes\n";
if (preg_match('/<img[^>]+qr\.php\?code=([^"&]+)[^>]*>/', $outIndex, $m)) {
    echo "index.php QR code found: " . urldecode($m[1]) . "\n";
    $code = urldecode($m[1]);
} else {
    echo "WARNING: index.php QR image tag NOT found!\n";
}

// 2. Test qr_management.php
ob_start();
include ROOT_PATH . '/modules/siswa/qr_management.php';
$outQr = ob_get_clean();

echo "qr_management.php rendered: " . strlen($outQr) . " bytes\n";
if (preg_match('/<img[^>]+qr\.php\?code=([^"&]+)[^>]*>/', $outQr, $m)) {
    echo "qr_management.php QR code found: " . urldecode($m[1]) . "\n";
} else {
    echo "WARNING: qr_management.php QR image tag NOT found!\n";
}

// 3. Test qr.php output for QR-STU009
$_GET = ['code' => 'QR-STU009'];
ob_start();
include ROOT_PATH . '/modules/siswa/qr.php';
$qrImg = ob_get_clean();
echo "qr.php output size for QR-STU009: " . strlen($qrImg) . " bytes\n";

// 4. Test qr.php output if code=STU009
$_GET = ['code' => 'STU009'];
ob_start();
include ROOT_PATH . '/modules/siswa/qr.php';
$qrImg2 = ob_get_clean();
echo "qr.php output size for STU009: " . strlen($qrImg2) . " bytes\n";

// 5. Test qr.php output if code is empty but user is logged in
$_GET = [];
ob_start();
include ROOT_PATH . '/modules/siswa/qr.php';
$qrImg3 = ob_get_clean();
echo "qr.php output size for empty code: " . strlen($qrImg3) . " bytes\n";

