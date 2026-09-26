<?php
declare(strict_types=1);

/**
 * Modul Riwayat Absensi & Koreksi Kehadiran Manual.
 * Filter tanggal, filter kelas, filter status, pencarian siswa.
 * Tampilan table di desktop, tampilan card/list di mobile.
 */

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';

require_login();

$user = current_user();
$isAdmin = ($user['role'] ?? '') === 'admin';
$isSiswa = ($user['role'] ?? '') === 'siswa';

$pageTitle = 'Riwayat Absensi';
$activeMenu = 'absensi';

// 1. Parsing Filter GET
$tanggal = (string) ($_GET['tanggal'] ?? date('Y-m-d'));
if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $tanggal)) {
    $tanggal = date('Y-m-d');
}

$kelasId = (int) ($_GET['kelas_id'] ?? 0);
$filterStatus = trim((string) ($_GET['status'] ?? ''));
$cari = trim((string) ($_GET['cari'] ?? ''));

// Daftar Kelas untuk filter dropdown
$kelasList = db()->query('SELECT id, nama_kelas FROM classes ORDER BY nama_kelas')->fetchAll();

// Daftar semua siswa untuk form input koreksi manual (staf only)
$allStudents = [];
if (!$isSiswa) {
    $allStudents = db()->query('SELECT s.id, s.student_id, s.nis, s.nama, c.nama_kelas FROM students s LEFT JOIN classes c ON c.id = s.class_id ORDER BY s.student_id ASC, s.nama ASC')->fetchAll();
}

// 2. Handle POST Koreksi Manual (Staf Only)
if (($_SERVER['REQUEST_METHOD'] ?? '') === 'POST' && ($_POST['aksi'] ?? '') === 'manual') {
    require_staff();
    require_csrf();
    $studentIdInput = trim((string) ($_POST['student_id'] ?? ''));
    $nisInput = trim((string) ($_POST['nis'] ?? ''));
    $status = (string) ($_POST['status'] ?? '');
    $keterangan = trim((string) ($_POST['keterangan'] ?? ''));
    $jamMasuk = trim((string) ($_POST['jam_masuk'] ?? ''));
    $allowed = ['Hadir', 'Terlambat', 'Izin', 'Sakit', 'Alfa', 'Alpa'];

    $siswa = null;
    if ($studentIdInput !== '') {
        $stmt = db()->prepare('SELECT id, student_id, nama, nis FROM students WHERE student_id = :sid OR id = :id LIMIT 1');
        $stmt->execute([':sid' => $studentIdInput, ':id' => is_numeric($studentIdInput) ? (int) $studentIdInput : 0]);
        $siswa = $stmt->fetch();
    } elseif ($nisInput !== '') {
        $stmt = db()->prepare('SELECT id, student_id, nama, nis FROM students WHERE nis = :nis LIMIT 1');
        $stmt->execute([':nis' => $nisInput]);
        $siswa = $stmt->fetch();
    }

    if (!$siswa) {
        set_flash_message('danger', 'Siswa tidak ditemukan. Pilih siswa atau masukkan NIS yang valid.');
    } elseif (!in_array($status, $allowed, true)) {
        set_flash_message('danger', 'Status absensi tidak valid.');
    } else {
        $jam = null;
        if (in_array($status, ['Hadir', 'Terlambat'], true)) {
            $jam = ($jamMasuk !== '' && preg_match('/^\d{2}:\d{2}(:\d{2})?$/', $jamMasuk))
                ? (strlen($jamMasuk) === 5 ? $jamMasuk . ':00' : $jamMasuk)
                : date('H:i:s');
        }

        try {
            $sql = 'INSERT INTO attendances (student_id, tanggal, jam_masuk, status, keterangan)
                    VALUES (:sid, :tgl, :jam, :status, :ket)
                    ON DUPLICATE KEY UPDATE 
                        jam_masuk = VALUES(jam_masuk),
                        status = VALUES(status),
                        keterangan = VALUES(keterangan),
                        updated_at = CURRENT_TIMESTAMP';
            $ins = db()->prepare($sql);
            $ins->execute([
                ':sid' => $siswa['student_id'],
                ':tgl' => $tanggal,
                ':jam' => $jam,
                ':status' => $status,
                ':ket' => $keterangan !== '' ? $keterangan : null,
            ]);
            set_flash_message('success', 'Catatan absensi [' . $siswa['student_id'] . '] "' . $siswa['nama'] . '" berhasil disimpan (' . $status . ').');
        } catch (PDOException $e) {
            set_flash_message('danger', 'Gagal menyimpan absensi: ' . $e->getMessage());
        }
    }

    $redirectQs = http_build_query(['tanggal' => $tanggal, 'kelas_id' => $kelasId, 'status' => $filterStatus, 'cari' => $cari]);
    redirect('modules/absensi/index.php?' . $redirectQs);
}

