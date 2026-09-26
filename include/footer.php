<footer class="app-footer text-center text-secondary small py-3">
    &copy; <?= e(date('Y')) ?> <?= e(APP_NAME) ?> &middot; Sistem Absensi Siswa QR Code
</footer>

<?php if (!empty($_SESSION['user'])): ?>
<?php
    $footerActiveMenu = $activeMenu ?? '';
    $footerUserRole = $_SESSION['user']['role'] ?? 'guru';
    $footerIsSiswa = ($footerUserRole === 'siswa');
?>
<!-- Mobile Bottom Navigation Bar (< 992px) -->
<nav class="mobile-bottom-nav d-lg-none" aria-label="Navigasi Cepat Mobile">
    <a href="<?= e(base_url('index.php')) ?>" class="bottom-nav-item <?= $footerActiveMenu === 'dashboard' ? 'active' : '' ?>">
        <i class="bi <?= $footerActiveMenu === 'dashboard' ? 'bi-grid-fill' : 'bi-grid' ?>"></i>
        <span>Dashboard</span>
    </a>
    
    <a href="<?= e(base_url('modules/absensi/index.php')) ?>" class="bottom-nav-item <?= $footerActiveMenu === 'absensi' ? 'active' : '' ?>">
        <i class="bi <?= $footerActiveMenu === 'absensi' ? 'bi-calendar2-check-fill' : 'bi-calendar2-check' ?>"></i>
        <span>Riwayat</span>
    </a>

    <?php if (!$footerIsSiswa): ?>
        <a href="<?= e(base_url('modules/scan/index.php')) ?>" class="bottom-nav-item <?= $footerActiveMenu === 'scan' ? 'active' : '' ?>" aria-label="Scan QR Siswa">
            <i class="bi bi-qr-code-scan"></i>
            <span>Scan QR</span>
        </a>

        <a href="<?= e(base_url('modules/siswa/index.php')) ?>" class="bottom-nav-item <?= $footerActiveMenu === 'siswa' ? 'active' : '' ?>">
            <i class="bi <?= $footerActiveMenu === 'siswa' ? 'bi-people-fill' : 'bi-people' ?>"></i>
            <span>Siswa</span>
        </a>
    <?php else: ?>
        <a href="<?= e(base_url('modules/siswa/qr_management.php')) ?>" class="bottom-nav-item <?= $footerActiveMenu === 'qr_siswa' ? 'active' : '' ?>" aria-label="QR Code Saya">
            <i class="bi bi-qr-code"></i>
            <span>QR Saya</span>
        </a>
    <?php endif; ?>

    <button type="button" class="bottom-nav-item border-0 bg-transparent" data-bs-toggle="offcanvas" data-bs-target="#appSidebar" aria-label="Buka Menu Navigasi">
        <i class="bi bi-list fs-5"></i>
        <span>Menu</span>
    </button>
</nav>
<?php endif; ?>

<button id="backToTop" class="btn btn-primary shadow" type="button" aria-label="Kembali ke atas">
    <i class="bi bi-arrow-up"></i>
</button>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<?= e(base_url('assets/js/app.js')) ?>"></script>
</body>
</html>