<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

require_login();

$user = current_user();
$isAdmin = ($user['role'] ?? '') === 'admin';
$isSiswa = ($user['role'] ?? '') === 'siswa';

$paramId = trim((string) ($_GET['student_id'] ?? $_GET['id'] ?? ''));
if ($paramId === '') {
    set_flash_message('danger', 'ID siswa tidak valid.');
    redirect('modules/siswa/index.php');
}

$stmt = db()->prepare(
    'SELECT s.*, c.nama_kelas, c.jurusan, c.tingkat, u.username AS user_username, q.qr_code AS qr_code_val 
     FROM students s 
     LEFT JOIN classes c ON c.id = s.class_id 
     LEFT JOIN users u ON u.student_id = s.student_id
     LEFT JOIN qr_codes q ON q.student_id = s.student_id
     WHERE s.student_id = :sid OR s.id = :id 
     LIMIT 1'
);
$stmt->execute([
    ':sid' => $paramId,
    ':id' => is_numeric($paramId) ? (int) $paramId : 0,
]);
$siswa = $stmt->fetch();

if (!$siswa) {
    set_flash_message('danger', 'Data siswa tidak ditemukan.');
    redirect('modules/siswa/index.php');
}

$studentId = (string) ($siswa['student_id'] ?? 'STU001');
$qrCodeString = !empty($siswa['qr_code_val']) ? (string) $siswa['qr_code_val'] : (!empty($siswa['qr_code']) ? (string) $siswa['qr_code'] : (string) $siswa['qr_token']);

// Pastikan file PNG QR tersedia
qrcode_ensure_file($qrCodeString);
$qrUrl = base_url('modules/siswa/qr.php?code=' . urlencode($qrCodeString));
$downloadUrl = base_url('modules/siswa/qr.php?code=' . urlencode($qrCodeString) . '&download=1');

// Ambil riwayat absensi siswa STUxxx ini secara eksklusif menggunakan student_id
$stmtAtt = db()->prepare(
    'SELECT * FROM attendances WHERE student_id = :sid ORDER BY tanggal DESC LIMIT 50'
);
$stmtAtt->execute([':sid' => $studentId]);
$riwayat = $stmtAtt->fetchAll();

// Hitung ringkasan kehadiran siswa ini
$ringkasan = [
    'Hadir' => 0,
    'Terlambat' => 0,
    'Izin' => 0,
    'Sakit' => 0,
    'Alfa' => 0,
];
foreach ($riwayat as $r) {
    $st = $r['status'] ?? '';
    if ($st === 'Alpa') {
        $st = 'Alfa';
    }
    if (isset($ringkasan[$st])) {
        $ringkasan[$st]++;
    }
}
$totalPresensi = count($riwayat);

