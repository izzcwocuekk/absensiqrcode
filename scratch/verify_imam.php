<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../config/database.php';

$pdo = db();
$imam = $pdo->query("SELECT * FROM students WHERE nis = 'R.0433.26'")->fetch(PDO::FETCH_ASSOC);
echo "Imam record:\n";
print_r($imam);
