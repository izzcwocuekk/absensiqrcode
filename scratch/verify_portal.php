<?php
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/scan/fungsi_scan.php';

$pdo = db();

echo "=== VERIFIKASI IDENTITAS & LOGIN FAIZ DHABIT ===\n";

// 1. Cek user faiz.rpl1
$user = $pdo->query("SELECT * FROM users WHERE username = 'faiz.rpl1'")->fetch();
if (!$user) {
    die("ERROR: User faiz.rpl1 tidak ditemukan di database!\n");
}
echo "✓ User faiz.rpl1 ditemukan. ID: " . $user['id'] . ", Role: " . $user['role'] . ", Student ID: " . $user['student_id'] . "\n";

// 2. Cek password hash
if (!password_verify('faiz123', $user['password'])) {
    die("ERROR: Password faiz123 tidak valid!\n");
}
echo "✓ Password faiz123 valid & terverifikasi.\n";

// 3. Cek data student STU009 (Faiz Dhabit)
$student = $pdo->query("SELECT * FROM students WHERE student_id = 'STU009'")->fetch();
if (!$student) {
    die("ERROR: Student STU009 tidak ditemukan di tabel students!\n");
}
echo "✓ Data siswa STU009: " . $student['nama'] . ", NIS: " . $student['nis'] . ", QR Code: " . $student['qr_code'] . "\n";

// 4. Cek data qr_codes
$qr = $pdo->query("SELECT * FROM qr_codes WHERE student_id = 'STU009'")->fetch();
if (!$qr) {
    die("ERROR: Student STU009 tidak memiliki record di qr_codes!\n");
}
echo "✓ Record qr_codes untuk STU009: " . $qr['qr_code'] . ", File: " . $qr['file_path'] . "\n";

// 5. Cek simulasi scan QR-STU009
$testDate = '2026-10-05';
$pdo->prepare("DELETE FROM attendances WHERE tanggal = :tgl")->execute([':tgl' => $testDate]);
$res = catat_scan($pdo, 'QR-STU009', $testDate, '06:50:00');
if (!$res['ok']) {
    die("ERROR: Scan QR-STU009 gagal: " . json_encode($res) . "\n");
}
echo "✓ Scan QR-STU009 berhasil dicatat: " . $res['data']['nama'] . " (" . $res['data']['student_id'] . ") -> Status: " . $res['status'] . "\n";

// 6. Cek absensi tersimpan dengan student_id
$att = $pdo->query("SELECT * FROM attendances WHERE student_id = 'STU009' AND tanggal = '$testDate'")->fetch();
if (!$att) {
    die("ERROR: Absensi tidak tersimpan dengan student_id STU009!\n");
}
echo "✓ Absensi di database tersimpan dengan student_id: " . $att['student_id'] . "\n";

// Bersihkan data tes
$pdo->prepare("DELETE FROM attendances WHERE tanggal = :tgl")->execute([':tgl' => $testDate]);

// 7. Cek detail.php dapat diakses dengan ?id=STU009
$_GET['id'] = 'STU009';
$_SESSION['user'] = [
    'id' => 1,
    'username' => 'admin',
    'nama' => 'Administrator',
    'role' => 'admin',
    'student_id' => null,
];
ob_start();
include ROOT_PATH . '/modules/siswa/detail.php';
$output = ob_get_clean();
if (!str_contains($output, 'FAIZ DHABIT HARFANDA MANURUNG') || !str_contains($output, 'STU009')) {
    die("ERROR: detail.php?id=STU009 gagal merender data Faiz Dhabit!\n");
}
echo "✓ detail.php?id=STU009 berhasil merender data khusus STU009.\n";

// 8. Cek kartu.php dapat diakses dengan ?id=STU009
ob_start();
include ROOT_PATH . '/modules/siswa/kartu.php';
$outputKartu = ob_get_clean();
if (!str_contains($outputKartu, 'FAIZ DHABIT HARFANDA MANURUNG') || !str_contains($outputKartu, 'STU009') || !str_contains($outputKartu, 'QR-STU009')) {
    die("ERROR: kartu.php?id=STU009 gagal merender kartu Faiz Dhabit!\n");
}
echo "✓ kartu.php?id=STU009 berhasil merender kartu identitas STU009.\n";

echo "\n>>> SEMUA VERIFIKASI BERHASIL 100%! <<<\n";