$pageTitle = 'Detail Siswa - ' . $siswa['nama'] . ' (' . $studentId . ')';
$activeMenu = 'siswa';

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>

        <!-- Navigasi Balik & Judul -->
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
            <div class="d-flex align-items-center gap-3">
                <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/index.php')) ?>" title="Kembali ke Daftar Siswa">
                    <i class="bi bi-arrow-left"></i>
                </a>
                <div>
                    <div class="d-flex align-items-center gap-2 mb-1">
                        <span class="badge bg-primary-subtle text-primary border font-monospace px-2 py-1"><?= e($studentId) ?></span>
                        <h1 class="h3 fw-bold mb-0 text-dark"><?= e($siswa['nama']) ?></h1>
                    </div>
                    <p class="text-secondary small mb-0">
                        NIS: <span class="font-monospace text-dark fw-semibold"><?= e($siswa['nis']) ?></span>
                        <?php if (!empty($siswa['nisn'])): ?>
                            &middot; NISN: <span class="font-monospace text-dark"><?= e($siswa['nisn']) ?></span>
                        <?php endif; ?>
                        &middot; Kelas: <span class="text-dark fw-semibold"><?= e($siswa['nama_kelas'] ?? '-') ?></span>
                        &middot; Username: <span class="badge bg-light text-dark border font-monospace"><?= e($siswa['user_username'] ?? '-') ?></span>
                    </p>
                </div>
            </div>

            <div class="d-flex flex-wrap gap-2">
                <a class="btn btn-outline-primary" href="<?= e($downloadUrl) ?>">
                    <i class="bi bi-download me-1"></i>Unduh QR
                </a>
                <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/kartu.php?id=' . $studentId)) ?>">
                    <i class="bi bi-printer me-1"></i>Cetak Kartu
                </a>
                <?php if ($isAdmin): ?>
                    <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/ubah.php?id=' . $studentId)) ?>">
                        <i class="bi bi-pencil me-1"></i>Edit
                    </a>
                <?php endif; ?>
            </div>
        </div>

        <div class="row g-4">
            <!-- Kolom Kiri: Profil & QR Siswa -->
            <div class="col-12 col-lg-4">
                <!-- Card Profil Siswa -->
                <div class="card border-0 shadow-sm mb-4">
                    <div class="card-body p-4 text-center">
                        <div class="mb-3 position-relative d-inline-block">
                            <?php if (!empty($siswa['foto']) && is_file(ROOT_PATH . '/assets/img/' . basename((string) $siswa['foto']))): ?>
                                <img src="<?= e(base_url('assets/img/' . basename((string) $siswa['foto']))) ?>"
                                    alt="Foto Siswa" width="100" height="100" class="rounded-circle object-fit-cover border shadow-sm">
                            <?php else: ?>
                                <div class="rounded-circle bg-light text-primary mx-auto d-inline-grid place-items-center border" style="width: 100px; height: 100px;">
                                    <i class="bi bi-person fs-1"></i>
                                </div>
                            <?php endif; ?>

                            <span class="badge position-absolute bottom-0 end-0 <?= ((int) ($siswa['is_active'] ?? 1) === 1) ? 'bg-success' : 'bg-danger' ?>" style="font-size: 0.7rem;">
                                <?= ((int) ($siswa['is_active'] ?? 1) === 1) ? 'Aktif' : 'Nonaktif' ?>
                            </span>
                        </div>

                        <span class="badge bg-primary-subtle text-primary border font-monospace mb-1"><?= e($studentId) ?></span>
                        <h2 class="h5 fw-bold text-dark mb-1"><?= e($siswa['nama']) ?></h2>
                        <p class="text-secondary small mb-3"><?= e($siswa['nama_kelas'] ?? '-') ?> &middot; <?= e($siswa['jurusan'] ?? '-') ?></p>

                        <div class="border-top pt-3 text-start small">
                            <div class="d-flex justify-content-between py-1 border-bottom">
                                <span class="text-secondary">Student ID:</span>
                                <strong class="font-monospace text-primary"><?= e($studentId) ?></strong>
                            </div>
                            <div class="d-flex justify-content-between py-1 border-bottom">
                                <span class="text-secondary">NIS:</span>
                                <strong class="font-monospace text-dark"><?= e($siswa['nis']) ?></strong>
                            </div>
                            <div class="d-flex justify-content-between py-1 border-bottom">
                                <span class="text-secondary">NISN:</span>
                                <strong class="font-monospace text-dark"><?= e(!empty($siswa['nisn']) ? $siswa['nisn'] : '-') ?></strong>
                            </div>
                            <div class="d-flex justify-content-between py-1 border-bottom">
                                <span class="text-secondary">Akun Login:</span>
                                <span class="badge bg-light text-dark border font-monospace"><?= e($siswa['user_username'] ?? '-') ?></span>
                            </div>
                            <div class="d-flex justify-content-between py-1 border-bottom">
                                <span class="text-secondary">QR Code ID:</span>
                                <span class="badge bg-light text-secondary border font-monospace"><?= e($qrCodeString) ?></span>
                            </div>
                            <div class="d-flex justify-content-between py-1 border-bottom">
                                <span class="text-secondary">Jenis Kelamin:</span>
                                <span class="badge <?= $siswa['jenis_kelamin'] === 'L' ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-danger-subtle text-danger border border-danger-subtle' ?>">
                                    <?= $siswa['jenis_kelamin'] === 'L' ? 'Laki-laki (L)' : 'Perempuan (P)' ?>
                                </span>
                            </div>
                            <div class="d-flex justify-content-between py-1">
                                <span class="text-secondary">Status Siswa:</span>
                                <span class="badge <?= ((int) ($siswa['is_active'] ?? 1) === 1) ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-danger-subtle text-danger border border-danger-subtle' ?>">
                                    <?= ((int) ($siswa['is_active'] ?? 1) === 1) ? 'Aktif' : 'Tidak Aktif' ?>
                                </span>
                            </div>
                        </div>

                        <?php if (!$isSiswa): ?>
                            <!-- Form Toggle Aktif / Nonaktif -->
                            <form method="post" action="<?= e(base_url('modules/siswa/toggle_status.php')) ?>" class="mt-3">
                                <?php csrf_field(); ?>
                                <input type="hidden" name="id" value="<?= e((string) $siswa['id']) ?>">
                                <button type="submit" class="btn btn-sm w-100 <?= ((int) ($siswa['is_active'] ?? 1) === 1) ? 'btn-outline-danger' : 'btn-outline-success' ?>"
                                    onclick="return confirm('Apakah Anda yakin ingin <?= ((int) ($siswa['is_active'] ?? 1) === 1) ? 'menonaktifkan' : 'mengaktifkan kembali' ?> siswa ini?')">
                                    <i class="bi <?= ((int) ($siswa['is_active'] ?? 1) === 1) ? 'bi-person-slash' : 'bi-person-check' ?> me-1"></i>
                                    <?= ((int) ($siswa['is_active'] ?? 1) === 1) ? 'Nonaktifkan Siswa' : 'Aktifkan Siswa' ?>
                                </button>
                            </form>
                        <?php endif; ?>
                    </div>
                </div>

                <!-- Card QR Siswa -->
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-white border-bottom py-3">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-qr-code text-primary fs-5"></i>
                            <h2 class="h6 fw-bold mb-0 text-dark">QR Code Siswa</h2>
                        </div>
                    </div>
                    <div class="card-body p-4 text-center">
                        <div class="p-3 bg-light rounded-3 d-inline-block border mb-3">
                            <img src="<?= e($qrUrl) ?>" alt="QR <?= e($siswa['nis']) ?>" width="180" height="180" class="d-block mx-auto img-fluid">
                        </div>

                        <p class="text-secondary small mb-2">Identifier Token Unik:</p>
                        <code class="d-block text-break small p-2 bg-light border rounded mb-3 font-monospace">
                            <?= e((string) $siswa['qr_token']) ?>
                        </code>

                        <?php if ($isAdmin): ?>
                            <!-- Form Regenerate Token -->
                            <form method="post" action="<?= e(base_url('modules/siswa/regenerate.php')) ?>"
                                onsubmit="return confirm('Peringatan: Regenerate akan membatalkan QR Code lama siswa ini dan menerbitkan token baru. Lanjutkan?')">
                                <?php csrf_field(); ?>
                                <input type="hidden" name="id" value="<?= e((string) $siswa['id']) ?>">
                                <button type="submit" class="btn btn-sm btn-outline-warning w-100">
                                    <i class="bi bi-arrow-repeat me-1"></i>Regenerate QR Token
                                </button>
                            </form>
                        <?php endif; ?>
                    </div>
                </div>
            </div>

            <!-- Kolom Kanan: Ringkasan & Riwayat Absensi Siswa -->
            <div class="col-12 col-lg-8">
                <!-- Ringkasan Kehadiran Siswa -->
                <div class="row g-2 mb-4">
                    <div class="col-6 col-sm-4 col-md-2.4 col-lg-custom">
                        <div class="card border-0 shadow-sm text-center p-2">
                            <span class="text-secondary small">Hadir</span>
                            <span class="h5 fw-bold mb-0 text-success"><?= e((string) $ringkasan['Hadir']) ?></span>
                        </div>
                    </div>
                    <div class="col-6 col-sm-4 col-md-2.4 col-lg-custom">
                        <div class="card border-0 shadow-sm text-center p-2">
                            <span class="text-secondary small">Terlambat</span>
                            <span class="h5 fw-bold mb-0 text-warning"><?= e((string) $ringkasan['Terlambat']) ?></span>
                        </div>
                    </div>
                    <div class="col-6 col-sm-4 col-md-2.4 col-lg-custom">
                        <div class="card border-0 shadow-sm text-center p-2">
                            <span class="text-secondary small">Izin</span>
                            <span class="h5 fw-bold mb-0 text-info"><?= e((string) $ringkasan['Izin']) ?></span>
                        </div>
                    </div>
                    <div class="col-6 col-sm-4 col-md-2.4 col-lg-custom">
                        <div class="card border-0 shadow-sm text-center p-2">
                            <span class="text-secondary small">Sakit</span>
                            <span class="h5 fw-bold mb-0 text-primary"><?= e((string) $ringkasan['Sakit']) ?></span>
                        </div>
                    </div>
                    <div class="col-6 col-sm-4 col-md-2.4 col-lg-custom">
                        <div class="card border-0 shadow-sm text-center p-2">
                            <span class="text-secondary small">Alfa</span>
                            <span class="h5 fw-bold mb-0 text-danger"><?= e((string) $ringkasan['Alfa']) ?></span>
                        </div>
                    </div>
                </div>

                <!-- Tabel Riwayat Kehadiran Siswa -->
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-calendar-check text-primary fs-5"></i>
                            <h2 class="h6 fw-bold mb-0 text-dark">Riwayat Presensi (<?= e((string) $totalPresensi) ?> Catatan)</h2>
                        </div>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0 table-mobile-card">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 3.5rem" class="text-center">No</th>
                                    <th>Tanggal</th>
                                    <th>Jam Masuk</th>
                                    <th>Status</th>
                                    <th>Keterangan</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php if ($riwayat === []): ?>
                                    <tr>
                                        <td colspan="5" class="empty-state">
                                            <div class="empty-state-icon"><i class="bi bi-calendar-x"></i></div>
                                            <h3 class="h6 fw-bold text-dark mb-1">Belum Ada Riwayat Absensi</h3>
                                            <p class="text-secondary small mb-0">Siswa ini belum memiliki catatan kehadiran.</p>
                                        </td>
                                    </tr>
                                <?php else: ?>
                                    <?php foreach ($riwayat as $i => $att): ?>
                                        <?php 
                                            $stClass = strtolower((string) $att['status']);
                                            if ($stClass === 'alpa') $stClass = 'alfa';
                                        ?>
                                        <tr>
                                            <td data-label="No" class="text-center text-secondary small"><?= e((string) ($i + 1)) ?></td>
                                            <td data-label="Tanggal" class="fw-semibold text-dark">
                                                <?= e(date('d M Y', strtotime((string) $att['tanggal']))) ?>
                                            </td>
                                            <td data-label="Jam Masuk" class="font-monospace">
                                                <?= e($att['jam_masuk'] ? substr((string) $att['jam_masuk'], 0, 5) . ' WIB' : '-') ?>
                                            </td>
                                            <td data-label="Status">
                                                <span class="badge badge-<?= $stClass ?>">
                                                    <?= e($att['status'] === 'Alpa' ? 'Alfa' : $att['status']) ?>
                                                </span>
                                            </td>
                                            <td data-label="Keterangan" class="text-secondary small">
                                                <?= e($att['keterangan'] ?? '-') ?>
                                            </td>
                                        </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
