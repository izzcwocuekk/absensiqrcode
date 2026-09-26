<?php
declare(strict_types=1);

$activeMenu = $activeMenu ?? '';
$user = current_user() ?? ($_SESSION['user'] ?? []);
$userRole = (string) ($user['role'] ?? 'guru');
$isAdmin = $userRole === 'admin';
$isGuru = $userRole === 'guru';
$isSiswa = $userRole === 'siswa';

$roleLabel = match ($userRole) {
    'admin' => 'Administrator',
    'guru' => 'Guru Pengajar',
    'siswa' => 'Siswa',
    default => ucfirst($userRole),
};

$userName = (string) ($user['nama'] ?? ($user['username'] ?? 'User'));
$userInitial = mb_strtoupper(mb_substr($userName, 0, 1));
?>
<aside class="offcanvas-lg offcanvas-start app-sidebar" tabindex="-1" id="appSidebar" aria-labelledby="sidebarTitle">
    <!-- Mobile Header inside Drawer -->
    <div class="offcanvas-header border-bottom d-lg-none py-3 px-3">
        <div class="d-flex align-items-center gap-2">
            <img src="<?= e(base_url('assets/img/logo.png')) ?>" alt="Logo SMK Tritech" width="34" height="34" class="object-fit-contain flex-shrink-0">
            <div>
                <h2 class="h6 fw-bold text-dark mb-0" id="sidebarTitle">SMK TRITECH</h2>
                <small class="text-secondary" style="font-size: 0.72rem;">Informatika Medan</small>
            </div>
        </div>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" data-bs-target="#appSidebar" aria-label="Tutup"></button>
    </div>

    <div class="offcanvas-body d-flex flex-column h-100 p-3">
        <!-- Brand Header (Desktop) -->
        <div class="d-none d-lg-flex align-items-center gap-2 mb-4 px-2 py-1">
            <img src="<?= e(base_url('assets/img/logo.png')) ?>" alt="Logo SMK Tritech" width="38" height="38" class="object-fit-contain flex-shrink-0">
            <div>
                <h2 class="sidebar-brand-title mb-0">SMK TRITECH</h2>
                <span class="sidebar-brand-sub">Informatika Medan</span>
            </div>
        </div>

        <!-- Menu Utama -->
        <nav class="nav nav-pills flex-column gap-1" aria-label="Navigasi Utama">
            <a class="nav-link <?= is_menu_active('dashboard', $activeMenu) ? 'active' : '' ?>" href="<?= e(base_url('index.php')) ?>" <?= is_menu_active('dashboard', $activeMenu) ? 'aria-current="page"' : '' ?>>
                <i class="bi bi-grid-1x2"></i>
                <span>Dashboard</span>
            </a>

            <?php if (!$isSiswa): ?>
                <a class="nav-link <?= is_menu_active('scan', $activeMenu) ? 'active' : '' ?>" href="<?= e(base_url('modules/scan/index.php')) ?>">
                    <i class="bi bi-qr-code-scan"></i>
                    <span>Absensi</span>
                </a>

                <a class="nav-link <?= is_menu_active('siswa', $activeMenu) ? 'active' : '' ?>" href="<?= e(base_url('modules/siswa/index.php')) ?>">
                    <i class="bi bi-people"></i>
                    <span>Data Siswa</span>
                </a>
            <?php endif; ?>

            <a class="nav-link <?= is_menu_active('qr_siswa', $activeMenu) ? 'active' : '' ?>" href="<?= e(base_url('modules/siswa/qr_management.php')) ?>">
                <i class="bi bi-qr-code"></i>
                <span>QR Siswa</span>
            </a>

            <a class="nav-link <?= is_menu_active('absensi', $activeMenu) ? 'active' : '' ?>" href="<?= e(base_url('modules/absensi/index.php')) ?>">
                <i class="bi bi-calendar-check"></i>
                <span>Riwayat Absensi</span>
            </a>

            <?php if (!$isSiswa): ?>
                <a class="nav-link <?= is_menu_active('laporan', $activeMenu) ? 'active' : '' ?>" href="<?= e(base_url('modules/laporan/index.php')) ?>">
                    <i class="bi bi-file-earmark-bar-graph"></i>
                    <span>Laporan</span>
                </a>
            <?php endif; ?>

            <!-- Separator -->
            <hr class="my-2 border-secondary-subtle">

            <?php if ($isAdmin): ?>
                <a class="nav-link <?= is_menu_active('pengaturan', $activeMenu) ? 'active' : '' ?>" href="<?= e(base_url('modules/pengaturan/index.php')) ?>">
                    <i class="bi bi-gear"></i>
                    <span>Pengaturan</span>
                </a>
            <?php endif; ?>
        </nav>

        <!-- Bottom User Card Sesuai Instruksi:
             avatar/inisial, nama user (FAIZ), role (Administrator) -->
        <div class="sidebar-user-card pt-3 border-top mt-auto">
            <div class="dropdown">
                <button class="btn w-100 p-2 d-flex align-items-center justify-content-between text-start border-0 bg-transparent rounded-3 sidebar-user-btn" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <div class="d-flex align-items-center gap-2 overflow-hidden">
                        <div class="sidebar-user-avatar">
                            <?= e($userInitial) ?>
                        </div>
                        <div class="overflow-hidden">
                            <p class="sidebar-user-name mb-0 text-truncate text-uppercase"><?= e($userName) ?></p>
                            <span class="sidebar-user-role"><?= e($roleLabel) ?></span>
                        </div>
                    </div>
                    <i class="bi bi-chevron-down text-secondary small ms-1"></i>
                </button>
                <ul class="dropdown-menu dropdown-menu-end shadow-sm border w-100 mb-1">
                    <li><span class="dropdown-header small text-secondary">Akun: <?= e($user['username'] ?? '') ?></span></li>
                    <?php if ($isSiswa): ?>
                        <li><a class="dropdown-item small" href="<?= e(base_url('modules/siswa/qr_management.php')) ?>"><i class="bi bi-qr-code me-2"></i>QR Code Saya</a></li>
                    <?php endif; ?>
                    <li><hr class="dropdown-divider"></li>
                    <li>
                        <a class="dropdown-item text-danger small fw-semibold" href="<?= e(base_url('modules/auth/logout.php')) ?>">
                            <i class="bi bi-box-arrow-right me-2"></i>Keluar Sistem
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</aside>