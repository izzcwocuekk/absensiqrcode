<?php
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';
$rows = db()->query('SELECT student_id, nis, nama, qr_code FROM students ORDER BY student_id ASC')->fetchAll();
foreach ($rows as $s) {
    echo $s['student_id'] . ' | ' . $s['nis'] . ' | ' . $s['nama'] . ' | ' . $s['qr_code'] . "\n";
}
echo "\nUser faiz.rpl1:\n";
print_r(db()->query("SELECT id, nama, username, role, student_id FROM users WHERE username = 'faiz.rpl1'")->fetch());

