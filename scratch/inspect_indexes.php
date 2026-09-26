<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../config/database.php';

$pdo = db();
echo "Indexes on attendances:\n";
$stmt = $pdo->query("SHOW INDEX FROM attendances");
foreach ($stmt->fetchAll(PDO::FETCH_ASSOC) as $idx) {
    echo " - " . $idx['Key_name'] . " (" . $idx['Column_name'] . ") Unique: " . ($idx['Non_unique'] == 0 ? 'YES' : 'NO') . "\n";
}

echo "\nClasses:\n";
$classes = $pdo->query("SELECT * FROM classes")->fetchAll(PDO::FETCH_ASSOC);
print_r($classes);

echo "\nStudents count per class:\n";
$counts = $pdo->query("SELECT c.nama_kelas, count(s.id) as total FROM classes c LEFT JOIN students s ON s.class_id = c.id GROUP BY c.id")->fetchAll(PDO::FETCH_ASSOC);
print_r($counts);