// 3. Handle POST Hapus Absensi (Admin Only)
if (($_SERVER['REQUEST_METHOD'] ?? '') === 'POST' && ($_POST['aksi'] ?? '') === 'hapus') {
    require_role('admin');
    require_csrf();
    $delId = (int) ($_POST['id'] ?? 0);
    db()->prepare('DELETE FROM attendances WHERE id = :id AND tanggal = :tgl')->execute([
        ':id' => $delId,
        ':tgl' => $tanggal,
    ]);
    set_flash_message('success', 'Catatan kehadiran berhasil dihapus.');

    $redirectQs = http_build_query(['tanggal' => $tanggal, 'kelas_id' => $kelasId, 'status' => $filterStatus, 'cari' => $cari]);
    redirect('modules/absensi/index.php?' . $redirectQs);
}

// 4. Query Data Absensi Berdasarkan Filter
$where = ['a.tanggal = :tgl'];
$params = [':tgl' => $tanggal];

if ($isSiswa) {
    // Jika siswa yang login, tampilkan data miliknya secara akurat berdasarkan student_id
    $myStudentId = (string) ($user['student_id'] ?? '');
    $uname = (string) ($user['username'] ?? '');
    $where[] = '((s.student_id != \'\' AND s.student_id = :mySid) OR s.student_id = (SELECT student_id FROM users WHERE username = :myUname LIMIT 1) OR s.nis = :myUname2)';
    $params[':mySid'] = $myStudentId !== '' ? $myStudentId : 'STU009';
    $params[':myUname'] = $uname;
    $params[':myUname2'] = $uname;
} else {
    if ($kelasId > 0) {
        $where[] = 's.class_id = :kelas_id';
        $params[':kelas_id'] = $kelasId;
    }

    if ($filterStatus !== '') {
        if ($filterStatus === 'Alfa' || $filterStatus === 'Alpa') {
            $where[] = "a.status IN ('Alfa', 'Alpa')";
        } else {
            $where[] = 'a.status = :status';
            $params[':status'] = $filterStatus;
        }
    }

    if ($cari !== '') {
        $where[] = '(s.student_id LIKE :cari OR s.nis LIKE :cari OR s.nisn LIKE :cari OR s.nama LIKE :cari)';
        $params[':cari'] = '%' . $cari . '%';
    }
}

$whereClause = 'WHERE ' . implode(' AND ', $where);

$query = "SELECT a.*, s.student_id, s.nis, s.nisn, s.nama, s.foto, c.nama_kelas
          FROM attendances a
          JOIN students s ON (s.student_id = a.student_id OR s.id = a.student_id)
          LEFT JOIN classes c ON c.id = s.class_id
          $whereClause
          ORDER BY a.id DESC, s.nama ASC";

$stmt = db()->prepare($query);
$stmt->execute($params);
$rows = $stmt->fetchAll();

