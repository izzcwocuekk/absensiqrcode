<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';

require_staff();

$user = current_user();
$isAdmin = ($user['role'] ?? '') === 'admin';
$pageTitle = 'Data Kelas';
$activeMenu = 'kelas';

$rows = db()->query(
    'SELECT c.*, (SELECT COUNT(*) FROM students s WHERE s.class_id = c.id) AS total_siswa
     FROM classes c ORDER BY c.tingkat, c.nama_kelas'
)->fetchAll();

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>

        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
            <div>
                <h1 class="h3 fw-bold mb-1 text-dark">Data Kelas</h1>
                <p class="text-secondary mb-0">Kelola daftar kelas dan rombongan belajar sekolah.</p>
            </div>
            <?php if ($isAdmin): ?>
                <a class="btn btn-primary" href="<?= e(base_url('modules/kelas/tambah.php')) ?>">
                    <i class="bi bi-plus-lg me-1"></i>Tambah Kelas
                </a>
            <?php endif; ?>
        </div>

        <div class="card border-0 shadow-sm">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 table-mobile-card">
                    <thead class="table-light">
                        <tr>
                            <th style="width:3.5rem" class="text-center">No</th>
                            <th>Nama Kelas</th>
                            <th>Jurusan / Peminatan</th>
                            <th>Tingkat</th>
                            <th class="text-center">Jumlah Siswa</th>
                            <?php if ($isAdmin): ?><th style="width:9rem" class="text-center">Aksi</th><?php endif; ?>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if ($rows === []): ?>
                            <tr>
                                <td colspan="<?= $isAdmin ? 6 : 5 ?>" class="empty-state">
                                    <div class="empty-state-icon"><i class="bi bi-building-slash"></i></div>
                                    <h3 class="h6 fw-bold text-dark mb-1">Belum Ada Data Kelas</h3>
                                    <p class="text-secondary small mb-0">Klik tombol Tambah Kelas untuk membuat kelas baru.</p>
                                </td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($rows as $i => $row): ?>
                                <tr>
                                    <td data-label="No" class="text-center text-secondary small"><?= e((string) ($i + 1)) ?></td>
                                    <td data-label="Nama Kelas" class="fw-semibold text-dark"><?= e($row['nama_kelas']) ?></td>
                                    <td data-label="Jurusan" class="text-secondary"><?= e($row['jurusan']) ?></td>
                                    <td data-label="Tingkat">
                                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                                            Kelas <?= e($row['tingkat']) ?>
                                        </span>
                                    </td>
                                    <td data-label="Jumlah Siswa" class="text-center">
                                        <span class="badge bg-light text-dark border">
                                            <i class="bi bi-people me-1 text-primary"></i><?= e((string) $row['total_siswa']) ?> Siswa
                                        </span>
                                    </td>
                                    <?php if ($isAdmin): ?>
                                        <td data-label="Aksi" class="text-center">
                                            <div class="btn-group btn-group-sm" role="group">
                                                <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/kelas/ubah.php?id=' . (int) $row['id'])) ?>" title="Ubah Kelas">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <a class="btn btn-outline-danger" href="<?= e(base_url('modules/kelas/hapus.php?id=' . (int) $row['id'])) ?>" title="Hapus Kelas">
                                                    <i class="bi bi-trash"></i>
                                                </a>
                                            </div>
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
