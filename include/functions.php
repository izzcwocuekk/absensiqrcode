<?php
declare(strict_types=1);

/** Mengamankan teks sebelum ditampilkan ke HTML. */
function e(mixed $value): string
{
    return htmlspecialchars((string) ($value ?? ''), ENT_QUOTES, 'UTF-8');
}

/** Membuat URL absolut terhadap folder aplikasi. */
function base_url(string $path = ''): string
{
    $base = rtrim(BASE_URL, '/');

    return $path === '' ? $base : $base . '/' . ltrim($path, '/');
}

/** Mengarahkan browser ke halaman lain di dalam aplikasi. */
function redirect(string $path): never
{
    header('Location: ' . base_url($path));
    exit;
}

/** Token CSRF sederhana untuk form POST (Fase 09). */
function csrf_token(): string
{
    if (empty($_SESSION['csrf_token']) || !is_string($_SESSION['csrf_token'])) {
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    }

    return $_SESSION['csrf_token'];
}

/** Cetak input hidden CSRF di dalam <form method="post">. */
function csrf_field(): void
{
    echo '<input type="hidden" name="_csrf" value="' . e(csrf_token()) . '">';
}

/** Validasi token CSRF request POST; berhenti 419 bila tidak cocok. */
function require_csrf(): void
{
    $kirim = (string) ($_POST['_csrf'] ?? $_SERVER['HTTP_X_CSRF_TOKEN'] ?? '');
    $simpan = (string) ($_SESSION['csrf_token'] ?? '');

    if ($kirim === '' || $simpan === '' || !hash_equals($simpan, $kirim)) {
        if (
            (!empty($_SERVER['HTTP_ACCEPT']) && str_contains($_SERVER['HTTP_ACCEPT'], 'application/json')) ||
            (!empty($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) === 'xmlhttprequest')
        ) {
            http_response_code(419);
            header('Content-Type: application/json; charset=utf-8');
            echo json_encode(['success' => false, 'pesan' => 'Sesi atau token keamanan kedaluwarsa. Silakan muat ulang halaman.']);
            exit;
        }
        http_response_code(419);
        exit('Token keamanan kedaluwarsa. Kembali dan coba lagi.');
    }
}

/** Menyusun judul tab browser secara konsisten. */
function page_title(?string $title = null): string
{
    return $title === null || $title === ''
        ? APP_NAME
        : $title . ' - ' . APP_NAME;
}

/** Menentukan menu aktif tanpa memerlukan router atau framework. */
function is_menu_active(string $menu, string $activeMenu): bool
{
    return $menu === $activeMenu;
}

/** Mengambil konfigurasi jam masuk & batas terlambat dari database. */
function get_attendance_settings(): array
{
    static $settings = null;
    if ($settings !== null) {
        return $settings;
    }

    try {
        if (function_exists('db')) {
            $row = db()->query('SELECT * FROM attendance_settings ORDER BY id ASC LIMIT 1')->fetch();
            if ($row) {
                $settings = $row;
                return $settings;
            }
        }
    } catch (Throwable $e) {
        // Fallback default jika tabel belum siap
    }

    $settings = [
        'id' => 1,
        'school_name' => 'SMK TRITECH INFORMATIKA MEDAN',
        'school_start_time' => '07:00:00',
        'late_after' => '07:00:00',
        'school_address' => 'Jl. Bhayangkara No. 434, Medan',
    ];

    return $settings;
}