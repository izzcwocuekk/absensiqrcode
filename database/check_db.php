<?php
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';

$pdo = db();
$tables = $pdo->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
echo "TABLES:\n" . implode("\n", $tables) . "\n\n";

foreach ($tables as $t) {
    echo "--- Table: $t ---\n";
    $cols = $pdo->query("DESCRIBE `$t`")->fetchAll(PDO::FETCH_ASSOC);
    foreach ($cols as $c) {
        echo "  {$c['Field']} ({$c['Type']}) " . ($c['Null'] === 'NO' ? 'NOT NULL' : 'NULL') . " Key:{$c['Key']} Default:{$c['Default']}\n";
    }
}
