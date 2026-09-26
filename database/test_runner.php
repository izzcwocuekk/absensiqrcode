<?php
declare(strict_types=1);

/**
 * Script Uji Komprehensif Alur Absensi & Akses Sistem
 */

require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../modules/scan/fungsi_scan.php';

$pdo = db();

echo "====================================================\n";
echo "MENJALANKAN PENGUJIAN OTOMATIS APLIKASI ABSENSI SISWA\n";
echo "====================================================\n\n";

$passCount = 0;
$failCount = 0;

function assertTest(string $desc, bool $condition, string $detail = '') {
    global $passCount, $failCount;
    if ($condition) {
        $passCount++;
        echo " [PASS] $desc\n";
    } else {
        $failCount++;
        echo " [FAIL] $desc" . ($detail ? " ($detail)" : '') . "\n";
    }
}

// 1. Uji Database: Verifikasi 25 Siswa X RPL 1
$stmt = $pdo->query("SELECT COUNT(*) as total FROM students WHERE class_id = 1");
$totalSiswa = (int) $stmt->fetch()['total'];
assertTest("Jumlah siswa X RPL 1 tepat 25 siswa", $totalSiswa === 25, "Found: $totalSiswa");

// Cek Imam Hanifah Margolang NISN kosong
$stmt = $pdo->prepare("SELECT nisn FROM students WHERE nis = 'R.0433.26'");
$stmt->execute();
$row = $stmt->fetch();
assertTest("Imam Hanifah Margolang (R.0433.26) NISN NULL/kosong", empty($row['nisn']));

// Cek Faiz Dhabit Harfanda Manurung (R.0422.26)
$stmt = $pdo->prepare("SELECT * FROM students WHERE nis = 'R.0422.26'");
$stmt->execute();
$faiz = $stmt->fetch();
assertTest("Faiz Dhabit Harfanda Manurung terdaftar", !empty($faiz));

// 2. Uji Scan: QR Tidak Valid / Data Siswa Tidak Ditemukan
$hasilInvalid = catat_scan($pdo, 'TOKEN_RANDOM_TIDAK_ADA_DI_DATABASE', '2026-09-26', '06:45:00');
assertTest("Scan QR tidak valid menghasilkan code INVALID_CODE", 
    !$hasilInvalid['ok'] && ($hasilInvalid['code'] ?? '') === 'INVALID_CODE', 
    json_encode($hasilInvalid)
);

// 3. Uji Scan: Siswa Tidak Aktif
// Nonaktifkan siswa uji sementara (misal ID 52 - TEGUH HARIYADI)
$teguh = $pdo->query("SELECT * FROM students WHERE nis = 'R.0472.26'")->fetch();
$pdo->prepare("UPDATE students SET is_active = 0 WHERE id = :id")->execute([':id' => $teguh['id']]);

$hasilInactive = catat_scan($pdo, (string) $teguh['qr_token'], '2026-09-26', '06:45:00');
assertTest("Scan siswa nonaktif menghasilkan code STUDENT_INACTIVE", 
    !$hasilInactive['ok'] && ($hasilInactive['code'] ?? '') === 'STUDENT_INACTIVE', 
    json_encode($hasilInactive)
);

// Kembalikan status aktif Teguh Hariyadi
$pdo->prepare("UPDATE students SET is_active = 1 WHERE id = :id")->execute([':id' => $teguh['id']]);

// 4. Uji Scan: Siswa Belum Absen Hari Ini (Gunakan tanggal tes khusus agar tidak mengotori hari ini jika sudah ada)
$testDate = '2026-10-01';
// Bersihkan dulu jika ada data uji pada tanggal ini
$pdo->prepare("DELETE FROM attendances WHERE tanggal = :tgl")->execute([':tgl' => $testDate]);

// Uji scan tepat waktu (06:45:00) -> Status Hadir
$hasilHadir = catat_scan($pdo, (string) $faiz['qr_token'], $testDate, '06:45:00');
assertTest("Scan valid jam 06:45 status Hadir", 
    $hasilHadir['ok'] && ($hasilHadir['status'] ?? '') === 'Hadir', 
    json_encode($hasilHadir)
);

// 5. Uji Scan Ulang (Siswa yang sudah absen hari itu)
$hasilSudah = catat_scan($pdo, (string) $faiz['qr_token'], $testDate, '06:50:00');
assertTest("Scan QR yang sama dua kali menghasilkan code ALREADY_ATTENDED", 
    !$hasilSudah['ok'] && ($hasilSudah['code'] ?? '') === 'ALREADY_ATTENDED', 
    json_encode($hasilSudah)
);
assertTest("Pesan sudah absen tepat: 'Absensi hari ini sudah tercatat.'", 
    ($hasilSudah['pesan'] ?? '') === 'Absensi hari ini sudah tercatat.', 
    $hasilSudah['pesan'] ?? ''
);

