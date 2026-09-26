<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';

require_staff();

$user = current_user();
$isAdmin = ($user['role'] ?? '') === 'admin';
$isSiswa = ($user['role'] ?? '') === 'siswa';

$pageTitle = 'Data Siswa';
$activeMenu = 'siswa';

// Filter & Pencarian
$cari = trim((string) ($_GET['cari'] ?? ''));
$filterKelas = (int) ($_GET['kelas_id'] ?? 0);
$filterStatus = (string) ($_GET['status'] ?? '');

$kelasList = db()->query('SELECT id, nama_kelas FROM classes ORDER BY nama_kelas')->fetchAll();

// Susun query dengan filter
$where = [];
$params = [];

if ($cari !== '') {
    $where[] = '(s.student_id LIKE :cari OR s.nis LIKE :cari OR s.nisn LIKE :cari OR s.nama LIKE :cari OR s.qr_code LIKE :cari)';
    $params[':cari'] = '%' . $cari . '%';
}

if ($filterKelas > 0) {
    $where[] = 's.class_id = :kelas_id';
    $params[':kelas_id'] = $filterKelas;
}

if ($filterStatus === 'aktif') {
    $where[] = 's.is_active = 1';
} elseif ($filterStatus === 'nonaktif') {
    $where[] = 's.is_active = 0';
}

$whereClause = $where !== [] ? 'WHERE ' . implode(' AND ', $where) : '';

$sql = "SELECT s.*, c.nama_kelas, c.jurusan, u.username AS user_username, q.qr_code AS qr_code_val 
        FROM students s 
        LEFT JOIN classes c ON c.id = s.class_id
        LEFT JOIN users u ON u.student_id = s.student_id
        LEFT JOIN qr_codes q ON q.student_id = s.student_id
        $whereClause 
        ORDER BY s.student_id ASC, s.nama ASC LIMIT 250";

$stmt = db()->prepare($sql);
$stmt->execute($params);
$rows = $stmt->fetchAll();

