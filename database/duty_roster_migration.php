<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';

db()->exec(<<<'SQL'
CREATE TABLE IF NOT EXISTS teacher_duty_rosters (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT UNSIGNED NOT NULL,
    duty_date DATE NULL,
    day ENUM('Senin','Selasa','Rabu','Kamis','Jumat','Sabtu','Minggu') NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(150) NOT NULL,
    duty_type VARCHAR(100) NOT NULL DEFAULT 'Piket Sekolah',
    notes TEXT NULL,
    status ENUM('ACTIVE','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_duty_teacher_date (teacher_id, duty_date),
    INDEX idx_duty_day (day, start_time),
    CONSTRAINT fk_duty_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
SQL);

echo "teacher_duty_rosters siap digunakan.\n";
