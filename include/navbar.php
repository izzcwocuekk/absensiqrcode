<nav class="navbar navbar-light fixed-top app-navbar bg-white border-bottom d-lg-none shadow-xs">
    <div class="container-fluid px-3 py-1">
        <button
            class="btn btn-sm btn-light border me-2 d-flex align-items-center justify-content-center"
            style="width: 36px; height: 36px; border-radius: 8px;"
            type="button"
            data-bs-toggle="offcanvas"
            data-bs-target="#appSidebar"
            aria-controls="appSidebar"
            aria-label="Buka menu navigasi"
        >
            <i class="bi bi-list fs-5 text-dark"></i>
        </button>

        <a class="navbar-brand d-flex align-items-center gap-2 mb-0 me-auto text-decoration-none" href="<?= e(base_url('index.php')) ?>">
            <img src="<?= e(base_url('assets/img/logo.png')) ?>" alt="Logo SMK Tritech" width="32" height="32" class="object-fit-contain flex-shrink-0">
            <div class="d-flex flex-column text-start">
                <span class="fw-bold text-dark lh-1" style="font-size: 0.92rem; letter-spacing: -0.01em;">SMK TRITECH</span>
                <span class="text-secondary" style="font-size: 0.68rem; font-weight: 500;">Informatika Medan</span>
            </div>
        </a>

        <div class="d-flex align-items-center gap-2">
            <?php if (($_SESSION['user']['role'] ?? '') !== 'siswa'): ?>
                <a href="<?= e(base_url('modules/scan/index.php')) ?>" class="btn btn-primary btn-sm d-flex align-items-center justify-content-center" style="width: 34px; height: 34px; border-radius: 8px;" title="Scan QR">
                    <i class="bi bi-qr-code-scan"></i>
                </a>
            <?php endif; ?>
            <?php 
                $navbarUser = $_SESSION['user'] ?? null; 
                $navInitial = mb_strtoupper(mb_substr(is_array($navbarUser) ? ($navbarUser['nama'] ?? 'U') : 'U', 0, 1));
            ?>
            <span class="avatar-circle shadow-xs" style="width: 34px; height: 34px; font-size: 0.8rem; font-weight: 700; background-color: var(--app-primary-light); color: var(--app-primary); border: 1px solid var(--app-primary-border);">
                <?= e($navInitial) ?>
            </span>
        </div>
    </div>
</nav>