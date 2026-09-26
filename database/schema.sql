-- Database Schema Absensi Siswa QR Code
-- Sekolah: SMK Tritech Informatika Medan

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Tabel Users (admin, guru, siswa)
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `nama` VARCHAR(100) NOT NULL,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` ENUM('admin', 'guru', 'siswa') NOT NULL DEFAULT 'guru',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tabel Classes (Rombel / Kelas)
CREATE TABLE IF NOT EXISTS `classes` (
    `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `nama_kelas` VARCHAR(50) NOT NULL UNIQUE,
    `jurusan` VARCHAR(100) NOT NULL,
    `tingkat` ENUM('X', 'XI', 'XII') NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tabel Students (Data Siswa & Token QR Code)
CREATE TABLE IF NOT EXISTS `students` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `nis` VARCHAR(30) NOT NULL UNIQUE,
    `nisn` VARCHAR(30) NULL,
    `nama` VARCHAR(100) NOT NULL,
    `class_id` INT UNSIGNED NOT NULL,
    `jenis_kelamin` ENUM('L', 'P') NOT NULL,
    `qr_token` VARCHAR(64) NOT NULL UNIQUE,
    `foto` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_students_class` (`class_id`),
    INDEX `idx_students_nama` (`nama`),
    INDEX `idx_students_is_active` (`is_active`),
    CONSTRAINT `fk_students_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Tabel Attendances (Presensi Harian Siswa)
-- UNIQUE KEY(student_id, tanggal) MENCEGAH DUPLIKASI ABSENSI DI LEVEL DATABASE
CREATE TABLE IF NOT EXISTS `attendances` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `student_id` BIGINT UNSIGNED NOT NULL,
    `tanggal` DATE NOT NULL,
    `jam_masuk` TIME NULL,
    `status` ENUM('Hadir', 'Terlambat', 'Izin', 'Sakit', 'Alfa', 'Alpa') NOT NULL DEFAULT 'Hadir',
    `keterangan` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uq_attendances_student_date` (`student_id`, `tanggal`),
    INDEX `idx_attendances_tanggal` (`tanggal`),
    INDEX `idx_attendances_status` (`status`),
    INDEX `idx_attendances_tanggal_student` (`tanggal`, `student_id`),
    CONSTRAINT `fk_attendances_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Tabel Attendance Settings (Konfigurasi Jam Masuk & Batas Toleransi)
CREATE TABLE IF NOT EXISTS `attendance_settings` (
    `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `school_name` VARCHAR(150) NOT NULL DEFAULT 'SMK TRITECH INFORMATIKA MEDAN',
    `school_start_time` TIME NOT NULL DEFAULT '07:00:00',
    `late_after` TIME NOT NULL DEFAULT '07:00:00',
    `school_address` VARCHAR(255) NULL DEFAULT 'Jl. Bhayangkara No. 434, Medan',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- Default Settings & Users
INSERT INTO `attendance_settings` (`id`, `school_name`, `school_start_time`, `late_after`, `school_address`)
VALUES (1, 'SMK TRITECH INFORMATIKA MEDAN', '07:00:00', '07:00:00', 'Jl. Bhayangkara No. 434, Medan')
ON DUPLICATE KEY UPDATE `school_name` = VALUES(`school_name`);

-- Akun Default (admin, guru, siswa)
-- Password admin: admin123
-- Password guru: guru123
-- Password siswa: siswa123
INSERT INTO `users` (`nama`, `username`, `email`, `password`, `role`) VALUES
('Administrator', 'admin', 'admin@sekolah.local', '$2y$10$dHzHxrWMb1rUhSDQI0qmPOKMJglw0omK7aMR4YwMthAzVQxxPzfiG', 'admin'),
('Guru Piket', 'guru', 'guru@sekolah.local', '$2y$10$hBBnMSW1op6lJ2v15tri4OljznElZrjjiNiyHoI2cVgRf1Iv/I4ya', 'guru'),
('FAIZ DHABIT HARFANDA MANURUNG', 'siswa', 'faiz@siswa.local', '$2y$10$Q7v1v4.r5T3dI9UqgH5oVuYFv0Vqf8h6S6uJt5U7jU3aM9P3CjX7G', 'siswa')
ON DUPLICATE KEY UPDATE
  `nama` = VALUES(`nama`),
  `role` = VALUES(`role`);
