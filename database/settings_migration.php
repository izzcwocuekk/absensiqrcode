<?php
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';

try {
    $pdo = db();
    $pdo->exec("
        CREATE TABLE IF NOT EXISTS `attendance_settings` (
            `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
            `school_name` VARCHAR(150) NOT NULL DEFAULT 'SMK TRITECH INFORMATIKA MEDAN',
            `school_start_time` TIME NOT NULL DEFAULT '07:00:00',
            `late_after` TIME NOT NULL DEFAULT '07:00:00',
            `school_address` VARCHAR(255) NULL DEFAULT 'Jl. Bhayangkara No. 434, Medan',
            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    ");

    $count = (int) $pdo->query("SELECT COUNT(*) FROM attendance_settings")->fetchColumn();
    if ($count === 0) {
        $pdo->exec("
            INSERT INTO `attendance_settings` (`school_name`, `school_start_time`, `late_after`, `school_address`)
            VALUES ('SMK TRITECH INFORMATIKA MEDAN', '07:00:00', '07:00:00', 'Jl. Bhayangkara No. 434, Medan');
        ");
    }

    echo "attendance_settings table initialized successfully!\n";
} catch (Exception $e) {
    echo "Migration error: " . $e->getMessage() . "\n";
}
