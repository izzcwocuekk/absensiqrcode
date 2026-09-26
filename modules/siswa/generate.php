<?php
declare(strict_types=1);

/**
 * Fase 06 — Generate massal PNG QR semua siswa (admin only).
 * Membuat file assets/qrcode/generate/{qr_token}.png yang belum ada.
 */

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

require_role('admin');

$students = db()->query('SELECT id, student_id, nis, nama, qr_code, qr_token FROM students ORDER BY student_id ASC')->fetchAll();
$made = 0;
foreach ($students as $s) {
    if (!empty($s['qr_code'])) {
        $path = qrcode_ensure_file((string) $s['qr_code']);
        if (is_file($path)) {
            $made++;
        }
    }
    if (!empty($s['qr_token'])) {
        qrcode_ensure_file((string) $s['qr_token']);
    }
}

set_flash_message('success', 'Generate QR selesai: ' . $made . ' dari ' . count($students) . ' file PNG QR unik siswa (QR-STUxxx) siap di assets/qrcode/generate/.');
redirect('modules/siswa/index.php');
