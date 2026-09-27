-- Schema tambahan Sistem Absensi Pembelajaran Sekolah.
-- Jalankan setelah database/schema.sql dan migration lama.
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS teachers (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NULL UNIQUE,
    nama VARCHAR(150) NOT NULL,
    nip VARCHAR(40) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teachers_nama (nama),
    CONSTRAINT fk_teachers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subjects (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    nama_mata_pelajaran VARCHAR(180) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT UNSIGNED NOT NULL,
    class_id INT UNSIGNED NOT NULL,
    subject_id INT UNSIGNED NOT NULL,
    day ENUM('Senin','Selasa','Rabu','Kamis','Jumat','Sabtu') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    lesson_number TINYINT UNSIGNED NOT NULL,
    source_sheet VARCHAR(80) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_schedule_slot (teacher_id, class_id, subject_id, day, lesson_number),
    INDEX idx_schedule_day (day, lesson_number),
    CONSTRAINT fk_schedule_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_schedule_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attendance_sessions (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    -- Token acak yang dicetak menjadi QR khusus satu sesi pembelajaran.
    -- Nullable agar migrasi instalasi lama tetap aman; sesi baru selalu mengisinya.
    session_token VARCHAR(64) NULL,
    schedule_id BIGINT UNSIGNED NOT NULL,
    teacher_id BIGINT UNSIGNED NOT NULL,
    class_id INT UNSIGNED NOT NULL,
    subject_id INT UNSIGNED NOT NULL,
    date DATE NOT NULL,
    lesson_number TINYINT UNSIGNED NOT NULL,
    opened_at DATETIME NOT NULL,
    closed_at DATETIME NULL,
    status ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,
    location_verified TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_session_schedule_date (schedule_id, date),
    UNIQUE KEY uq_session_token (session_token),
    INDEX idx_session_date (date, class_id),
    CONSTRAINT fk_session_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE RESTRICT,
    CONSTRAINT fk_session_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_session_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_session_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT UNSIGNED NOT NULL,
    student_id BIGINT UNSIGNED NOT NULL,
    status ENUM('HADIR','IZIN','SAKIT','ALPA') NOT NULL DEFAULT 'HADIR',
    check_in_time DATETIME NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_record_session_student (session_id, student_id),
    INDEX idx_record_status (status),
    CONSTRAINT fk_record_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_record_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tambahkan token sesi pada instalasi yang sudah lebih dahulu membuat tabel.
SET @session_token_col := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'attendance_sessions'
      AND COLUMN_NAME = 'session_token'
);
SET @session_token_sql := IF(
    @session_token_col = 0,
    'ALTER TABLE attendance_sessions ADD COLUMN session_token VARCHAR(64) NULL AFTER id, ADD UNIQUE KEY uq_session_token (session_token)',
    'SELECT 1'
);
PREPARE session_token_stmt FROM @session_token_sql;
EXECUTE session_token_stmt;
DEALLOCATE PREPARE session_token_stmt;

-- Roster piket guru berdiri terpisah dari jadwal mengajar.
-- duty_date dipakai untuk piket tanggal tertentu; day dapat dipakai untuk
-- pola mingguan. Keduanya tidak menghasilkan sesi absensi pembelajaran.
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Koordinat sekolah dapat diisi admin. Radius dalam meter; enforcement off secara default
-- agar instalasi lama tetap berjalan sampai koordinat sekolah dikonfigurasi.
SET @c1 := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'attendance_settings' AND COLUMN_NAME = 'school_latitude');
SET @sql := IF(@c1 = 0, 'ALTER TABLE attendance_settings ADD school_latitude DECIMAL(10,7) NULL, ADD school_longitude DECIMAL(10,7) NULL, ADD location_radius_m INT UNSIGNED NOT NULL DEFAULT 150, ADD require_school_location TINYINT(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
