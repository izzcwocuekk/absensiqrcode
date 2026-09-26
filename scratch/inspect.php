<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../config/database.php';

try {
    $pdo = db();
    echo "Connected successfully to database.\n";
    $tables = $pdo->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
    echo "Tables in database:\n";
    foreach ($tables as $t) {
        echo " - " . $t . "\n";
        $cols = $pdo->query("DESCRIBE `$t`")->fetchAll(PDO::FETCH_ASSOC);
        foreach ($cols as $c) {
            echo "     " . $c['Field'] . " (" . $c['Type'] . ") " . ($c['Null'] === 'YES' ? 'NULL' : 'NOT NULL') . " " . ($c['Key'] ? '[' . $c['Key'] . ']' : '') . " DEFAULT " . var_export($c['Default'], true) . "\n";
        }
        $count = $pdo->query("SELECT COUNT(*) FROM `$t`")->fetchColumn();
        echo "     Total rows: $count\n\n";
    }
} catch (Exception $e) {
    echo "Database error: " . $e->getMessage() . "\n";
}
