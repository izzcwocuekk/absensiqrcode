<?php
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';
$pdo = db();

foreach (['students','users','qr_codes','attendances'] as $t) {
    $r = $pdo->query("SHOW TABLE STATUS LIKE '$t'")->fetch();
    echo $t . ': ' . ($r['Collation'] ?? 'none') . "\n";
    $cols = $pdo->query("SHOW FULL COLUMNS FROM `$t`")->fetchAll();
    foreach ($cols as $c) {
        if ($c['Collation']) {
            echo "   " . $c['Field'] . ": " . $c['Collation'] . "\n";
        }
    }
}