// Hitung statistik per status
$statRingkas = [
    'Hadir' => 0,
    'Terlambat' => 0,
    'Izin' => 0,
    'Sakit' => 0,
    'Alfa' => 0,
];
foreach ($rows as $r) {
    $st = $r['status'] ?? '';
    if ($st === 'Alpa') {
        $st = 'Alfa';
    }
    if (isset($statRingkas[$st])) {
        $statRingkas[$st]++;
    }
}
$totalCatatan = count($rows);

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
                <h1 class="h3 fw-bold mb-1 text-dark">Riwayat Absensi</h1>
                <p class="text-secondary mb-0">Catatan riwayat kehadiran siswa dan pelacakan presensi harian.</p>
            </div>
            <?php if (!$isSiswa): ?>
                <div class="d-flex gap-2">
                    <a class="btn btn-primary" href="<?= e(base_url('modules/scan/index.php')) ?>">
                        <i class="bi bi-qr-code-scan me-1"></i>Buka Scanner QR
                    </a>
                </div>
            <?php endif; ?>
        </div>

        <!-- Filter & Search Bar -->
        <div class="card border-0 shadow-sm mb-4">
            <div class="card-body p-3">
                <form class="row g-2 align-items-end" method="get" action="<?= e(base_url('modules/absensi/index.php')) ?>">
                    <div class="col-12 col-sm-6 col-md-3">
                        <label class="form-label small fw-semibold text-secondary mb-1" for="filterTanggal">Tanggal</label>
                        <input class="form-control" type="date" id="filterTanggal" name="tanggal" value="<?= e($tanggal) ?>">
                    </div>

                    <?php if (!$isSiswa): ?>
                        <div class="col-6 col-sm-6 col-md-2">
                            <label class="form-label small fw-semibold text-secondary mb-1" for="filterKelas">Kelas</label>
                            <select class="form-select" id="filterKelas" name="kelas_id">
                                <option value="0">Semua Kelas</option>
                                <?php foreach ($kelasList as $k): ?>
                                    <option value="<?= e((string) $k['id']) ?>" <?= $kelasId === (int) $k['id'] ? 'selected' : '' ?>>
                                        <?= e($k['nama_kelas']) ?>
                                    </option>
                                <?php endforeach; ?>
                            </select>
                        </div>

                        <div class="col-6 col-sm-6 col-md-2">
                            <label class="form-label small fw-semibold text-secondary mb-1" for="filterStatus">Status</label>
                            <select class="form-select" id="filterStatus" name="status">
                                <option value="">Semua Status</option>
                                <option value="Hadir" <?= $filterStatus === 'Hadir' ? 'selected' : '' ?>>Hadir</option>
                                <option value="Terlambat" <?= $filterStatus === 'Terlambat' ? 'selected' : '' ?>>Terlambat</option>
                                <option value="Izin" <?= $filterStatus === 'Izin' ? 'selected' : '' ?>>Izin</option>
                                <option value="Sakit" <?= $filterStatus === 'Sakit' ? 'selected' : '' ?>>Sakit</option>
                                <option value="Alfa" <?= ($filterStatus === 'Alfa' || $filterStatus === 'Alpa') ? 'selected' : '' ?>>Alfa</option>
                            </select>
                        </div>

                        <div class="col-12 col-sm-6 col-md-3">
                            <label class="form-label small fw-semibold text-secondary mb-1" for="filterCari">Cari Siswa</label>
                            <div class="input-group">
                                <span class="input-group-text bg-light text-secondary"><i class="bi bi-search"></i></span>
                                <input class="form-control" type="search" id="filterCari" name="cari" placeholder="NIS, NISN, atau Nama..."
                                    value="<?= e($cari) ?>">
                            </div>
                        </div>
                    <?php endif; ?>

                    <div class="col-12 col-md-2 d-flex gap-2">
                        <button class="btn btn-primary flex-fill" type="submit">Filter</button>
                        <?php if ($cari !== '' || $kelasId > 0 || $filterStatus !== '' || $tanggal !== date('Y-m-d')): ?>
                            <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/absensi/index.php')) ?>" title="Reset Filter">
                                <i class="bi bi-arrow-counterclockwise"></i>
                            </a>
                        <?php endif; ?>
                    </div>
                </form>
            </div>
        </div>

        <!-- Kartu Statistik Ringkas Hari Terpilih -->
        <div class="row g-2 mb-4">
            <div class="col-6 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-success"><?= e((string) $statRingkas['Hadir']) ?></p>
                        <span class="badge badge-hadir">Hadir</span>
                    </div>
                </div>
            </div>
            <div class="col-6 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-warning"><?= e((string) $statRingkas['Terlambat']) ?></p>
                        <span class="badge badge-terlambat">Terlambat</span>
                    </div>
                </div>
            </div>
            <div class="col-4 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-info"><?= e((string) $statRingkas['Izin']) ?></p>
                        <span class="badge badge-izin">Izin</span>
                    </div>
                </div>
            </div>
            <div class="col-4 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-primary"><?= e((string) $statRingkas['Sakit']) ?></p>
                        <span class="badge badge-sakit">Sakit</span>
                    </div>
                </div>
            </div>
            <div class="col-4 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-danger"><?= e((string) $statRingkas['Alfa']) ?></p>
                        <span class="badge badge-alfa">Alfa</span>
                    </div>
                </div>
            </div>
        </div>

        <?php if (!$isSiswa): ?>
            <!-- Form Koreksi Kehadiran Manual (Staf Only) -->
            <div class="card border-0 shadow-sm mb-4">
                <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
                    <div class="d-flex align-items-center gap-2">
                        <i class="bi bi-pencil-square text-primary fs-5"></i>
                        <h2 class="h6 fw-bold mb-0 text-dark">Koreksi / Catat Kehadiran Manual</h2>
                    </div>
                    <button class="btn btn-sm btn-link text-decoration-none" type="button" data-bs-toggle="collapse" data-bs-target="#collapseManualForm" aria-expanded="false" aria-controls="collapseManualForm">
                        <i class="bi bi-chevron-down"></i> Buka/Tutup
                    </button>
                </div>
                <div class="collapse" id="collapseManualForm">
                    <div class="card-body p-3 p-md-4">
                        <form class="row g-2 align-items-end" method="post" action="<?= e(base_url('modules/absensi/index.php?' . http_build_query(['tanggal' => $tanggal, 'kelas_id' => $kelasId, 'status' => $filterStatus, 'cari' => $cari]))) ?>">
                            <?php csrf_field(); ?>
                            <input type="hidden" name="aksi" value="manual">

                            <div class="col-12 col-md-4">
                                <label class="form-label small fw-semibold text-secondary mb-1" for="selectStudent">Pilih Siswa</label>
                                <select class="form-select" id="selectStudent" name="student_id" required>
                                    <option value="">-- Pilih Siswa --</option>
                                    <?php foreach ($allStudents as $st): ?>
                                        <option value="<?= e($st['student_id']) ?>">
                                            [<?= e($st['student_id']) ?>] <?= e($st['nis']) ?> - <?= e($st['nama']) ?> (<?= e($st['nama_kelas'] ?? '-') ?>)
                                        </option>
                                    <?php endforeach; ?>
                                </select>
                            </div>

                            <div class="col-6 col-md-2">
                                <label class="form-label small fw-semibold text-secondary mb-1" for="manualStatus">Status</label>
                                <select class="form-select" id="manualStatus" name="status" required>
                                    <option value="Hadir">Hadir</option>
                                    <option value="Terlambat">Terlambat</option>
                                    <option value="Izin">Izin</option>
                                    <option value="Sakit">Sakit</option>
                                    <option value="Alfa">Alfa</option>
                                </select>
                            </div>

                            <div class="col-6 col-md-4">
                                <label class="form-label small fw-semibold text-secondary mb-1" for="manualKeterangan">Keterangan / Alasan</label>
                                <input class="form-control" type="text" id="manualKeterangan" name="keterangan" maxlength="255" placeholder="Contoh: Sakit flu, surat dokter...">
                            </div>

                            <div class="col-12 col-md-2">
                                <button class="btn btn-primary w-100" type="submit">
                                    <i class="bi bi-save me-1"></i>Simpan
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        <?php endif; ?>

        <!-- Tabel / Kartu Daftar Absensi -->
        <div class="card border-0 shadow-sm">
            <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
                <h2 class="h6 fw-bold mb-0 text-dark">
                    Daftar Kehadiran (<?= e((string) $totalCatatan) ?> catatan)
                </h2>
                <span class="badge bg-light text-secondary border">
                    <?= e(date('d F Y', strtotime($tanggal))) ?>
                </span>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 table-mobile-card">
                    <thead class="table-light">
                        <tr>
                            <th style="width:3rem" class="text-center">No</th>
                            <th style="width:7.5rem">Student ID</th>
                            <th>Nama Siswa</th>
                            <th>NIS</th>
                            <th>Kelas</th>
                            <th>Tanggal</th>
                            <th>Jam Masuk</th>
                            <th>Status</th>
                            <th>Keterangan</th>
                            <?php if ($isAdmin): ?>
                                <th style="width:5rem" class="text-center">Aksi</th>
                            <?php endif; ?>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if ($rows === []): ?>
                            <tr>
                                <td colspan="<?= $isAdmin ? 10 : 9 ?>" class="empty-state">
                                    <div class="empty-state-icon"><i class="bi bi-calendar-x"></i></div>
                                    <h3 class="h6 fw-bold text-dark mb-1">Belum Ada Catatan Kehadiran</h3>
                                    <p class="text-secondary small mb-0">Belum ada data absensi untuk filter yang dipilih.</p>
                                </td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($rows as $i => $row): ?>
                                <?php
                                    $stClass = strtolower((string) $row['status']);
                                    if ($stClass === 'alpa') $stClass = 'alfa';
                                    $stLabel = ($row['status'] === 'Alpa') ? 'Alfa' : $row['status'];
                                ?>
                                <tr>
                                    <td data-label="No" class="text-center text-secondary small"><?= e((string) ($i + 1)) ?></td>
                                    <td data-label="Student ID">
                                        <span class="badge bg-light text-primary border font-monospace fw-bold"><?= e($row['student_id'] ?? '-') ?></span>
                                    </td>
                                    <td data-label="Nama Siswa">
                                        <div class="d-flex align-items-center gap-2">
                                            <?php if (!empty($row['foto']) && is_file(ROOT_PATH . '/assets/img/' . basename((string) $row['foto']))): ?>
                                                <img src="<?= e(base_url('assets/img/' . basename((string) $row['foto']))) ?>"
                                                    alt="Foto" width="34" height="34" class="rounded-circle object-fit-cover border flex-shrink-0">
                                            <?php else: ?>
                                                <div class="rounded-circle bg-light text-secondary d-inline-grid place-items-center flex-shrink-0" style="width:34px; height:34px;">
                                                    <i class="bi bi-person"></i>
                                                </div>
                                            <?php endif; ?>
                                            <div>
                                                <a href="<?= e(base_url('modules/siswa/detail.php?id=' . urlencode((string) ($row['student_id'] ?? '')))) ?>" class="fw-semibold text-dark text-decoration-none">
                                                    <?= e($row['nama']) ?>
                                                </a>
                                            </div>
                                        </div>
                                    </td>
                                    <td data-label="NIS" class="font-monospace small"><?= e($row['nis']) ?></td>
                                    <td data-label="Kelas">
                                        <span class="badge bg-light text-dark border"><?= e($row['nama_kelas'] ?? '-') ?></span>
                                    </td>
                                    <td data-label="Tanggal" class="small text-secondary">
                                        <?= e(date('d/m/Y', strtotime((string) $row['tanggal']))) ?>
                                    </td>
                                    <td data-label="Jam Masuk">
                                        <?= $row['jam_masuk'] !== null ? '<span class="fw-semibold text-dark font-monospace">' . e(substr((string) $row['jam_masuk'], 0, 5)) . ' WIB</span>' : '<span class="text-muted">-</span>' ?>
                                    </td>
                                    <td data-label="Status">
                                        <span class="badge badge-<?= $stClass ?>">
                                            <?= e($stLabel) ?>
                                        </span>
                                    </td>
                                    <td data-label="Keterangan" class="text-secondary small">
                                        <?= e($row['keterangan'] ?? '-') ?>
                                    </td>
                                    <?php if ($isAdmin): ?>
                                        <td data-label="Aksi" class="text-center">
                                            <form method="post" action="<?= e(base_url('modules/absensi/index.php?' . http_build_query(['tanggal' => $tanggal, 'kelas_id' => $kelasId, 'status' => $filterStatus, 'cari' => $cari]))) ?>"
                                                onsubmit="return confirm('Hapus catatan kehadiran <?= e($row['nama']) ?>?')">
                                                <?php csrf_field(); ?>
                                                <input type="hidden" name="aksi" value="hapus">
                                                <input type="hidden" name="id" value="<?= e((string) $row['id']) ?>">
                                                <button class="btn btn-sm btn-outline-danger" type="submit" title="Hapus Catatan">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </form>
                                        </td>
                                    <?php endif; ?>
                                </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
