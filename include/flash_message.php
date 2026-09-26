<?php
declare(strict_types=1);

/** Menyimpan satu pesan singkat untuk request berikutnya. */
function set_flash_message(string $type, string $message): void
{
    $allowedTypes = ['success', 'danger', 'warning', 'info'];
    $safeType = in_array($type, $allowedTypes, true) ? $type : 'info';

    $_SESSION['flash_message'] = [
        'type' => $safeType,
        'message' => $message,
    ];
}

/** Menampilkan lalu menghapus flash message dari session. */
function display_flash_message(): void
{
    if (!isset($_SESSION['flash_message']) || !is_array($_SESSION['flash_message'])) {
        return;
    }

    $flash = $_SESSION['flash_message'];
    unset($_SESSION['flash_message']);

    echo '<div class="alert alert-' . e($flash['type'] ?? 'info')
        . ' alert-dismissible fade show" role="alert">'
        . e($flash['message'] ?? '')
        . '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Tutup"></button>'
        . '</div>';
}