// 6. Uji Scan Terlambat (lewat jam batas keterlambatan)
// Gunakan siswa lain: Ade Vito Maulana Tanjung (R.0400.26)
$ade = $pdo->query("SELECT * FROM students WHERE nis = 'R.0400.26'")->fetch();
$hasilTerlambat = catat_scan($pdo, (string) $ade['qr_token'], $testDate, '07:30:00');
assertTest("Scan valid jam 07:30 status Terlambat", 
    $hasilTerlambat['ok'] && ($hasilTerlambat['status'] ?? '') === 'Terlambat', 
    json_encode($hasilTerlambat)
);

// 7. Uji Database Unique Constraint (Mencegah Duplikasi langsung di DB)
$dbDuplicateBlocked = false;
try {
    $pdo->prepare("INSERT INTO attendances (student_id, tanggal, jam_masuk, status) VALUES (:sid, :tgl, '08:00:00', 'Hadir')")
        ->execute([':sid' => $faiz['student_id'], ':tgl' => $testDate]);
} catch (PDOException $e) {
    if ((int) $e->getCode() === 23000) {
        $dbDuplicateBlocked = true;
    }
}
assertTest("Database constraint uq_attendances_student_date menolak duplikasi absensi", $dbDuplicateBlocked);

// 8. Uji Regenerate QR Token
$oldToken = (string) $faiz['qr_token'];
$newToken = bin2hex(random_bytes(16));
$pdo->prepare("UPDATE students SET qr_token = :t WHERE id = :id")->execute([':t' => $newToken, ':id' => $faiz['id']]);

$hasilOldToken = catat_scan($pdo, $oldToken, $testDate, '06:55:00');
assertTest("Token lama ditolak setelah QR di-regenerate", 
    !$hasilOldToken['ok'] && ($hasilOldToken['code'] ?? '') === 'INVALID_CODE'
);

$hasilNewToken = catat_scan($pdo, $newToken, $testDate, '06:55:00');
assertTest("Token baru dikenali sistem", 
    !$hasilNewToken['ok'] && ($hasilNewToken['code'] ?? '') === 'ALREADY_ATTENDED'
);

// Kembalikan token asli Faiz
$pdo->prepare("UPDATE students SET qr_token = :t WHERE id = :id")->execute([':t' => $oldToken, ':id' => $faiz['id']]);

// Bersihkan data tanggal tes
$pdo->prepare("DELETE FROM attendances WHERE tanggal = :tgl")->execute([':tgl' => $testDate]);

// 9. Uji Akun Login Role: admin, guru, siswa
$roles = $pdo->query("SELECT role, COUNT(*) as cnt FROM users GROUP BY role")->fetchAll(PDO::FETCH_KEY_PAIR);
assertTest("Role admin tersedia", isset($roles['admin']) && $roles['admin'] >= 1);
assertTest("Role guru tersedia", isset($roles['guru']) && $roles['guru'] >= 1);
assertTest("Role siswa tersedia", isset($roles['siswa']) && $roles['siswa'] >= 1);

// 10. Uji Identitas Unik 25 Siswa X RPL 1 (STU001 - STU025)
$students25 = $pdo->query("SELECT student_id, nama, nis, nisn, qr_code FROM students ORDER BY student_id ASC")->fetchAll();
assertTest("Jumlah siswa tepat 25 record", count($students25) === 25);
$expectedIds = [];
for ($i = 1; $i <= 25; $i++) {
    $expectedIds[] = sprintf('STU%03d', $i);
}
$actualIds = array_column($students25, 'student_id');
assertTest("Semua 25 siswa memiliki student_id unik STU001 sampai STU025", $actualIds === $expectedIds, "Diff: " . json_encode(array_diff($expectedIds, $actualIds)));

// 11. Uji Akun Login & Profil Faiz Dhabit Harfanda Manurung (Absen 9 - STU009)
$faizUser = $pdo->query("SELECT * FROM users WHERE student_id = 'STU009'")->fetch();
assertTest("User Faiz Dhabit Harfanda Manurung terdaftar dengan student_id STU009 (Absen 9)", !empty($faizUser));
assertTest("Username Faiz adalah faiz.rpl1", ($faizUser['username'] ?? '') === 'faiz.rpl1');
assertTest("Password faiz123 terverifikasi dengan hash", password_verify('faiz123', (string) ($faizUser['password'] ?? '')));
assertTest("Role Faiz adalah siswa", ($faizUser['role'] ?? '') === 'siswa');

