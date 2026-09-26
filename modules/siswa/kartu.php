<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

require_login();

$pageTitle = 'Kartu Siswa';
$activeMenu = 'siswa';

$paramId = trim((string) ($_GET['student_id'] ?? $_GET['id'] ?? ''));
$stmt = db()->prepare(
    'SELECT s.*, c.nama_kelas, c.jurusan, c.tingkat, u.username AS user_username, q.qr_code AS qr_code_val
     FROM students s 
     LEFT JOIN classes c ON c.id = s.class_id 
     LEFT JOIN users u ON u.student_id = s.student_id
     LEFT JOIN qr_codes q ON q.student_id = s.student_id
     WHERE s.student_id = :sid OR s.id = :id LIMIT 1'
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

// Pastikan PNG tersedia agar kartu bisa cetak offline
qrcode_ensure_file($qrCodeString);
$qrUrl = base_url('modules/siswa/qr.php?code=' . urlencode($qrCodeString));
$hasFoto = !empty($siswa['foto']) && is_file(ROOT_PATH . '/assets/img/' . basename((string) $siswa['foto']));

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>

        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4 no-print">
            <div>
                <h1 class="h3 fw-bold mb-1 text-dark">Kartu Siswa &amp; QR Presensi</h1>
                <p class="text-secondary mb-0">Kartu identitas resmi siswa dengan Student ID dan QR Code presensi.</p>
            </div>
            <div class="d-flex gap-2">
                <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/detail.php?id=' . $studentId)) ?>">
                    <i class="bi bi-arrow-left me-1"></i>Kembali
                </a>
                <button class="btn btn-primary" type="button" onclick="window.print()">
                    <i class="bi bi-printer me-1"></i>Cetak Kartu
                </button>
            </div>
        </div>

        <!-- Tampilan Kartu Siswa Modern -->
        <div class="kartu-cetak-wrapper mx-auto">
            <div class="kartu-siswa card border-0 shadow-sm overflow-hidden">
                <!-- Header Kartu -->
                <div class="kartu-header text-white p-3 text-center" style="background: linear-gradient(135deg, #064e2b 0%, #0d8a4f 60%, #6a1e5a 100%);">
                    <div class="d-flex align-items-center justify-content-center gap-2 mb-1">
                        <img src="<?= e(base_url('assets/img/logo.png')) ?>" alt="Logo" width="34" height="34" class="object-fit-contain">
                        <span class="fw-bold small text-uppercase" style="letter-spacing: 0.05em;">SMK TRITECH INFORMATIKA</span>
                    </div>
                    <p class="mb-0 fw-semibold" style="font-size: 0.78rem; opacity: 0.9;">KARTU IDENTITAS SISWA &amp; PRESENSI</p>
                </div>

                <!-- Body Kartu -->
                <div class="card-body p-4 text-center bg-white">
                    <div class="mb-2">
                        <?php if ($hasFoto): ?>
                            <img src="<?= e(base_url('assets/img/' . basename((string) $siswa['foto']))) ?>"
                                alt="Foto Siswa" width="90" height="90" class="rounded-circle object-fit-cover border border-3 border-light shadow-sm">
                        <?php else: ?>
                            <div class="rounded-circle bg-light text-primary mx-auto d-inline-grid place-items-center border border-2 border-primary-subtle shadow-sm" style="width: 84px; height: 84px;">
                                <i class="bi bi-person-fill fs-1"></i>
                            </div>
                        <?php endif; ?>
                    </div>

                    <div class="mb-2">
                        <span class="badge bg-primary-subtle text-primary border font-monospace px-2 py-1"><?= e($studentId) ?></span>
                    </div>

                    <h2 class="h5 fw-bold mb-1 text-dark"><?= e($siswa['nama']) ?></h2>
                    <p class="text-secondary small mb-2">
                        NIS: <strong class="text-dark font-monospace"><?= e($siswa['nis']) ?></strong> &middot; <?= e($siswa['nama_kelas'] ?? 'X RPL 1') ?>
                    </p>
                    <p class="text-muted small mb-3">
                        Username: <span class="badge bg-light text-dark border font-monospace"><?= e($siswa['user_username'] ?? ($studentId . '.rpl1')) ?></span>
                    </p>

                    <!-- QR Code Box -->
                    <div class="p-3 bg-light rounded-3 d-inline-block border mb-2">
                        <img src="<?= e($qrUrl) ?>" alt="QR <?= e($studentId) ?>" width="170" height="170" class="d-block mx-auto img-fluid">
                    </div>

                    <div class="d-flex align-items-center justify-content-center gap-2">
                        <span class="badge bg-dark-subtle text-dark border font-monospace"><?= e($qrCodeString) ?></span>
                    </div>
                </div>

                <!-- Footer Kartu -->
                <div class="card-footer bg-light py-2 text-center border-top">
                    <small class="text-secondary" style="font-size: 0.72rem;">
                        Tunjukkan kartu ini ke kamera scanner saat melakukan presensi.
                    </small>
                </div>
            </div>
        </div>
    </div>
</main>

<style>
.kartu-cetak-wrapper {
    max-width: 340px;
}
.kartu-siswa {
    border-radius: 1rem;
    border: 1px solid var(--app-border) !important;
}
@media print {
    .no-print, .app-navbar, .app-sidebar, .app-footer {
        display: none !important;
    }
    .main-content, .app-footer {
        margin-left: 0 !important;
        padding-top: 0 !important;
    }
    .kartu-cetak-wrapper {
        margin-top: 1cm;
        max-width: 8.6cm;
    }
    .kartu-siswa {
        box-shadow: none !important;
        border: 1px solid #1e3a8a !important;
        page-break-inside: avoid;
    }
}
</style>

<?php require ROOT_PATH . '/include/footer.php'; ?>
