<?php
declare(strict_types=1);

if (!defined('ROOT_PATH')) {
    define('ROOT_PATH', dirname(__DIR__));
}

// Override lokal (tidak di-commit bila sensitif): salin dari config.local.example.php
$GLOBALS['APP_LOCAL_OVERRIDE'] = [];
if (is_file(ROOT_PATH . '/config/config.local.php')) {
    $local = require ROOT_PATH . '/config/config.local.php';
    if (is_array($local)) {
        foreach (['APP_NAME', 'BASE_URL', 'APP_TIMEZONE', 'DB_HOST', 'DB_PORT', 'DB_NAME', 'DB_USER', 'DB_PASS'] as $key) {
            if (array_key_exists($key, $local)) {
                $GLOBALS['APP_LOCAL_OVERRIDE'][$key] = $local[$key];
            }
        }
    }
}

if (!defined('APP_NAME')) {
    define('APP_NAME', $GLOBALS['APP_LOCAL_OVERRIDE']['APP_NAME'] ?? 'Sistem Absensi Pembelajaran Sekolah');
}
if (!defined('BASE_URL')) {
    define('BASE_URL', $GLOBALS['APP_LOCAL_OVERRIDE']['BASE_URL'] ?? '/absensi-siswa-qrcode');
}
if (!defined('APP_TIMEZONE')) {
    define('APP_TIMEZONE', $GLOBALS['APP_LOCAL_OVERRIDE']['APP_TIMEZONE'] ?? 'Asia/Jakarta');
}

date_default_timezone_set(APP_TIMEZONE);

require_once ROOT_PATH . '/config/session.php';
require_once ROOT_PATH . '/include/functions.php';
require_once ROOT_PATH . '/include/flash_message.php';