$faizStudent = $pdo->query("SELECT * FROM students WHERE student_id = 'STU009'")->fetch();
assertTest("Siswa STU009 nama Faiz Dhabit Harfanda Manurung", ($faizStudent['nama'] ?? '') === 'FAIZ DHABIT HARFANDA MANURUNG');
assertTest("Siswa STU009 qr_code adalah QR-STU009", ($faizStudent['qr_code'] ?? '') === 'QR-STU009');

// Uji Siswa Absen 1 (STU001 - Ade Vito Maulana Tanjung)
$vitoStudent = $pdo->query("SELECT * FROM students WHERE student_id = 'STU001'")->fetch();
assertTest("Siswa STU001 (Absen 1) adalah ADE VITO MAULANA TANJUNG", ($vitoStudent['nama'] ?? '') === 'ADE VITO MAULANA TANJUNG');

// 12. Uji Relasi Tabel Berdasarkan student_id (Foreign Keys)
$qrCodesCount = (int) $pdo->query("SELECT COUNT(*) FROM qr_codes q JOIN students s ON s.student_id = q.student_id")->fetchColumn();
assertTest("Semua 25 siswa memiliki relasi di tabel qr_codes via student_id", $qrCodesCount === 25, "Found: $qrCodesCount");

$studentUsersCount = (int) $pdo->query("SELECT COUNT(*) FROM users u JOIN students s ON s.student_id = u.student_id WHERE u.role = 'siswa'")->fetchColumn();
assertTest("Semua 25 siswa memiliki akun login di tabel users via student_id", $studentUsersCount === 25, "Found: $studentUsersCount");

// 13. Uji Scan QR-STU009: Alur Lengkap Identitas Siswa Faiz Dhabit (Absen 9)
$scanTestDate = '2026-10-02';
$pdo->prepare("DELETE FROM attendances WHERE tanggal = :tgl")->execute([':tgl' => $scanTestDate]);

$scanResult = catat_scan($pdo, 'QR-STU009', $scanTestDate, '06:40:00');
assertTest("Scan QR-STU009 berhasil dicatat", $scanResult['ok'] === true, json_encode($scanResult));
assertTest("Scan QR-STU009 mengembalikan student_id STU009", ($scanResult['data']['student_id'] ?? '') === 'STU009');
assertTest("Scan QR-STU009 mengembalikan nama Faiz Dhabit", ($scanResult['data']['nama'] ?? '') === 'FAIZ DHABIT HARFANDA MANURUNG');
assertTest("Scan QR-STU009 mengembalikan qr_code QR-STU009", ($scanResult['data']['qr_code'] ?? '') === 'QR-STU009');

// Verifikasi tersimpan di tabel attendances dengan student_id STU009
$savedAtt = $pdo->prepare("SELECT * FROM attendances WHERE student_id = 'STU009' AND tanggal = :tgl");
$savedAtt->execute([':tgl' => $scanTestDate]);
$attRow = $savedAtt->fetch();
assertTest("Data absensi tersimpan di database dengan student_id STU009", !empty($attRow) && $attRow['student_id'] === 'STU009');
assertTest("Status absensi tersimpan adalah Hadir", ($attRow['status'] ?? '') === 'Hadir');

// Uji scan ulang QR-STU009 pada tanggal yang sama -> harus ditolak ALREADY_ATTENDED
$scanAgain = catat_scan($pdo, 'QR-STU009', $scanTestDate, '06:45:00');
assertTest("Scan ulang QR-STU009 pada hari yang sama ditolak (ALREADY_ATTENDED)", 
    !$scanAgain['ok'] && ($scanAgain['code'] ?? '') === 'ALREADY_ATTENDED'
);

// 14. Uji Scan langsung dengan Student ID (STU002 - Airlangga & STU001 - Ade Vito)
$scanStu002 = catat_scan($pdo, 'STU002', $scanTestDate, '06:42:00');
assertTest("Scan langsung menggunakan Student ID STU002 berhasil", $scanStu002['ok'] === true);
assertTest("Hasil scan STU002 mengembalikan nama Airlangga Rizky Ramadhan", ($scanStu002['data']['nama'] ?? '') === 'AIRLANGGA RIZKY RAMADHAN');

$scanStu001 = catat_scan($pdo, 'STU001', $scanTestDate, '06:43:00');
assertTest("Scan langsung menggunakan Student ID STU001 berhasil", $scanStu001['ok'] === true);
assertTest("Hasil scan STU001 mengembalikan nama Ade Vito Maulana Tanjung", ($scanStu001['data']['nama'] ?? '') === 'ADE VITO MAULANA TANJUNG');

// Bersihkan data tes scan
$pdo->prepare("DELETE FROM attendances WHERE tanggal = :tgl")->execute([':tgl' => $scanTestDate]);

echo "\n====================================================\n";
echo "HASIL PENGUJIAN: $passCount LULUS, $failCount GAGAL\n";
echo "====================================================\n";

if ($failCount > 0) {
    exit(1);
}
