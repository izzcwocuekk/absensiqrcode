<?php
declare(strict_types=1);

if (!defined('DB_HOST')) {
    define('DB_HOST', $GLOBALS['APP_LOCAL_OVERRIDE']['DB_HOST'] ?? 'localhost');
}
if (!defined('DB_PORT')) {
    define('DB_PORT', $GLOBALS['APP_LOCAL_OVERRIDE']['DB_PORT'] ?? '3306');
}
if (!defined('DB_NAME')) {
    define('DB_NAME', $GLOBALS['APP_LOCAL_OVERRIDE']['DB_NAME'] ?? 'absensi_siswa');
}
if (!defined('DB_USER')) {
    define('DB_USER', $GLOBALS['APP_LOCAL_OVERRIDE']['DB_USER'] ?? 'root');
}
if (!defined('DB_PASS')) {
    define('DB_PASS', $GLOBALS['APP_LOCAL_OVERRIDE']['DB_PASS'] ?? '');
}

function db(): PDO
{
    static $pdo = null;

    if ($pdo === null) {
        $dsn = sprintf(
            'mysql:host=%s;port=%s;dbname=%s;charset=utf8mb4',
            DB_HOST,
            DB_PORT,
            DB_NAME
        );

        $pdo = new PDO($dsn, DB_USER, DB_PASS, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]);
    }

    return $pdo;
}