// Hitung total dan statistik ringkas siswa
$totalSiswa = count($rows);
$totalLaki = 0;
$totalPerempuan = 0;
$totalAktif = 0;
foreach ($rows as $r) {
    if (($r['jenis_kelamin'] ?? '') === 'L') {
        $totalLaki++;
    } elseif (($r['jenis_kelamin'] ?? '') === 'P') {
        $totalPerempuan++;
    }
    if ((int) ($r['is_active'] ?? 1) === 1) {
        $totalAktif++;
    }
}

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>

        <!-- Header Halaman -->
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
            <div>
                <h1 class="h3 fw-bold mb-1 text-dark">Data Siswa</h1>
                <p class="text-secondary mb-0">Kelola data siswa, status aktif, dan QR Code presensi.</p>
            </div>
            <div class="d-flex flex-wrap gap-2">
                <?php if ($isAdmin): ?>
                    <a class="btn btn-outline-success" href="<?= e(base_url('modules/siswa/generate.php')) ?>"
                        title="Buat ulang semua file PNG QR yang belum tersedia">
                        <i class="bi bi-qr-code me-1"></i>Generate Semua QR
                    </a>
                    <a class="btn btn-primary" href="<?= e(base_url('modules/siswa/tambah.php')) ?>">
                        <i class="bi bi-plus-lg me-1"></i>Tambah Siswa
                    </a>
                <?php endif; ?>
            </div>
        </div>

        <!-- Filter & Search Bar -->
        <div class="card border-0 shadow-sm mb-4">
            <div class="card-body p-3">
                <form class="row g-2 align-items-center" method="get" action="<?= e(base_url('modules/siswa/index.php')) ?>">
                    <div class="col-12 col-md-4">
                        <div class="input-group">
                            <span class="input-group-text bg-light text-secondary"><i class="bi bi-search"></i></span>
                            <input class="form-control" type="search" name="cari" placeholder="Cari Student ID, NIS, atau Nama..."
                                value="<?= e($cari) ?>">
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <select class="form-select" name="kelas_id" onchange="this.form.submit()">
                            <option value="0">Semua Kelas</option>
                            <?php foreach ($kelasList as $k): ?>
                                <option value="<?= e((string) $k['id']) ?>" <?= $filterKelas === (int) $k['id'] ? 'selected' : '' ?>>
                                    <?= e($k['nama_kelas']) ?>
                                </option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                    <div class="col-6 col-md-3">
                        <select class="form-select" name="status" onchange="this.form.submit()">
                            <option value="" <?= $filterStatus === '' ? 'selected' : '' ?>>Semua Status</option>
                            <option value="aktif" <?= $filterStatus === 'aktif' ? 'selected' : '' ?>>Hanya Siswa Aktif</option>
                            <option value="nonaktif" <?= $filterStatus === 'nonaktif' ? 'selected' : '' ?>>Hanya Siswa Nonaktif</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-2 d-flex gap-2">
                        <button class="btn btn-primary flex-fill" type="submit">Filter</button>
                        <?php if ($cari !== '' || $filterKelas > 0 || $filterStatus !== ''): ?>
                            <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/index.php')) ?>" title="Reset Filter">
                                <i class="bi bi-arrow-counterclockwise"></i>
                            </a>
                        <?php endif; ?>
                    </div>
                </form>
            </div>
        </div>

        <!-- Ringkasan Singkat -->
        <div class="d-flex align-items-center justify-content-between mb-2 px-1">
            <span class="text-secondary small">
                Menampilkan <strong><?= e((string) $totalSiswa) ?></strong> siswa
                (<strong><?= e((string) $totalLaki) ?></strong> Laki-laki, <strong><?= e((string) $totalPerempuan) ?></strong> Perempuan &middot; 
                <span class="text-success fw-semibold"><?= e((string) $totalAktif) ?> Aktif</span>)
            </span>
        </div>

        <!-- Tabel Data Siswa -->
        <div class="card border-0 shadow-sm">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 table-mobile-card">
                    <thead class="table-light">
                        <tr>
                            <th style="width:3rem" class="text-center">No</th>
                            <th style="width:6.5rem">Student ID</th>
                            <th>Identitas Siswa</th>
                            <th>NIS &amp; NISN</th>
                            <th>Kelas</th>
                            <th>Akun Login</th>
                            <th>QR Code</th>
                            <th style="width:3.5rem" class="text-center">L/P</th>
                            <th class="text-center">Status</th>
                            <th style="width:12rem" class="text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if ($rows === []): ?>
                            <tr>
                                <td colspan="10" class="empty-state">
                                    <div class="empty-state-icon"><i class="bi bi-people"></i></div>
                                    <h3 class="h6 fw-bold text-dark mb-1">Data Siswa Tidak Ditemukan</h3>
                                    <p class="text-secondary small mb-0">Coba kata kunci pencarian lain atau pilih filter yang berbeda.</p>
                                </td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($rows as $i => $row): ?>
                                <?php
                                    $sId = (string) ($row['student_id'] ?? 'STU001');
                                    $qrCodeStr = !empty($row['qr_code_val']) ? $row['qr_code_val'] : (!empty($row['qr_code']) ? $row['qr_code'] : 'QR-' . $sId);
                                ?>
                                <tr data-student-id="<?= e($sId) ?>">
                                    <td data-label="No" class="text-center text-secondary small"><?= e((string) ($i + 1)) ?></td>
                                    <td data-label="Student ID">
                                        <span class="badge bg-primary-subtle text-primary border font-monospace fw-bold"><?= e($sId) ?></span>
                                    </td>
                                    <td data-label="Nama Siswa">
                                        <div class="d-flex align-items-center gap-2">
                                            <?php if (!empty($row['foto']) && is_file(ROOT_PATH . '/assets/img/' . basename((string) $row['foto']))): ?>
                                                <img src="<?= e(base_url('assets/img/' . basename((string) $row['foto']))) ?>"
                                                    alt="Foto" width="36" height="36" class="rounded-circle object-fit-cover border flex-shrink-0">
                                            <?php else: ?>
                                                <div class="rounded-circle bg-light text-secondary d-inline-grid place-items-center flex-shrink-0" style="width:36px; height:36px;">
                                                    <i class="bi bi-person fs-5"></i>
                                                </div>
                                            <?php endif; ?>
                                            <div>
                                                <a href="<?= e(base_url('modules/siswa/detail.php?id=' . urlencode($sId))) ?>" class="fw-semibold text-dark text-decoration-none">
                                                    <?= e($row['nama']) ?>
                                                </a>
                                            </div>
                                        </div>
                                    </td>
                                    <td data-label="NIS / NISN">
                                        <div class="font-monospace small">
                                            <span class="fw-semibold text-dark"><?= e($row['nis']) ?></span>
                                            <?php if (!empty($row['nisn'])): ?>
                                                <div class="text-secondary" style="font-size: 0.72rem;">NISN: <?= e($row['nisn']) ?></div>
                                            <?php endif; ?>
                                        </div>
                                    </td>
                                    <td data-label="Kelas">
                                        <span class="badge bg-light text-dark border"><?= e($row['nama_kelas'] ?? '-') ?></span>
                                    </td>
                                    <td data-label="Akun Login">
                                        <span class="badge bg-light text-dark border font-monospace"><?= e($row['user_username'] ?? ($sId . '.rpl1')) ?></span>
                                    </td>
                                    <td data-label="QR Code">
                                        <span class="badge bg-light text-secondary border font-monospace" title="<?= e($qrCodeStr) ?>">
                                            <i class="bi bi-qr-code me-1 text-success"></i><?= e($qrCodeStr) ?>
                                        </span>
                                    </td>
                                    <td data-label="L/P" class="text-center">
                                        <span class="badge <?= $row['jenis_kelamin'] === 'L' ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-danger-subtle text-danger border border-danger-subtle' ?>">
                                            <?= e($row['jenis_kelamin']) ?>
                                        </span>
                                    </td>
                                    <td data-label="Status" class="text-center">
                                        <?php if ((int) ($row['is_active'] ?? 1) === 1): ?>
                                            <span class="badge bg-success-subtle text-success border border-success-subtle">Aktif</span>
                                        <?php else: ?>
                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Nonaktif</span>
                                        <?php endif; ?>
                                    </td>
                                    <td data-label="Aksi" class="text-center">
                                        <div class="btn-group btn-group-sm" role="group">
                                            <!-- Detail Siswa -->
                                            <a class="btn btn-outline-primary" href="<?= e(base_url('modules/siswa/detail.php?id=' . urlencode($sId))) ?>" title="Lihat Detail & Riwayat Siswa (<?= e($sId) ?>)">
                                                <i class="bi bi-eye"></i>
                                            </a>

                                            <!-- Tombol Modal QR -->
                                            <button type="button" class="btn btn-outline-secondary btn-preview-qr"
                                                data-id="<?= e((string) $row['id']) ?>"
                                                data-student-id="<?= e($sId) ?>"
                                                data-nis="<?= e($row['nis']) ?>"
                                                data-nisn="<?= e($row['nisn'] ?? '-') ?>"
                                                data-nama="<?= e($row['nama']) ?>"
                                                data-kelas="<?= e($row['nama_kelas'] ?? '-') ?>"
                                                data-username="<?= e($row['user_username'] ?? ($sId . '.rpl1')) ?>"
                                                data-qr-code="<?= e($qrCodeStr) ?>"
                                                data-token="<?= e((string) $row['qr_token']) ?>"
                                                data-qr-url="<?= e(base_url('modules/siswa/qr.php?code=' . urlencode($qrCodeStr))) ?>"
                                                data-download-url="<?= e(base_url('modules/siswa/qr.php?code=' . urlencode($qrCodeStr) . '&download=1')) ?>"
                                                data-kartu-url="<?= e(base_url('modules/siswa/kartu.php?id=' . urlencode($sId))) ?>"
                                                title="Lihat QR Code">
                                                <i class="bi bi-qr-code"></i>
                                            </button>

                                            <?php if (!$isSiswa): ?>
                                                <!-- Toggle Aktif / Nonaktif -->
                                                <form method="post" action="<?= e(base_url('modules/siswa/toggle_status.php')) ?>" class="d-inline"
                                                    onsubmit="return confirm('Apakah Anda yakin ingin <?= ((int) ($row['is_active'] ?? 1) === 1) ? 'menonaktifkan' : 'mengaktifkan kembali' ?> siswa ini?')">
                                                    <?php csrf_field(); ?>
                                                    <input type="hidden" name="id" value="<?= e((string) $row['id']) ?>">
                                                    <button type="submit" class="btn btn-outline-<?= ((int) ($row['is_active'] ?? 1) === 1) ? 'warning' : 'success' ?>"
                                                        title="<?= ((int) ($row['is_active'] ?? 1) === 1) ? 'Nonaktifkan Siswa' : 'Aktifkan Siswa' ?>">
                                                        <i class="bi <?= ((int) ($row['is_active'] ?? 1) === 1) ? 'bi-person-slash' : 'bi-person-check' ?>"></i>
                                                    </button>
                                                </form>
                                            <?php endif; ?>

                                            <?php if ($isAdmin): ?>
                                                <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/ubah.php?id=' . urlencode($sId))) ?>" title="Ubah Siswa">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <a class="btn btn-outline-danger" href="<?= e(base_url('modules/siswa/hapus.php?id=' . urlencode($sId))) ?>" title="Hapus Siswa">
                                                    <i class="bi bi-trash"></i>
                                                </a>
                                            <?php endif; ?>
                                        </div>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<!-- Modal Preview QR Siswa -->
