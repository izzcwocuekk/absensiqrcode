<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/laporan/fungsi_laporan.php';
require_once ROOT_PATH . '/modules/learning/functions.php';

require_staff();

$pageTitle = 'Laporan Absensi';
$activeMenu = 'laporan';

$filter = laporan_filter();
$kelasList = db()->query('SELECT id, nama_kelas FROM classes ORDER BY nama_kelas')->fetchAll();
$teacherList = learning_tables_ready(db()) ? db()->query('SELECT id, nama FROM teachers WHERE is_active=1 ORDER BY nama')->fetchAll() : [];
$subjectList = learning_tables_ready(db()) ? db()->query('SELECT id, nama_mata_pelajaran FROM subjects ORDER BY nama_mata_pelajaran')->fetchAll() : [];
[$rows, $ringkas] = laporan_data(db(), $filter['dari'], $filter['sampai'], $filter['kelas_id'], $filter['teacher_id'], $filter['subject_id'], $filter['lesson_number'], $filter['status']);

$qs = http_build_query($filter);

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>

        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4 no-print">
            <div>
                <h1 class="h3 fw-bold mb-1 text-dark">Laporan Rekap Absensi</h1>
                <p class="text-secondary mb-0">Rekap kehadiran siswa per rentang tanggal dan kelas.</p>
            </div>
            <div class="d-flex gap-2">
                <button class="btn btn-outline-secondary" type="button" onclick="window.print()">
                    <i class="bi bi-printer me-1"></i>Cetak Laporan
                </button>
                <a class="btn btn-success" href="<?= e(base_url('modules/laporan/export.php?' . $qs)) ?>">
                    <i class="bi bi-file-earmark-spreadsheet me-1"></i>Ekspor CSV
                </a>
            </div>
        </div>

        <!-- Filter Periode & Kelas -->
        <div class="card border-0 shadow-sm mb-4 no-print">
            <div class="card-body p-3">
                <form class="row g-2 align-items-end" method="get" action="<?= e(base_url('modules/laporan/index.php')) ?>">
                    <div class="col-6 col-md-3">
                        <label class="form-label small fw-semibold text-secondary mb-1" for="dari">Dari Tanggal</label>
                        <input class="form-control" type="date" id="dari" name="dari" value="<?= e($filter['dari']) ?>">
                    </div>
                    <div class="col-6 col-md-3">
                        <label class="form-label small fw-semibold text-secondary mb-1" for="sampai">Sampai Tanggal</label>
                        <input class="form-control" type="date" id="sampai" name="sampai" value="<?= e($filter['sampai']) ?>">
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label small fw-semibold text-secondary mb-1" for="kelas_id">Filter Kelas</label>
                        <select class="form-select" id="kelas_id" name="kelas_id">
                            <option value="0">Semua Kelas</option>
                            <?php foreach ($kelasList as $k): ?>
                                <option value="<?= e((string) $k['id']) ?>" <?= $filter['kelas_id'] === (int) $k['id'] ? 'selected' : '' ?>>
                                    <?= e($k['nama_kelas']) ?>
                                </option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                    <div class="col-12 col-md-3">
                        <label class="form-label small fw-semibold text-secondary mb-1" for="teacher_id">Guru</label>
                        <select class="form-select" id="teacher_id" name="teacher_id"><option value="0">Semua Guru</option><?php foreach ($teacherList as $teacher): ?><option value="<?=e((string)$teacher['id'])?>" <?=$filter['teacher_id']==(int)$teacher['id']?'selected':''?>><?=e($teacher['nama'])?></option><?php endforeach; ?></select>
                    </div>
                    <div class="col-12 col-md-3">
                        <label class="form-label small fw-semibold text-secondary mb-1" for="subject_id">Mata Pelajaran</label>
                        <select class="form-select" id="subject_id" name="subject_id"><option value="0">Semua Mata Pelajaran</option><?php foreach ($subjectList as $subject): ?><option value="<?=e((string)$subject['id'])?>" <?=$filter['subject_id']==(int)$subject['id']?'selected':''?>><?=e($subject['nama_mata_pelajaran'])?></option><?php endforeach; ?></select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label small fw-semibold text-secondary mb-1" for="lesson_number">Les</label>
                        <select class="form-select" id="lesson_number" name="lesson_number"><option value="0">Semua Les</option><?php for($les=1;$les<=9;$les++): ?><option value="<?=$les?>" <?=$filter['lesson_number']==$les?'selected':''?>>Les <?=$les?></option><?php endfor; ?></select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label small fw-semibold text-secondary mb-1" for="status">Status</label>
                        <select class="form-select" id="status" name="status"><option value="">Semua Status</option><?php foreach(['HADIR','IZIN','SAKIT','ALPA'] as $st): ?><option value="<?=$st?>" <?=$filter['status']===$st?'selected':''?>><?=$st?></option><?php endforeach; ?></select>
                    </div>
                    <div class="col-12 col-md-2">
                        <button class="btn btn-primary w-100" type="submit">
                            <i class="bi bi-funnel me-1"></i>Tampilkan
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Ringkasan Statistik Periode -->
        <div class="row g-2 mb-4">
            <div class="col-6 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-success"><?= e((string) ($ringkas['Hadir'] ?? 0)) ?></p>
                        <span class="badge badge-hadir">Hadir</span>
                    </div>
                </div>
            </div>
            <div class="col-6 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-warning"><?= e((string) ($ringkas['Terlambat'] ?? 0)) ?></p>
                        <span class="badge badge-terlambat">Terlambat</span>
                    </div>
                </div>
            </div>
            <div class="col-4 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-info"><?= e((string) ($ringkas['Izin'] ?? 0)) ?></p>
                        <span class="badge badge-izin">Izin</span>
                    </div>
                </div>
            </div>
            <div class="col-4 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-primary"><?= e((string) ($ringkas['Sakit'] ?? 0)) ?></p>
                        <span class="badge badge-sakit">Sakit</span>
                    </div>
                </div>
            </div>
            <div class="col-4 col-md">
                <div class="card border-0 shadow-sm text-center">
                    <div class="card-body py-2 px-1">
                        <p class="h5 fw-bold mb-0 text-danger"><?= e((string) ($ringkas['Alfa'] ?? $ringkas['Alpa'] ?? 0)) ?></p>
                        <span class="badge badge-alfa">Alfa</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Tabel Laporan -->
        <div class="card border-0 shadow-sm area-cetak">
            <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
                <h2 class="h6 fw-bold mb-0 text-dark">
                    Data Rekap (<?= e((string) count($rows)) ?> baris)
                </h2>
                <span class="badge bg-light text-secondary border">
                    Periode: <?= e($filter['dari']) ?> s/d <?= e($filter['sampai']) ?>
                </span>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 table-mobile-card">
                    <thead class="table-light">
                        <tr>
                            <th style="width:3.5rem" class="text-center">No</th>
                            <th style="width:7.5rem">Student ID</th>
                            <th>Tanggal</th>
                            <th>NIS</th>
                            <th>Nama Siswa</th>
                            <th>Kelas</th>
                            <th>Guru</th>
                            <th>Mata Pelajaran</th>
                            <th>Les</th>
                            <th>Jam Masuk</th>
                            <th>Status</th>
                            <th>Keterangan</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if ($rows === []): ?>
                            <tr>
                                <td colspan="12" class="empty-state">
                                    <div class="empty-state-icon"><i class="bi bi-file-earmark-x"></i></div>
                                    <h3 class="h6 fw-bold text-dark mb-1">Tidak Ada Data Absensi</h3>
                                    <p class="text-secondary small mb-0">Tidak ditemukan catatan absensi pada periode dan kelas yang dipilih.</p>
                                </td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($rows as $i => $row): ?>
                                <tr>
                                    <td data-label="No" class="text-center text-secondary small"><?= e((string) ($i + 1)) ?></td>
                                    <td data-label="Student ID">
                                        <span class="badge bg-light text-primary border font-monospace fw-bold"><?= e($row['student_id'] ?? '-') ?></span>
                                    </td>
                                    <td data-label="Tanggal" class="fw-semibold text-dark"><?= e($row['tanggal']) ?></td>
                                    <td data-label="NIS" class="font-monospace text-secondary"><?= e($row['nis']) ?></td>
                                    <td data-label="Nama" class="fw-semibold text-dark">
                                        <a href="<?= e(base_url('modules/siswa/detail.php?id=' . urlencode((string) ($row['student_id'] ?? '')))) ?>" class="text-decoration-none text-dark">
                                            <?= e($row['nama']) ?>
                                        </a>
                                    </td>
                                    <td data-label="Kelas">
                                        <span class="badge bg-light text-dark border"><?= e($row['nama_kelas'] ?? '-') ?></span>
                                    </td>
                                    <td data-label="Guru" class="small"><?=e($row['nama_guru']??'-')?></td>
                                    <td data-label="Mata Pelajaran" class="small"><?=e($row['nama_mata_pelajaran']??'-')?></td>
                                    <td data-label="Les" class="text-center"><?=e($row['lesson_number']??'-')?></td>
                                    <td data-label="Jam Masuk">
                                        <?= $row['jam_masuk'] !== null ? '<span class="fw-semibold text-dark">' . e(strlen((string)$row['jam_masuk']) > 10 ? substr((string) $row['jam_masuk'], 11, 5) : substr((string) $row['jam_masuk'], 0, 5)) . ' WIB</span>' : '<span class="text-muted">-</span>' ?>
                                    </td>
                                    <td data-label="Status">
                                        <span class="badge badge-<?= strtolower((string) $row['status']) === 'alpa' ? 'alfa' : strtolower((string) $row['status']) ?>">
                                            <?= e($row['status'] === 'Alpa' ? 'Alfa' : $row['status']) ?>
                                        </span>
                                    </td>
                                    <td data-label="Keterangan" class="text-secondary small"><?= e($row['keterangan'] ?? '-') ?></td>
                                </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<style>
@media print {
    .no-print, .app-navbar, .app-sidebar, .app-footer, #backToTop {
        display: none !important;
    }
    .main-content, .app-footer {
        margin-left: 0 !important;
        padding-top: 0 !important;
    }
    .area-cetak {
        box-shadow: none !important;
        border: 1px solid #e2e8f0 !important;
    }
}
</style>

<?php require ROOT_PATH . '/include/footer.php'; ?>
