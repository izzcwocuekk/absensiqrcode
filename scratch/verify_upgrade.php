<?php
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';

$pdo = db();
echo "Students count: " . $pdo->query("SELECT count(*) FROM students")->fetchColumn() . "\n";
echo "QR codes count: " . $pdo->query("SELECT count(*) FROM qr_codes")->fetchColumn() . "\n";
echo "Siswa users: " . $pdo->query("SELECT count(*) FROM users WHERE role = 'siswa'")->fetchColumn() . "\n";
echo "Attendances: " . $pdo->query("SELECT count(*) FROM attendances")->fetchColumn() . "\n\n";

echo "Faiz Dhabit student record:\n";
$faiz = $pdo->query("SELECT student_id, nama, nis, nisn, qr_code, qr_token FROM students WHERE student_id = 'STU001'")->fetch(PDO::FETCH_ASSOC);
print_r($faiz);

echo "\nFaiz Dhabit user record:\n";
$userFaiz = $pdo->query("SELECT id, username, nama, role, student_id FROM users WHERE username = 'faiz.rpl1'")->fetch(PDO::FETCH_ASSOC);
print_r($userFaiz);

echo "\nFaiz QR code record:\n";
$qrFaiz = $pdo->query("SELECT * FROM qr_codes WHERE student_id = 'STU001'")->fetch(PDO::FETCH_ASSOC);
print_r($qrFaiz);

echo "\nAttendance sample:\n";
$att = $pdo->query("SELECT a.id, a.student_id, s.nama, a.tanggal, a.jam_masuk, a.status FROM attendances a JOIN students s ON s.student_id = a.student_id LIMIT 5")->fetchAll(PDO::FETCH_ASSOC);
print_r($att);
