<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../config/database.php';

$pdo = db();
$cols = $pdo->query("SHOW COLUMNS FROM students")->fetchAll(PDO::FETCH_ASSOC);
echo "Columns in students:\n";
foreach ($cols as $c) {
    echo $c['Field'] . " - " . $c['Type'] . " - " . ($c['Null'] == 'YES' ? 'NULL' : 'NOT NULL') . "\n";
}

echo "\nExisting students in DB:\n";
$st = $pdo->query("SELECT s.*, c.nama_kelas FROM students s LEFT JOIN classes c ON c.id = s.class_id LIMIT 10")->fetchAll(PDO::FETCH_ASSOC);
print_r($st);
