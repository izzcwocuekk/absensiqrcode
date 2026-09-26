<?php
declare(strict_types=1);

/**
 * Penjaga autentikasi & otorisasi berbasis peran (Role-based access control).
 */

function current_user(): ?array
{
    if (!isset($_SESSION['user']) || !is_array($_SESSION['user'])) {
        return null;
    }

    return $_SESSION['user'];
}

/** Wajib login, jika belum lempar ke halaman login. */
function require_login(): void
{
    if (current_user() === null) {
        set_flash_message('warning', 'Silakan login terlebih dahulu.');
        redirect('modules/auth/login.php');
    }
}

/** Wajib role tertentu (mis. admin). */
function require_role(string $role): void
{
    require_login();

    $user = current_user();
    if (($user['role'] ?? '') !== $role) {
        set_flash_message('danger', 'Akses ditolak untuk peran Anda.');
        redirect('index.php');
    }
}

/** Wajib staf (admin atau guru), menolak akses siswa. */
function require_staff(): void
{
    require_login();

    $user = current_user();
    $role = $user['role'] ?? '';
    if ($role !== 'admin' && $role !== 'guru') {
        set_flash_message('danger', 'Akses ditolak untuk peran Anda.');
        redirect('index.php');
    }
}
