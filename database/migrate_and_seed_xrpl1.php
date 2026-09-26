<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

try {
    $pdo = db();
    echo "=== Starting Migration & Seeding for X RPL 1 ===\n";

    // 1. Tambah kolom nisn pada students jika belum ada
    $checkNisn = $pdo->query("SHOW COLUMNS FROM students LIKE 'nisn'")->fetch();
    if (!$checkNisn) {
        $pdo->exec("ALTER TABLE `students` ADD COLUMN `nisn` VARCHAR(30) NULL AFTER `nis`");
        echo "Column `nisn` added to `students`.\n";
    }

    // 2. Tambah kolom is_active pada students jika belum ada
    $checkActive = $pdo->query("SHOW COLUMNS FROM students LIKE 'is_active'")->fetch();
    if (!$checkActive) {
        $pdo->exec("ALTER TABLE `students` ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT 1 AFTER `foto`");
        echo "Column `is_active` added to `students`.\n";
    }

    // 3. Update ENUM role di users jika belum memuat 'siswa'
    $pdo->exec("ALTER TABLE `users` MODIFY COLUMN `role` ENUM('admin','guru','siswa') NOT NULL DEFAULT 'guru'");
    echo "User roles updated to include 'siswa'.\n";

    // 4. Update ENUM status di attendances agar memuat 'Alfa' & 'Alpa'
    $pdo->exec("ALTER TABLE `attendances` MODIFY COLUMN `status` ENUM('Hadir','Terlambat','Izin','Sakit','Alfa','Alpa') NOT NULL DEFAULT 'Hadir'");
    echo "Attendance statuses updated.\n";

    // 5. Pastikan kelas X RPL 1 ada dan ambil id-nya
    $stmtKelas = $pdo->prepare("SELECT id FROM classes WHERE nama_kelas = 'X RPL 1' LIMIT 1");
    $stmtKelas->execute();
    $classId = $stmtKelas->fetchColumn();

    if (!$classId) {
        $insK = $pdo->prepare("INSERT INTO classes (nama_kelas, jurusan, tingkat) VALUES ('X RPL 1', 'Rekayasa Perangkat Lunak', 'X')");
        $insK->execute();
        $classId = $pdo->lastInsertId();
    }
    echo "Class 'X RPL 1' ID: $classId\n";

    // 6. Data 25 Siswa X RPL 1
    $daftarSiswa = [
        ['nama' => 'ADE VITO MAULANA TANJUNG', 'jk' => 'L', 'nis' => 'R.0400.26', 'nisn' => '0116250064'],
        ['nama' => 'AIRLANGGA RIZKY RAMADHAN', 'jk' => 'L', 'nis' => 'R.0403.26', 'nisn' => '0119555647'],
        ['nama' => 'AKMAL SYAFIQ SYAUQI ALI', 'jk' => 'L', 'nis' => 'R.0406.26', 'nisn' => '0118710366'],
        ['nama' => 'ALGRIBY WINATA', 'jk' => 'L', 'nis' => 'R.0409.26', 'nisn' => '0112459876'],
        ['nama' => 'ALIKA DEA ANANDA', 'jk' => 'P', 'nis' => 'R.0410.26', 'nisn' => '0114941004'],
        ['nama' => 'DAI ZAMZAMI SAID', 'jk' => 'L', 'nis' => 'R.0415.26', 'nisn' => '0114016518'],
        ['nama' => 'DECCO AUFAA DZAMAAR', 'jk' => 'L', 'nis' => 'R.0418.26', 'nisn' => '3118901136'],
        ['nama' => 'DEVIA ANGELICA BR.SILITONGA', 'jk' => 'P', 'nis' => 'R.0419.26', 'nisn' => '0105495936'],
        ['nama' => 'FAIZ DHABIT HARFANDA MANURUNG', 'jk' => 'L', 'nis' => 'R.0422.26', 'nisn' => '0014273374'],
        ['nama' => 'FIQRI DHIO DINATA', 'jk' => 'L', 'nis' => 'R.0425.26', 'nisn' => '0114783889'],
        ['nama' => 'HAMDAN SULAIMAN PULUNGAN', 'jk' => 'L', 'nis' => 'R.0429.26', 'nisn' => '0119107648'],
        ['nama' => 'HIRZI ESHA DAMAIS', 'jk' => 'L', 'nis' => 'R.0430.26', 'nisn' => '3112330259'],
        ['nama' => 'IMAM HANIFAH MARGOLANG', 'jk' => 'L', 'nis' => 'R.0433.26', 'nisn' => null],
        ['nama' => 'IRZA', 'jk' => 'L', 'nis' => 'R.0434.26', 'nisn' => '3117211061'],
        ['nama' => 'M ALIF MAULA', 'jk' => 'L', 'nis' => 'R.0439.26', 'nisn' => '109021787'],
        ['nama' => 'M.LUTHFI AL WAHIDI', 'jk' => 'L', 'nis' => 'R.0442.26', 'nisn' => '0111791550'],
        ['nama' => 'MHD. FIQRY HAIKAL', 'jk' => 'L', 'nis' => 'R.0446.26', 'nisn' => '0113396354'],
        ['nama' => 'MHD. IRSYAD ALFATIH', 'jk' => 'L', 'nis' => 'R.0449.26', 'nisn' => '0115938983'],
        ['nama' => 'MUHAMMAD ATHALLAH PUTRA IRSYI', 'jk' => 'L', 'nis' => 'R.0453.26', 'nisn' => '0127253784'],
        ['nama' => 'MUHAMMAD FACHRURROZI', 'jk' => 'L', 'nis' => 'R.0455.26', 'nisn' => '0114438347'],
        ['nama' => 'NABILA AQILA ARIFAH', 'jk' => 'P', 'nis' => 'R.0460.26', 'nisn' => '3112373899'],
        ['nama' => 'OKA PAHLEFI', 'jk' => 'L', 'nis' => 'R.0461.26', 'nisn' => '0119533929'],
        ['nama' => 'RASYA AL BUQORI', 'jk' => 'L', 'nis' => 'R.0464.26', 'nisn' => '3110252470'],
        ['nama' => 'RIZKY PRATAMA', 'jk' => 'L', 'nis' => 'R.0467.26', 'nisn' => '0111193618'],
        ['nama' => 'TEGUH HARIYADI', 'jk' => 'L', 'nis' => 'R.0472.26', 'nisn' => '0117360875'],
    ];

    // Bersihkan data dummy lama pada attendances dan students
    $pdo->exec("DELETE FROM attendances");
    $pdo->exec("DELETE FROM students");
    echo "Old dummy student and attendance records cleared.\n";

    // Insert 25 Siswa X RPL 1
    $insStmt = $pdo->prepare(
        "INSERT INTO students (nis, nisn, nama, class_id, jenis_kelamin, qr_token, is_active)
         VALUES (:nis, :nisn, :nama, :class_id, :jk, :qr_token, :is_active)"
    );

    $insertedStudents = [];
    foreach ($daftarSiswa as $idx => $s) {
        $token = bin2hex(random_bytes(16));
        $isActive = 1; // Semua aktif secara default

        $insStmt->execute([
            ':nis' => $s['nis'],
            ':nisn' => $s['nisn'],
            ':nama' => $s['nama'],
            ':class_id' => $classId,
            ':jk' => $s['jk'],
            ':qr_token' => $token,
            ':is_active' => $isActive,
        ]);
        $sid = $pdo->lastInsertId();
        $s['id'] = $sid;
        $s['qr_token'] = $token;
        $insertedStudents[] = $s;

        // Generate file PNG QR Code
        qrcode_ensure_file($token);
    }
    echo "Successfully seeded " . count($insertedStudents) . " students with unique secure QR tokens & PNGs.\n";

    // 7. Buat user siswa (Faiz Dhabit) untuk pengujian role siswa
    $passHash = password_hash('siswa123', PASSWORD_DEFAULT);
    $pdo->exec("
        INSERT INTO users (nama, username, email, password, role)
        VALUES ('FAIZ DHABIT HARFANDA MANURUNG', 'siswa', 'faiz@siswa.local', '$passHash', 'siswa')
        ON DUPLICATE KEY UPDATE nama = VALUES(nama), role = 'siswa'
    ");
    echo "User role siswa created: username 'siswa', password 'siswa123'.\n";

    // 8. Seeding data absensi realistis untuk tanggal hari ini dan hari-hari sebelumnya
    // Hari ini: 2026-09-26 (atau current date)
    $today = date('Y-m-d');
    $insAtt = $pdo->prepare(
        "INSERT INTO attendances (student_id, tanggal, jam_masuk, status, keterangan)
         VALUES (:sid, :tgl, :jam, :status, :ket)"
    );

    // Siswa yang sudah absen hari ini (contoh 15 siswa hadir, 3 terlambat, 2 izin, 1 sakit):
    // Faiz Dhabit kita buat sudah hadir jam 06:48 (bisa dipakai test 'siswa yang sudah absen')
    $faiz = null;
    foreach ($insertedStudents as $s) {
        if (str_contains($s['nama'], 'FAIZ DHABIT')) {
            $faiz = $s;
            break;
        }
    }

    if ($faiz) {
        $insAtt->execute([
            ':sid' => $faiz['id'],
            ':tgl' => $today,
            ':jam' => '06:48:15',
            ':status' => 'Hadir',
            ':ket' => null,
        ]);
    }

    // Beberapa siswa lain hari ini:
    $attScheduleToday = [
        0 => ['jam' => '06:42:00', 'status' => 'Hadir', 'ket' => null],
        1 => ['jam' => '06:45:30', 'status' => 'Hadir', 'ket' => null],
        2 => ['jam' => '06:50:10', 'status' => 'Hadir', 'ket' => null],
        3 => ['jam' => '06:55:00', 'status' => 'Hadir', 'ket' => null],
        4 => ['jam' => '06:58:20', 'status' => 'Hadir', 'ket' => null],
        5 => ['jam' => '07:05:12', 'status' => 'Terlambat', 'ket' => 'Kendala ban bocor'],
        6 => ['jam' => '07:12:45', 'status' => 'Terlambat', 'ket' => 'Macet jalan raya'],
        7 => ['jam' => null, 'status' => 'Izin', 'ket' => 'Urusan keluarga'],
        9 => ['jam' => '06:35:10', 'status' => 'Hadir', 'ket' => null],
        10 => ['jam' => null, 'status' => 'Sakit', 'ket' => 'Demam'],
    ];

    foreach ($attScheduleToday as $idx => $att) {
        if (isset($insertedStudents[$idx])) {
            $sid = $insertedStudents[$idx]['id'];
            $insAtt->execute([
                ':sid' => $sid,
                ':tgl' => $today,
                ':jam' => $att['jam'],
                ':status' => $att['status'],
                ':ket' => $att['ket'],
            ]);
        }
    }

    // Seeding tren absensi 5 hari sebelumnya agar grafik tren di dashboard berbobot & valid
    for ($d = 5; $d >= 1; $d--) {
        $pastDate = date('Y-m-d', strtotime("-$d days"));
        // Skip weekend jika mau, tapi untuk grafik absensi isi data bervariasi
        foreach ($insertedStudents as $idx => $st) {
            // Sebagian besar hadir
            $modulo = ($idx + $d) % 10;
            if ($modulo == 0) {
                $stt = 'Terlambat';
                $j = '07:15:00';
            } elseif ($modulo == 1) {
                $stt = 'Izin';
                $j = null;
            } elseif ($modulo == 2) {
                $stt = 'Sakit';
                $j = null;
            } elseif ($modulo == 3) {
                $stt = 'Alfa';
                $j = null;
            } else {
                $stt = 'Hadir';
                $j = '06:5' . ($idx % 9) . ':00';
            }

            try {
                $insAtt->execute([
                    ':sid' => $st['id'],
                    ':tgl' => $pastDate,
                    ':jam' => $j,
                    ':status' => $stt,
                    ':ket' => null,
                ]);
            } catch (Throwable $ignore) {}
        }
    }

    echo "Seed attendances completed successfully!\n";
    echo "=== Finished ===\n";

} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
}