<div class="modal fade" id="modalStudentQR" tabindex="-1" aria-labelledby="modalStudentQRTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-sm" style="max-width: 380px;">
        <div class="modal-content text-center p-3 border-0 shadow">
            <div class="modal-header border-0 pb-0 justify-content-between">
                <h3 class="h6 fw-bold mb-0 text-dark" id="modalStudentQRTitle">QR Code Siswa</h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body p-3">
                <div class="p-3 bg-light rounded-3 d-inline-block border mb-3">
                    <img id="modalQRImg" src="" alt="QR Siswa" width="180" height="180" class="d-block mx-auto img-fluid">
                </div>

                <div class="mb-1">
                    <span class="badge bg-primary-subtle text-primary border font-monospace px-2 py-1" id="modalQRStudentId">STU001</span>
                </div>
                <h4 class="h5 fw-bold mb-1 text-dark" id="modalQRStudentName">-</h4>
                <p class="text-secondary small mb-2" id="modalQRStudentInfo">NIS - &middot; -</p>
                <p class="text-muted small mb-3">
                    Username: <span class="badge bg-light text-dark border font-monospace" id="modalQRUsername">-</span>
                </p>

                <div class="attendance-detail-box text-start mb-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-secondary small">QR Code:</span>
                        <code class="small text-dark font-monospace fw-bold" id="modalQRCodeText">-</code>
                    </div>
                </div>

                <div class="d-grid gap-2">
                    <a id="btnDownloadQR" href="#" class="btn btn-outline-primary btn-sm">
                        <i class="bi bi-download me-1"></i>Unduh PNG QR
                    </a>
                    <a id="btnPrintKartu" href="#" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-printer me-1"></i>Cetak Kartu Siswa
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
'use strict';
document.addEventListener('DOMContentLoaded', () => {
    const modalEl = document.getElementById('modalStudentQR');
    if (!modalEl) return;
    const modalQR = new bootstrap.Modal(modalEl);

    document.querySelectorAll('.btn-preview-qr').forEach((btn) => {
        btn.addEventListener('click', () => {
            const sid = btn.getAttribute('data-student-id');
            const nama = btn.getAttribute('data-nama');
            const nis = btn.getAttribute('data-nis');
            const kelas = btn.getAttribute('data-kelas');
            const username = btn.getAttribute('data-username');
            const qrCode = btn.getAttribute('data-qr-code');
            const qrUrl = btn.getAttribute('data-qr-url');
            const downloadUrl = btn.getAttribute('data-download-url');
            const kartuUrl = btn.getAttribute('data-kartu-url');

            document.getElementById('modalQRStudentId').textContent = sid || 'STU001';
            document.getElementById('modalQRStudentName').textContent = nama;
            document.getElementById('modalQRStudentInfo').textContent = `NIS ${nis} · ${kelas}`;
            document.getElementById('modalQRUsername').textContent = username || '-';
            document.getElementById('modalQRCodeText').textContent = qrCode || '-';
            document.getElementById('modalQRImg').src = qrUrl;

            document.getElementById('btnDownloadQR').href = downloadUrl;
            document.getElementById('btnPrintKartu').href = kartuUrl;

            modalQR.show();
        });
    });
});
</script>
<?php require ROOT_PATH . '/include/footer.php'; ?>
