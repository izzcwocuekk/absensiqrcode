<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';

require_role('admin');

$pageTitle = 'Hapus Kelas';
$activeMenu = 'kelas';

$id = (int) ($_GET['id'] ?? 0);
$stmt = db()->prepare('SELECT * FROM classes WHERE id = :id LIMIT 1');
$stmt->execute([':id' => $id]);
$kelas = $stmt->fetch();

if (!$kelas) {
    set_flash_message('danger', 'Data kelas tidak ditemukan.');
    redirect('modules/kelas/index.php');
}

$cnt = db()->prepare('SELECT COUNT(*) AS c FROM students WHERE class_id = :id');
$cnt->execute([':id' => $id]);
$totalSiswa = (int) ($cnt->fetch()['c'] ?? 0);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_csrf();
    if ($totalSiswa > 0) {
        set_flash_message('danger', 'Kelas tidak bisa dihapus karena masih memiliki ' . $totalSiswa . ' siswa.');
        redirect('modules/kelas/index.php');
    }
    try {
        db()->prepare('DELETE FROM classes WHERE id = :id')->execute([':id' => $id]);
        set_flash_message('success', 'Kelas "' . $kelas['nama_kelas'] . '" berhasil dihapus.');
    } catch (PDOException $e) {
        set_flash_message('danger', 'Gagal menghapus: masih ada data terkait.');
    }
    redirect('modules/kelas/index.php');
}

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>
        <h1 class="h3 mb-3">Hapus Kelas</h1>
        <div class="card border-0 shadow-sm">
            <div class="card-body">
                <?php if ($totalSiswa > 0): ?>
                    <div class="alert alert-warning mb-0">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        Kelas <strong><?= e($kelas['nama_kelas']) ?></strong> masih memiliki
                        <strong><?= e((string) $totalSiswa) ?> siswa</strong>, jadi tidak bisa dihapus.
                        Pindahkan dulu siswanya ke kelas lain.
                    </div>
                    <a class="btn btn-outline-secondary mt-3" href="<?= e(base_url('modules/kelas/index.php')) ?>">Kembali</a>
                <?php else: ?>
                    <p>Yakin hapus kelas <strong><?= e($kelas['nama_kelas']) ?></strong>
                        (<?= e($kelas['jurusan']) ?>, tingkat <?= e($kelas['tingkat']) ?>)? Tindakan ini tidak bisa dibatalkan.</p>
                    <form method="post" action="<?= e(base_url('modules/kelas/hapus.php?id=' . $id)) ?>">
                        <?php csrf_field(); ?>
                        <button class="btn btn-danger" type="submit"><i class="bi bi-trash me-1"></i>Ya, hapus</button>
                        <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/kelas/index.php')) ?>">Batal</a>
                    </form>
                <?php endif; ?>
            </div>
        </div>
<?php require ROOT_PATH . '/include/footer.php'; ?>
