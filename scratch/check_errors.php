<?php
error_reporting(E_ALL);
ini_set('display_errors', '1');

require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';

echo "=== CHECKING FOR ERRORS ON ALL PAGES FOR SISWA (FAIZ) ===\n";

$_SESSION['user'] = [
    'id' => 4,
    'username' => 'faiz.rpl1',
    'nama' => 'FAIZ DHABIT HARFANDA MANURUNG',
    'role' => 'siswa',
    'student_id' => 'STU009'
];

$pages = [
    'index.php' => ROOT_PATH . '/index.php',
    'modules/siswa/qr_management.php' => ROOT_PATH . '/modules/siswa/qr_management.php',
    'modules/absensi/index.php' => ROOT_PATH . '/modules/absensi/index.php',
    'modules/siswa/detail.php?id=STU009' => function() {
        $_GET['id'] = 'STU009';
        include ROOT_PATH . '/modules/siswa/detail.php';
    },
    'modules/siswa/kartu.php?id=STU009' => function() {
        $_GET['id'] = 'STU009';
        include ROOT_PATH . '/modules/siswa/kartu.php';
    },
];

foreach ($pages as $name => $target) {
    echo "\n--- Testing $name ---\n";
    ob_start();
    try {
        if (is_callable($target)) {
            $target();
        } else {
            include $target;
        }
        $out = ob_get_clean();
        echo "SUCCESS ($name) - length: " . strlen($out) . " bytes\n";
    } catch (Throwable $e) {
        ob_end_clean();
        echo "ERROR ($name): " . $e->getMessage() . " at " . $e->getFile() . ":" . $e->getLine() . "\n";
    }
}
