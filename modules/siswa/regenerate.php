<?php
declare(strict_types=1);

/**
 * Regenerate QR Token untuk siswa tertentu (Admin only).
 * Menghasilkan token acak 32-karakter unik baru dan menghapus file QR lama.
 */

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

require_role('admin');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('modules/siswa/index.php');
}

require_csrf();

$idRaw = trim((string) ($_POST['id'] ?? ''));
if ($idRaw === '') {
    set_flash_message('danger', 'ID siswa tidak valid.');
    redirect('modules/siswa/index.php');
}

$stmt = db()->prepare('SELECT id, student_id, nis, nama, qr_token FROM students WHERE student_id = :sid OR id = :id LIMIT 1');
$stmt->execute([':sid' => $idRaw, ':id' => is_numeric($idRaw) ? (int) $idRaw : 0]);
$siswa = $stmt->fetch();

if (!$siswa) {
    set_flash_message('danger', 'Data siswa tidak ditemukan.');
    redirect('modules/siswa/index.php');
}

try {
    $oldToken = (string) $siswa['qr_token'];
    $newToken = bin2hex(random_bytes(16));

    // Update token di database
    $upd = db()->prepare('UPDATE students SET qr_token = :qr, updated_at = CURRENT_TIMESTAMP WHERE id = :id');
    $upd->execute([':qr' => $newToken, ':id' => (int) $siswa['id']]);

    // Hapus file QR lama jika ada
    if ($oldToken !== '') {
        $oldFile = ROOT_PATH . '/assets/qrcode/generate/' . preg_replace('/[^A-Za-z0-9]/', '', $oldToken) . '.png';
        if (is_file($oldFile)) {
            @unlink($oldFile);
        }
    }

    // Pastikan file QR baru dibuat
    qrcode_ensure_file($newToken);

    set_flash_message('success', 'QR Code siswa "' . $siswa['nama'] . '" (NIS: ' . $siswa['nis'] . ') berhasil diperbarui.');
} catch (PDOException $e) {
    set_flash_message('danger', 'Gagal memperbarui QR Token: ' . $e->getMessage());
} catch (Throwable $e) {
    set_flash_message('danger', 'Terjadi kesalahan sistem saat generate QR baru.');
}

redirect('modules/siswa/index.php');
