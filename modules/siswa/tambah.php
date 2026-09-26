<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/siswa/upload.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

require_role('admin');

$pageTitle = 'Tambah Siswa';
$activeMenu = 'siswa';
$error = '';
$old = ['nis' => '', 'nisn' => '', 'nama' => '', 'class_id' => '', 'jenis_kelamin' => 'L', 'is_active' => '1'];
$kelasList = db()->query('SELECT id, nama_kelas FROM classes ORDER BY nama_kelas')->fetchAll();

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
            $foto = upload_foto_siswa($old['nis']);
            
            // Auto generate student_id unik berikutnya (misal STU026)
            $lastStu = db()->query("SELECT student_id FROM students WHERE student_id LIKE 'STU%' ORDER BY student_id DESC LIMIT 1")->fetchColumn();
            $nextNum = 1;
            if ($lastStu && preg_match('/^STU(\d+)$/', (string) $lastStu, $m)) {
                $nextNum = ((int) $m[1]) + 1;
            }
            $studentId = sprintf('STU%03d', $nextNum);
            $qrCode = 'QR-' . $studentId;
            $qrToken = bin2hex(random_bytes(16));
            $nisnFinal = $old['nisn'] !== '' ? $old['nisn'] : null;
            $isActive = $old['is_active'] === '1' ? 1 : 0;

            $stmt = db()->prepare(
                'INSERT INTO students (student_id, nis, nisn, nama, class_id, jenis_kelamin, qr_token, qr_code, foto, is_active)
                 VALUES (:sid, :nis, :nisn, :nama, :class_id, :jk, :qr, :qrcode, :foto, :is_active)'
            );
            $stmt->execute([
                ':sid' => $studentId,
                ':nis' => $old['nis'],
                ':nisn' => $nisnFinal,
                ':nama' => $old['nama'],
                ':class_id' => (int) $old['class_id'],
                ':jk' => $old['jenis_kelamin'],
                ':qr' => $qrToken,
                ':qrcode' => $qrCode,
                ':foto' => $foto,
                ':is_active' => $isActive,
            ]);

            // Buat file PNG QR Code & catat di qr_codes
            $filePath = qrcode_ensure_file($qrCode);
            qrcode_ensure_file($qrToken);
            $insQr = db()->prepare('INSERT INTO qr_codes (student_id, qr_code, file_path) VALUES (:sid, :code, :path)');
            $insQr->execute([':sid' => $studentId, ':code' => $qrCode, ':path' => $filePath]);

            // Buat akun login siswa
            $usernameBase = strtolower(preg_replace('/[^a-zA-Z0-9]/', '', explode(' ', $old['nama'])[0]));
            if ($usernameBase === '') {
                $usernameBase = 'siswa' . $nextNum;
            }
            $username = $usernameBase . '.rpl1';
            $uCheck = db()->prepare("SELECT COUNT(*) FROM users WHERE username = :u");
            $uCheck->execute([':u' => $username]);
            if ((int) $uCheck->fetchColumn() > 0) {
                $username = $usernameBase . $nextNum . '.rpl1';
            }
            $rawPass = $usernameBase . '123';
            $insUser = db()->prepare("INSERT INTO users (nama, username, email, password, role, student_id) VALUES (:nama, :u, :e, :p, 'siswa', :sid)");
            $insUser->execute([
                ':nama' => $old['nama'],
                ':u' => $username,
                ':e' => $username . '@siswa.tritech.sch.id',
                ':p' => password_hash($rawPass, PASSWORD_DEFAULT),
                ':sid' => $studentId,
            ]);

            set_flash_message('success', 'Siswa [' . $studentId . '] "' . $old['nama'] . '" berhasil ditambahkan dengan username: ' . $username . '.');
            redirect('modules/siswa/index.php');
        } catch (RuntimeException $e) {
            $error = $e->getMessage();
        } catch (PDOException $e) {
            $error = ((int) $e->getCode() === 23000)
                ? 'NIS sudah dipakai oleh siswa lain. Gunakan NIS unik.'
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
            <h1 class="h3 fw-bold mb-0 text-dark">Tambah Siswa</h1>
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
                <form method="post" action="<?= e(base_url('modules/siswa/tambah.php')) ?>" enctype="multipart/form-data">
                    <?php csrf_field(); ?>
                    <div class="row g-3">
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="nis">NIS (Nomor Induk Siswa) <span class="text-danger">*</span></label>
                            <input class="form-control" id="nis" name="nis" required maxlength="30" placeholder="cth: R.0400.26" value="<?= e($old['nis']) ?>">
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="nisn">NISN (opsional)</label>
                            <input class="form-control" id="nisn" name="nisn" maxlength="30" placeholder="cth: 0116250064" value="<?= e($old['nisn']) ?>">
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="nama">Nama Lengkap <span class="text-danger">*</span></label>
                            <input class="form-control" id="nama" name="nama" required maxlength="100" placeholder="Nama lengkap siswa" value="<?= e($old['nama']) ?>">
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold text-secondary" for="class_id">Kelas <span class="text-danger">*</span></label>
                            <select class="form-select" id="class_id" name="class_id" required>
                                <option value="">-- Pilih Kelas --</option>
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
                            <label class="form-label small fw-semibold text-secondary" for="foto">Foto Profil Siswa (opsional, JPG/PNG maks 2MB)</label>
                            <input class="form-control" type="file" id="foto" name="foto" accept=".jpg,.jpeg,.png">
                        </div>
                    </div>
                    <div class="mt-4 d-flex gap-2">
                        <button class="btn btn-primary" type="submit"><i class="bi bi-save me-1"></i>Simpan Siswa</button>
                        <a class="btn btn-outline-secondary" href="<?= e(base_url('modules/siswa/index.php')) ?>">Batal</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
