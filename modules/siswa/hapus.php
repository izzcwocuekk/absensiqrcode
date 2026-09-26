<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/siswa/upload.php';

require_role('admin');

$pageTitle = 'Hapus Siswa';
$activeMenu = 'siswa';

$paramId = trim((string) ($_GET['student_id'] ?? $_GET['id'] ?? ''));
$stmt = db()->prepare('SELECT s.*, c.nama_kelas FROM students s LEFT JOIN classes c ON c.id = s.class_id WHERE s.student_id = :sid OR s.id = :id LIMIT 1');
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
$cnt = db()->prepare('SELECT COUNT(*) AS c FROM attendances WHERE student_id = :sid');
$cnt->execute([':sid' => $studentId]);
$totalAbsensi = (int) ($cnt->fetch()['c'] ?? 0);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_csrf();
    // Riwayat absensi, qr_codes, dan relasi users ikut terhapus via FK CASCADE/SET NULL
    db()->prepare('DELETE FROM students WHERE student_id = :sid OR id = :id')->execute([
        ':sid' => $studentId,
        ':id' => (int) $siswa['id'],
    ]);
    hapus_foto_siswa(is_string($siswa['foto']) ? $siswa['foto'] : null);
    set_flash_message('success', 'Siswa "' . $siswa['nama'] . '" (' . $studentId . ') dihapus (termasuk ' . $totalAbsensi . ' riwayat absensi).');
    redirect('modules/siswa/index.php');
}

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>
        <h1 class="h3 mb-3">Hapus Siswa</h1>
        <div class="card border-0 shadow-sm">
            <div class="card-body">
                <p>Yakin hapus <strong><?= e($siswa['nama']) ?></strong>
                    (NIS <?= e($siswa['nis']) ?>, kelas <?= e($siswa['nama_kelas'] ?? '-') ?>)?</p>
                <?php if ($totalAbsensi > 0): ?>
                    <div class="alert alert-warning">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        Siswa ini memiliki <strong><?= e((string) $totalAbsensi) ?> riwayat absensi</strong>
                        yang ikut terhapus (aturan FK CASCADE).
                    </div>
                <?php endif; ?>
                <form method="post" action="<?= e(base_url('modules/siswa/hapus.php?id=' . $id)) ?>">
                    <?php csrf_field(); ?>
                    <button class="btn btn-danger" type="submit"><i class="bi bi-trash me-1"></i>Ya, hapus</button>
                    <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/index.php')) ?>">Batal</a>
                </form>
            </div>
        </div>
<?php require ROOT_PATH . '/include/footer.php'; ?>
