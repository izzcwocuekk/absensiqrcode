<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/siswa/upload.php';

require_role('admin');

$pageTitle = 'Ubah Siswa';
$activeMenu = 'siswa';
$error = '';

$paramId = trim((string) ($_GET['student_id'] ?? $_GET['id'] ?? ''));
$stmt = db()->prepare('SELECT * FROM students WHERE student_id = :sid OR id = :id LIMIT 1');
$stmt->execute([
    ':sid' => $paramId,
    ':id' => is_numeric($paramId) ? (int) $paramId : 0,
]);
$siswa = $stmt->fetch();

if (!$siswa) {
    set_flash_message('danger', 'Data siswa tidak ditemukan.');
    redirect('modules/siswa/index.php');
}

$kelasList = db()->query('SELECT id, nama_kelas FROM classes ORDER BY nama_kelas')->fetchAll();
$old = [
    'nis' => (string) $siswa['nis'],
    'nisn' => (string) ($siswa['nisn'] ?? ''),
    'nama' => (string) $siswa['nama'],
    'class_id' => (string) $siswa['class_id'],
    'jenis_kelamin' => (string) $siswa['jenis_kelamin'],
    'is_active' => (string) ($siswa['is_active'] ?? 1),
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_csrf();
    $old['nis'] = trim((string) ($_POST['nis'] ?? ''));
    $old['nisn'] = trim((string) ($_POST['nisn'] ?? ''));
    $old['nama'] = trim((string) ($_POST['nama'] ?? ''));
    $old['class_id'] = (string) ($_POST['class_id'] ?? '');
    $old['jenis_kelamin'] = (string) ($_POST['jenis_kelamin'] ?? 'L');
    $old['is_active'] = (string) ($_POST['is_active'] ?? '1');

    if ($old['nis'] === '' || $old['nama'] === '' || $old['class_id'] === '' || !in_array($old['jenis_kelamin'], ['L', 'P'], true)) {
        $error = 'NIS, nama lengkap, kelas, dan jenis kelamin wajib diisi dengan benar.';
    } else {
        try {
            $fotoBaru = upload_foto_siswa($old['nis']);
            $fotoFinal = $fotoBaru ?? $siswa['foto'];
            $nisnFinal = $old['nisn'] !== '' ? $old['nisn'] : null;
            $isActive = $old['is_active'] === '1' ? 1 : 0;

            $upd = db()->prepare(
                'UPDATE students SET nis = :nis, nisn = :nisn, nama = :nama, class_id = :class_id,
                 jenis_kelamin = :jk, foto = :foto, is_active = :is_active, updated_at = CURRENT_TIMESTAMP WHERE id = :id'
            );
            $upd->execute([
                ':nis' => $old['nis'],
                ':nisn' => $nisnFinal,
                ':nama' => $old['nama'],
                ':class_id' => (int) $old['class_id'],
                ':jk' => $old['jenis_kelamin'],
                ':foto' => $fotoFinal,
                ':is_active' => $isActive,
                ':id' => $id,
            ]);

            if ($fotoBaru !== null && $fotoBaru !== $siswa['foto']) {
                hapus_foto_siswa(is_string($siswa['foto']) ? $siswa['foto'] : null);
            }

            set_flash_message('success', 'Data siswa "' . $old['nama'] . '" berhasil diperbarui.');
            redirect('modules/siswa/index.php');
        } catch (RuntimeException $e) {
            $error = $e->getMessage();
        } catch (PDOException $e) {
            $error = ((int) $e->getCode() === 23000)
                ? 'NIS sudah dipakai siswa lain.'
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

        <div class="d-flex align-items-center justify-content-between mb-3">
            <h1 class="h3 fw-bold mb-0 text-dark">Ubah Siswa</h1>
            <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/index.php')) ?>">
                <i class="bi bi-arrow-left me-1"></i>Kembali
            </a>
        </div>

        <?php if ($error !== ''): ?>
            <div class="alert alert-danger d-flex align-items-center gap-2 mb-3" role="alert">
                <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
                <div><?= e($error) ?></div>
            </div>
        <?php endif; ?>

        <div class="card border-0 shadow-sm">
            <div class="card-body p-4">
                <form method="post" action="<?= e(base_url('modules/siswa/ubah.php?id=' . $id)) ?>" enctype="multipart/form-data">
                    <?php csrf_field(); ?>
                    <div class="row g-3">
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="nis">NIS (Nomor Induk Siswa) <span class="text-danger">*</span></label>
                            <input class="form-control" id="nis" name="nis" required maxlength="30" value="<?= e($old['nis']) ?>">
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="nisn">NISN (opsional)</label>
                            <input class="form-control" id="nisn" name="nisn" maxlength="30" value="<?= e($old['nisn']) ?>">
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="nama">Nama Lengkap <span class="text-danger">*</span></label>
                            <input class="form-control" id="nama" name="nama" required maxlength="100" value="<?= e($old['nama']) ?>">
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="class_id">Kelas <span class="text-danger">*</span></label>
                            <select class="form-select" id="class_id" name="class_id" required>
                                <?php foreach ($kelasList as $k): ?>
                                    <option value="<?= e((string) $k['id']) ?>" <?= $old['class_id'] === (string) $k['id'] ? 'selected' : '' ?>><?= e($k['nama_kelas']) ?></option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="jenis_kelamin">Jenis Kelamin <span class="text-danger">*</span></label>
                            <select class="form-select" id="jenis_kelamin" name="jenis_kelamin">
                                <option value="L" <?= $old['jenis_kelamin'] === 'L' ? 'selected' : '' ?>>Laki-laki (L)</option>
                                <option value="P" <?= $old['jenis_kelamin'] === 'P' ? 'selected' : '' ?>>Perempuan (P)</option>
                            </select>
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="is_active">Status Siswa</label>
                            <select class="form-select" id="is_active" name="is_active">
                                <option value="1" <?= $old['is_active'] === '1' ? 'selected' : '' ?>>Aktif</option>
                                <option value="0" <?= $old['is_active'] === '0' ? 'selected' : '' ?>>Nonaktif</option>
                            </select>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold text-secondary" for="foto">Foto Profil Siswa</label>
                            <?php if (!empty($siswa['foto'])): ?>
                                <div class="mb-2">
                                    <img src="<?= e(base_url('assets/img/' . basename((string) $siswa['foto']))) ?>"
                                        alt="Foto saat ini" width="60" height="60" class="rounded-circle object-fit-cover border">
                                    <span class="small text-secondary ms-2">Foto saat ini</span>
                                </div>
                            <?php endif; ?>
                            <input class="form-control" type="file" id="foto" name="foto" accept=".jpg,.jpeg,.png">
                            <div class="form-text">Biarkan kosong jika tidak ingin mengubah foto.</div>
                        </div>
                    </div>
                    <div class="mt-4 d-flex gap-2">
                        <button class="btn btn-primary" type="submit"><i class="bi bi-save me-1"></i>Simpan Perubahan</button>
                        <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/index.php')) ?>">Batal</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
