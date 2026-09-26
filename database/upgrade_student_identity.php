<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

try {
    $pdo = db();
    echo "=== UPGRADE STUDENT IDENTITY & UNIQUE COMPONENTS ===\n";

    // 1. Pastikan kolom student_id dan qr_code di students
    $cols = $pdo->query("SHOW COLUMNS FROM students")->fetchAll(PDO::FETCH_COLUMN);
    if (!in_array('student_id', $cols, true)) {
        $pdo->exec("ALTER TABLE `students` ADD COLUMN `student_id` VARCHAR(20) NULL UNIQUE AFTER `id`");
        echo "Column `student_id` added to `students`.\n";
    }
    if (!in_array('qr_code', $cols, true)) {
        $pdo->exec("ALTER TABLE `students` ADD COLUMN `qr_code` VARCHAR(64) NULL UNIQUE AFTER `qr_token`");
        echo "Column `qr_code` added to `students`.\n";
    }

    // 2. Pastikan kolom student_id di users
    $userCols = $pdo->query("SHOW COLUMNS FROM users")->fetchAll(PDO::FETCH_COLUMN);
    if (!in_array('student_id', $userCols, true)) {
        $pdo->exec("ALTER TABLE `users` ADD COLUMN `student_id` VARCHAR(20) NULL UNIQUE AFTER `role`");
        echo "Column `student_id` added to `users`.\n";
    }

    // 3. Buat tabel qr_codes jika belum ada
    $pdo->exec("
        CREATE TABLE IF NOT EXISTS `qr_codes` (
            `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
            `student_id` VARCHAR(20) NOT NULL UNIQUE,
            `qr_code` VARCHAR(64) NOT NULL UNIQUE,
            `file_path` VARCHAR(255) NULL,
            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            INDEX `idx_qrcodes_student` (`student_id`),
            INDEX `idx_qrcodes_code` (`qr_code`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ");
    echo "Table `qr_codes` verified/created.\n";

    // 4. Pastikan kelas X RPL 1 ada
    $stmtKelas = $pdo->prepare("SELECT id FROM classes WHERE nama_kelas = 'X RPL 1' LIMIT 1");
    $stmtKelas->execute();
    $classId = $stmtKelas->fetchColumn();
    if (!$classId) {
        $insK = $pdo->prepare("INSERT INTO classes (nama_kelas, jurusan, tingkat) VALUES ('X RPL 1', 'Rekayasa Perangkat Lunak', 'X')");
        $insK->execute();
        $classId = $pdo->lastInsertId();
    }

    // 5. Data resmi 25 Siswa X RPL 1 urut absen kelas (Faiz Dhabit absen 9 = STU009)
    $daftar25Siswa = [
        [
            'student_id' => 'STU001',
            'nama' => 'ADE VITO MAULANA TANJUNG',
            'jk' => 'L',
            'nis' => 'R.0400.26',
            'nisn' => '0116250064',
            'username' => 'adevito.rpl1',
            'password' => 'adevito123',
            'qr_code' => 'QR-STU001',
        ],
        [
            'student_id' => 'STU002',
            'nama' => 'AIRLANGGA RIZKY RAMADHAN',
            'jk' => 'L',
            'nis' => 'R.0403.26',
            'nisn' => '0119555647',
            'username' => 'airlangga.rpl1',
            'password' => 'airlangga123',
            'qr_code' => 'QR-STU002',
        ],
        [
            'student_id' => 'STU003',
            'nama' => 'AKMAL SYAFIQ SYAUQI ALI',
            'jk' => 'L',
            'nis' => 'R.0406.26',
            'nisn' => '0118710366',
            'username' => 'akmal.rpl1',
            'password' => 'akmal123',
            'qr_code' => 'QR-STU003',
        ],
        [
            'student_id' => 'STU004',
            'nama' => 'ALGRIBY WINATA',
            'jk' => 'L',
            'nis' => 'R.0409.26',
            'nisn' => '0112459876',
            'username' => 'algriby.rpl1',
            'password' => 'algriby123',
            'qr_code' => 'QR-STU004',
        ],
        [
            'student_id' => 'STU005',
            'nama' => 'ALIKA DEA ANANDA',
            'jk' => 'P',
            'nis' => 'R.0410.26',
            'nisn' => '0114941004',
            'username' => 'alika.rpl1',
            'password' => 'alika123',
            'qr_code' => 'QR-STU005',
        ],
        [
            'student_id' => 'STU006',
            'nama' => 'DAI ZAMZAMI SAID',
            'jk' => 'L',
            'nis' => 'R.0415.26',
            'nisn' => '0114016518',
            'username' => 'daizamzami.rpl1',
            'password' => 'daizamzami123',
            'qr_code' => 'QR-STU006',
        ],
        [
            'student_id' => 'STU007',
            'nama' => 'DECCO AUFAA DZAMAAR',
            'jk' => 'L',
            'nis' => 'R.0418.26',
            'nisn' => '3118901136',
            'username' => 'decco.rpl1',
            'password' => 'decco123',
            'qr_code' => 'QR-STU007',
        ],
        [
            'student_id' => 'STU008',
            'nama' => 'DEVIA ANGELICA BR.SILITONGA',
            'jk' => 'P',
            'nis' => 'R.0419.26',
            'nisn' => '0105495936',
            'username' => 'devia.rpl1',
            'password' => 'devia123',
            'qr_code' => 'QR-STU008',
        ],
        [
            'student_id' => 'STU009',
            'nama' => 'FAIZ DHABIT HARFANDA MANURUNG',
            'jk' => 'L',
            'nis' => 'R.0422.26',
            'nisn' => '0014273374',
            'username' => 'faiz.rpl1',
            'password' => 'faiz123',
            'qr_code' => 'QR-STU009',
        ],
        [
            'student_id' => 'STU010',
            'nama' => 'FIQRI DHIO DINATA',
            'jk' => 'L',
            'nis' => 'R.0425.26',
            'nisn' => '0114783889',
            'username' => 'fiqridhio.rpl1',
            'password' => 'fiqridhio123',
            'qr_code' => 'QR-STU010',
        ],
        [
            'student_id' => 'STU011',
            'nama' => 'HAMDAN SULAIMAN PULUNGAN',
            'jk' => 'L',
            'nis' => 'R.0429.26',
            'nisn' => '0119107648',
            'username' => 'hamdan.rpl1',
            'password' => 'hamdan123',
            'qr_code' => 'QR-STU011',
        ],
        [
            'student_id' => 'STU012',
            'nama' => 'HIRZI ESHA DAMAIS',
            'jk' => 'L',
            'nis' => 'R.0430.26',
            'nisn' => '3112330259',
            'username' => 'hirzi.rpl1',
            'password' => 'hirzi123',
            'qr_code' => 'QR-STU012',
        ],
        [
            'student_id' => 'STU013',
            'nama' => 'IMAM HANIFAH MARGOLANG',
            'jk' => 'L',
            'nis' => 'R.0433.26',
            'nisn' => null,
            'username' => 'imamhanifah.rpl1',
            'password' => 'imamhanifah123',
            'qr_code' => 'QR-STU013',
        ],
        [
            'student_id' => 'STU014',
            'nama' => 'IRZA',
            'jk' => 'L',
            'nis' => 'R.0434.26',
            'nisn' => '3117211061',
            'username' => 'irza.rpl1',
            'password' => 'irza123',
            'qr_code' => 'QR-STU014',
        ],
        [
            'student_id' => 'STU015',
            'nama' => 'M ALIF MAULA',
            'jk' => 'L',
            'nis' => 'R.0439.26',
            'nisn' => '109021787',
            'username' => 'alifmaula.rpl1',
            'password' => 'alifmaula123',
            'qr_code' => 'QR-STU015',
        ],
        [
            'student_id' => 'STU016',
            'nama' => 'M.LUTHFI AL WAHIDI',
            'jk' => 'L',
            'nis' => 'R.0442.26',
            'nisn' => '0111791550',
            'username' => 'luthfialwahidi.rpl1',
            'password' => 'luthfialwahidi123',
            'qr_code' => 'QR-STU016',
        ],
        [
            'student_id' => 'STU017',
            'nama' => 'MHD. FIQRY HAIKAL',
            'jk' => 'L',
            'nis' => 'R.0446.26',
            'nisn' => '0113396354',
            'username' => 'fiqryhaikal.rpl1',
            'password' => 'fiqryhaikal123',
            'qr_code' => 'QR-STU017',
        ],
        [
            'student_id' => 'STU018',
            'nama' => 'MHD. IRSYAD ALFATIH',
            'jk' => 'L',
            'nis' => 'R.0449.26',
            'nisn' => '0115938983',
            'username' => 'irsyadalfatih.rpl1',
            'password' => 'irsyadalfatih123',
            'qr_code' => 'QR-STU018',
        ],
        [
            'student_id' => 'STU019',
            'nama' => 'MUHAMMAD ATHALLAH PUTRA IRSYI',
            'jk' => 'L',
            'nis' => 'R.0453.26',
            'nisn' => '0127253784',
            'username' => 'athallah.rpl1',
            'password' => 'athallah123',
            'qr_code' => 'QR-STU019',
        ],
        [
            'student_id' => 'STU020',
            'nama' => 'MUHAMMAD FACHRURROZI',
            'jk' => 'L',
            'nis' => 'R.0455.26',
            'nisn' => '0114438347',
            'username' => 'fachrurrozi.rpl1',
            'password' => 'fachrurrozi123',
            'qr_code' => 'QR-STU020',
        ],
        [
            'student_id' => 'STU021',
            'nama' => 'NABILA AQILA ARIFAH',
            'jk' => 'P',
            'nis' => 'R.0460.26',
            'nisn' => '3112373899',
            'username' => 'nabila.rpl1',
            'password' => 'nabila123',
            'qr_code' => 'QR-STU021',
        ],
        [
            'student_id' => 'STU022',
            'nama' => 'OKA PAHLEFI',
            'jk' => 'L',
            'nis' => 'R.0461.26',
            'nisn' => '0119533929',
            'username' => 'okapahlefi.rpl1',
            'password' => 'okapahlefi123',
            'qr_code' => 'QR-STU022',
        ],
        [
            'student_id' => 'STU023',
            'nama' => 'RASYA AL BUQORI',
            'jk' => 'L',
            'nis' => 'R.0464.26',
            'nisn' => '3110252470',
            'username' => 'rasya.rpl1',
            'password' => 'rasya123',
            'qr_code' => 'QR-STU023',
        ],
        [
            'student_id' => 'STU024',
            'nama' => 'RIZKY PRATAMA',
            'jk' => 'L',
            'nis' => 'R.0467.26',
            'nisn' => '0111193618',
            'username' => 'rizkypratama.rpl1',
            'password' => 'rizkypratama123',
            'qr_code' => 'QR-STU024',
        ],
        [
            'student_id' => 'STU025',
            'nama' => 'TEGUH HARIYADI',
            'jk' => 'L',
            'nis' => 'R.0472.26',
            'nisn' => '0117360875',
            'username' => 'teguh.rpl1',
            'password' => 'teguh123',
            'qr_code' => 'QR-STU025',
        ],
    ];

    // Matikan check FK sementara untuk upgrade struktur attendances jika diperlukan
    $pdo->exec("SET FOREIGN_KEY_CHECKS = 0");

    // 6. Cek struktur tabel attendances: jika student_id masih BIGINT, kita update ke VARCHAR(20)
    $attCols = $pdo->query("SHOW COLUMNS FROM attendances")->fetchAll(PDO::FETCH_ASSOC);
    $attStudentCol = null;
    foreach ($attCols as $c) {
        if ($c['Field'] === 'student_id') {
            $attStudentCol = $c;
            break;
        }
    }

    // Ubah kolom attendances.student_id ke VARCHAR(20) agar langsung memegang STU001..STU025
    if ($attStudentCol && !str_starts_with(strtolower((string) $attStudentCol['Type']), 'varchar')) {
        echo "Modifying attendances.student_id to VARCHAR(20)...\n";
        $pdo->exec("ALTER TABLE `attendances` DROP FOREIGN KEY `fk_attendances_student`");
        $pdo->exec("ALTER TABLE `attendances` MODIFY COLUMN `student_id` VARCHAR(20) NOT NULL");
        echo "attendances.student_id is now VARCHAR(20).\n";
    }

    // 6b. Bersihkan tabrakan nilai unik sementara
    $pdo->exec("SET FOREIGN_KEY_CHECKS = 0");
    $pdo->exec("UPDATE students SET student_id = CONCAT('TMP_', id), qr_code = CONCAT('TMP_', id)");
    $pdo->exec("DELETE FROM qr_codes");
    $pdo->exec("UPDATE users SET student_id = NULL WHERE role = 'siswa'");

    // 7. Update atau Insert 25 Siswa ke tabel students
    $updSiswa = $pdo->prepare("
        INSERT INTO students (student_id, nis, nisn, nama, class_id, jenis_kelamin, qr_token, qr_code, is_active)
        VALUES (:student_id, :nis, :nisn, :nama, :class_id, :jk, :qr_token, :qr_code, 1)
        ON DUPLICATE KEY UPDATE
            student_id = VALUES(student_id),
            nama = VALUES(nama),
            nisn = VALUES(nisn),
            class_id = VALUES(class_id),
            jenis_kelamin = VALUES(jenis_kelamin),
            qr_code = VALUES(qr_code),
            is_active = 1
    ");

    $updQr = $pdo->prepare("
        INSERT INTO qr_codes (student_id, qr_code, file_path)
        VALUES (:student_id, :qr_code, :file_path)
        ON DUPLICATE KEY UPDATE
            qr_code = VALUES(qr_code),
            file_path = VALUES(file_path)
    ");

    $updUser = $pdo->prepare("
        INSERT INTO users (nama, username, email, password, role, student_id)
        VALUES (:nama, :username, :email, :password, 'siswa', :student_id)
        ON DUPLICATE KEY UPDATE
            nama = VALUES(nama),
            email = VALUES(email),
            password = VALUES(password),
            role = 'siswa',
            student_id = VALUES(student_id)
    ");

    foreach ($daftar25Siswa as $s) {
        // Ambil qr_token yang ada atau generate baru
        $existingToken = $pdo->query("SELECT qr_token FROM students WHERE nis = " . $pdo->quote($s['nis']))->fetchColumn();
        $token = $existingToken ?: bin2hex(random_bytes(16));

        $updSiswa->execute([
            ':student_id' => $s['student_id'],
            ':nis' => $s['nis'],
            ':nisn' => $s['nisn'],
            ':nama' => $s['nama'],
            ':class_id' => $classId,
            ':jk' => $s['jk'],
            ':qr_token' => $token,
            ':qr_code' => $s['qr_code'],
        ]);

        // Generate file PNG untuk qr_code (misal QR-STU001) dan token
        $qrPath = qrcode_ensure_file($s['qr_code']);
        qrcode_ensure_file($token);

        $updQr->execute([
            ':student_id' => $s['student_id'],
            ':qr_code' => $s['qr_code'],
            ':file_path' => $qrPath,
        ]);

        // Buat/update user login siswa
        $email = $s['username'] . '@siswa.tritech.sch.id';
        $passHash = password_hash($s['password'], PASSWORD_DEFAULT);

        $updUser->execute([
            ':nama' => $s['nama'],
            ':username' => $s['username'],
            ':email' => $email,
            ':password' => $passHash,
            ':student_id' => $s['student_id'],
        ]);
    }
    echo "Successfully updated all 25 students, QR codes, and login accounts (STU001 to STU025).\n";

    // 8. Hapus siswa lama yang bukan dari 25 siswa X RPL 1 (jika ada dummy)
    $allNis = array_map(fn($item) => $pdo->quote($item['nis']), $daftar25Siswa);
    $pdo->exec("DELETE FROM students WHERE nis NOT IN (" . implode(',', $allNis) . ")");
    echo "Verified clean students table: exactly 25 official students.\n";

    // 9. Migrasi data attendances jika masih ada yang menggunakan integer ID lama
    // Bersihkan atau map attendances lama
    $existingAttendances = $pdo->query("SELECT id, student_id FROM attendances")->fetchAll(PDO::FETCH_ASSOC);
    foreach ($existingAttendances as $att) {
        if (is_numeric($att['student_id'])) {
            // Cari student_id varchar dari students
            $sId = $pdo->query("SELECT student_id FROM students WHERE id = " . (int)$att['student_id'])->fetchColumn();
            if ($sId) {
                $pdo->prepare("UPDATE attendances SET student_id = :sid WHERE id = :id")->execute([
                    ':sid' => $sId,
                    ':id' => $att['id'],
                ]);
            }
        }
    }

    // Pasang foreign key dan unique constraint pada attendances
    // Hapus foreign key lama jika ada
    try {
        $pdo->exec("ALTER TABLE `attendances` ADD CONSTRAINT `fk_attendances_student_id` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE");
        echo "Foreign key fk_attendances_student_id added to attendances.\n";
    } catch (PDOException $e) {
        // Abaikan jika sudah ada
    }

    try {
        $pdo->exec("ALTER TABLE `users` ADD CONSTRAINT `fk_users_student_id` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE SET NULL ON UPDATE CASCADE");
        echo "Foreign key fk_users_student_id added to users.\n";
    } catch (PDOException $e) {
        // Abaikan jika sudah ada
    }

    try {
        $pdo->exec("ALTER TABLE `qr_codes` ADD CONSTRAINT `fk_qrcodes_student_id` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE");
        echo "Foreign key fk_qrcodes_student_id added to qr_codes.\n";
    } catch (PDOException $e) {
        // Abaikan jika sudah ada
    }

    $pdo->exec("SET FOREIGN_KEY_CHECKS = 1");

    // 10. Seeding data absensi realistis untuk tanggal hari ini jika attendances kosong
    $today = date('Y-m-d');
    $cntAtt = (int) $pdo->query("SELECT COUNT(*) FROM attendances WHERE tanggal = '$today'")->fetchColumn();
    if ($cntAtt === 0) {
        $insAtt = $pdo->prepare("
            INSERT INTO attendances (student_id, tanggal, jam_masuk, status, keterangan)
            VALUES (:sid, :tgl, :jam, :status, :ket)
            ON DUPLICATE KEY UPDATE jam_masuk = VALUES(jam_masuk), status = VALUES(status)
        ");

        // Faiz Dhabit Harfanda Manurung (STU009 - Absen 9) sudah hadir
        $insAtt->execute([
            ':sid' => 'STU009',
            ':tgl' => $today,
            ':jam' => '06:48:15',
            ':status' => 'Hadir',
            ':ket' => null,
        ]);

        // Seeding beberapa siswa lain
        $sampleToday = [
            'STU001' => ['jam' => '06:42:00', 'status' => 'Hadir', 'ket' => null],
            'STU002' => ['jam' => '06:45:30', 'status' => 'Hadir', 'ket' => null],
            'STU003' => ['jam' => '06:50:10', 'status' => 'Hadir', 'ket' => null],
            'STU004' => ['jam' => '06:55:00', 'status' => 'Hadir', 'ket' => null],
            'STU005' => ['jam' => '06:58:20', 'status' => 'Hadir', 'ket' => null],
            'STU006' => ['jam' => '07:05:12', 'status' => 'Terlambat', 'ket' => 'Kendala ban bocor'],
            'STU007' => ['jam' => '07:12:45', 'status' => 'Terlambat', 'ket' => 'Macet jalan raya'],
            'STU008' => ['jam' => null, 'status' => 'Izin', 'ket' => 'Urusan keluarga'],
            'STU010' => ['jam' => '06:35:10', 'status' => 'Hadir', 'ket' => null],
            'STU011' => ['jam' => null, 'status' => 'Sakit', 'ket' => 'Demam tinggi'],
        ];

        foreach ($sampleToday as $sid => $item) {
            $insAtt->execute([
                ':sid' => $sid,
                ':tgl' => $today,
                ':jam' => $item['jam'],
                ':status' => $item['status'],
                ':ket' => $item['ket'],
            ]);
        }
        echo "Seeded realistic attendance for today ($today).\n";
    }

    echo "=== MIGRATION & SEEDING COMPLETED SUCCESSFULLY ===\n";
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n" . $e->getTraceAsString() . "\n";
    exit(1);
}
