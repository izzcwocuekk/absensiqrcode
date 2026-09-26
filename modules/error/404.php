<?php
declare(strict_types=1);

/**
 * Halaman 404 sederhana (Fase 09) — Bahasa Indonesia, layout aplikasi.
 * Dipakai bila modul menerima id yang tidak ada (sebagai fallback aman).
 */

require_once __DIR__ . '/../../config/config.php';

http_response_code(404);
$pageTitle = 'Halaman Tidak Ditemukan';
$activeMenu = '';

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4 text-center py-5">
        <p class="display-1 fw-bold text-primary mb-0">404</p>
        <h1 class="h4 mb-2">Halaman tidak ditemukan</h1>
        <p class="text-secondary mb-4">Alamat yang Anda tuju salah atau data sudah dihapus.</p>
        <a class="btn btn-primary" href="<?= e(base_url('index.php')) ?>">
            <i class="bi bi-house me-1"></i>Kembali ke Dashboard
        </a>
    </div>
</main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
