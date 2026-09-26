<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';

if (isset($_SESSION['user']) && is_array($_SESSION['user'])) {
    redirect('index.php');
}

$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_csrf();
    $username = trim((string) ($_POST['username'] ?? ''));
    $password = (string) ($_POST['password'] ?? '');

    if ($username === '' || $password === '') {
        $error = 'Username dan password wajib diisi.';
    } else {
        try {
            $stmt = db()->prepare('SELECT id, username, password AS password_hash, nama, role, student_id FROM users WHERE username = :username LIMIT 1');
            $stmt->execute([':username' => $username]);
            $user = $stmt->fetch();

            if ($user && password_verify($password, (string) $user['password_hash'])) {
                session_regenerate_id(true);
                $_SESSION['user'] = [
                    'id' => (int) $user['id'],
                    'username' => (string) $user['username'],
                    'nama' => (string) $user['nama'],
                    'role' => (string) $user['role'],
                    'student_id' => !empty($user['student_id']) ? (string) $user['student_id'] : null,
                ];
                set_flash_message('success', 'Selamat datang kembali, ' . $_SESSION['user']['nama'] . '!');
                redirect('index.php');
            }

            $error = 'Username atau password yang Anda masukkan salah.';
        } catch (PDOException $e) {
            $error = 'Database belum siap. Import database/schema.sql terlebih dahulu.';
        }
    }
}

$pageTitle = 'Login Sistem Absensi';
?>
<!doctype html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= e(page_title($pageTitle)) ?></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="<?= e(base_url('assets/css/style.css')) ?>" rel="stylesheet">
</head>
<body class="bg-light d-flex align-items-center justify-content-center" style="min-height: 100vh;">
<main class="container py-4">
    <div class="row justify-content-center">
        <div class="col-12 col-sm-10 col-md-6 col-lg-4">
            <!-- Brand & Logo -->
            <div class="text-center mb-4">
                <img src="<?= e(base_url('assets/img/logo.png')) ?>" alt="Logo SMK Tritech" width="84" height="84" class="object-fit-contain mb-2">
                <h1 class="h4 fw-bold text-dark mb-1">SMK TRITECH INFORMATIKA</h1>
                <p class="text-secondary small mb-0">Sistem Presensi Siswa Berbasis QR Code</p>
            </div>

            <?php display_flash_message(); ?>
            <?php if ($error !== ''): ?>
                <div class="alert alert-danger d-flex align-items-center gap-2 mb-3" role="alert">
                    <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
                    <div class="small"><?= e($error) ?></div>
                </div>
            <?php endif; ?>

            <!-- Card Login -->
            <div class="card border-0 shadow-sm">
                <div class="card-body p-4">
                    <form method="post" action="<?= e(base_url('modules/auth/login.php')) ?>">
                        <?php csrf_field(); ?>

                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary" for="username">Username</label>
                            <div class="input-group">
                                <span class="input-group-text bg-light text-secondary"><i class="bi bi-person"></i></span>
                                <input class="form-control" type="text" id="username" name="username" required autofocus
                                    placeholder="Masukkan username" value="<?= e((string) ($_POST['username'] ?? '')) ?>" autocomplete="username">
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label small fw-semibold text-secondary" for="password">Password</label>
                            <div class="input-group">
                                <span class="input-group-text bg-light text-secondary"><i class="bi bi-lock"></i></span>
                                <input class="form-control" type="password" id="password" name="password" required
                                    placeholder="Masukkan password" autocomplete="current-password">
                                <button class="btn btn-outline-secondary" type="button" id="btnTogglePassword" aria-label="Lihat Password">
                                    <i class="bi bi-eye"></i>
                                </button>
                            </div>
                        </div>

                        <button class="btn btn-primary w-100 py-2 fw-semibold" type="submit">
                            <i class="bi bi-box-arrow-in-right me-1"></i>Masuk ke Sistem
                        </button>
                    </form>
                </div>
            </div>

            <!-- Demo Hint -->
            <div class="card border border-light-subtle shadow-none bg-light-subtle mt-3">
                <div class="card-body p-3 text-center">
                    <span class="text-muted small d-block mb-2">Akun Akses Cepat Demo:</span>
                    <div class="d-flex flex-wrap justify-content-center gap-2">
                        <button type="button" class="btn btn-sm btn-outline-primary py-0 px-2 small" onclick="fillLogin('admin', 'admin123')">
                            Admin (admin)
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-secondary py-0 px-2 small" onclick="fillLogin('guru', 'guru123')">
                            Guru Piket (guru)
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-success py-0 px-2 small" onclick="fillLogin('faiz.rpl1', 'faiz123')">
                            Faiz - Absen 9 (faiz.rpl1)
                        </button>
                    </div>
                </div>
            </div>

            <p class="text-center text-secondary small mt-3 mb-0">
                &copy; <?= e(date('Y')) ?> <?= e(APP_NAME) ?>
            </p>
        </div>
    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
'use strict';
function fillLogin(user, pass) {
    document.getElementById('username').value = user;
    document.getElementById('password').value = pass;
}

const toggleBtn = document.getElementById('btnTogglePassword');
const passInput = document.getElementById('password');
if (toggleBtn && passInput) {
    toggleBtn.addEventListener('click', () => {
        const isPass = passInput.type === 'password';
        passInput.type = isPass ? 'text' : 'password';
        toggleBtn.innerHTML = isPass ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
    });
}
</script>
</body>
</html>
