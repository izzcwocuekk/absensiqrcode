<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../config/database.php';

$pdo = db();
echo "Students count: " . $pdo->query("SELECT count(*) FROM students")->fetchColumn() . "\n";
echo "Attendances count: " . $pdo->query("SELECT count(*) FROM attendances")->fetchColumn() . "\n";
$s = $pdo->query("SELECT id, nis, nisn, nama, is_active, qr_token FROM students LIMIT 5")->fetchAll(PDO::FETCH_ASSOC);
print_r($s);
