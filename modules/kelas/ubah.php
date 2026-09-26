<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';

require_role('admin');

$pageTitle = 'Ubah Kelas';
$activeMenu = 'kelas';
$error = '';
$allowedTingkat = ['X', 'XI', 'XII'];

$id = (int) ($_GET['id'] ?? 0);
$stmt = db()->prepare('SELECT * FROM classes WHERE id = :id LIMIT 1');
$stmt->execute([':id' => $id]);
$kelas = $stmt->fetch();

if (!$kelas) {
    set_flash_message('danger', 'Data kelas tidak ditemukan.');
    redirect('modules/kelas/index.php');
}

$old = ['nama_kelas' => (string) $kelas['nama_kelas'], 'jurusan' => (string) $kelas['jurusan'], 'tingkat' => (string) $kelas['tingkat']];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_csrf();
    $old['nama_kelas'] = trim((string) ($_POST['nama_kelas'] ?? ''));
    $old['jurusan'] = trim((string) ($_POST['jurusan'] ?? ''));
    $old['tingkat'] = (string) ($_POST['tingkat'] ?? 'X');

    if ($old['nama_kelas'] === '' || $old['jurusan'] === '' || !in_array($old['tingkat'], $allowedTingkat, true)) {
        $error = 'Nama kelas, jurusan, dan tingkat (X/XI/XII) wajib diisi dengan benar.';
    } else {
        try {
            $upd = db()->prepare('UPDATE classes SET nama_kelas = :nama, jurusan = :jurusan, tingkat = :tingkat WHERE id = :id');
            $upd->execute([':nama' => $old['nama_kelas'], ':jurusan' => $old['jurusan'], ':tingkat' => $old['tingkat'], ':id' => $id]);
            set_flash_message('success', 'Kelas berhasil diubah.');
            redirect('modules/kelas/index.php');
        } catch (PDOException $e) {
            $error = ((int) $e->getCode() === 23000)
                ? 'Nama kelas sudah dipakai kelas lain.'
                : 'Gagal menyimpan: ' . $e->getMessage();
        }
    }
}

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>
        <h1 class="h3 mb-3">Ubah Kelas</h1>
        <?php if ($error !== ''): ?><div class="alert alert-danger"><?= e($error) ?></div><?php endif; ?>
        <div class="card border-0 shadow-sm">
            <div class="card-body">
                <form method="post" action="<?= e(base_url('modules/kelas/ubah.php?id=' . $id)) ?>">
                    <?php csrf_field(); ?>
                    <div class="mb-3">
                        <label class="form-label" for="nama_kelas">Nama Kelas</label>
                        <input class="form-control" id="nama_kelas" name="nama_kelas" required maxlength="50" value="<?= e($old['nama_kelas']) ?>">
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="jurusan">Jurusan</label>
                        <input class="form-control" id="jurusan" name="jurusan" required maxlength="100" value="<?= e($old['jurusan']) ?>">
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="tingkat">Tingkat</label>
                        <select class="form-select" id="tingkat" name="tingkat">
                            <?php foreach ($allowedTingkat as $t): ?>
                                <option value="<?= e($t) ?>" <?= $old['tingkat'] === $t ? 'selected' : '' ?>><?= e($t) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                    <button class="btn btn-primary" type="submit"><i class="bi bi-save me-1"></i>Simpan</button>
                    <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/kelas/index.php')) ?>">Batal</a>
                </form>
            </div>
        </div>
    </div>
</main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
