<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';

require_login();

// Hanya guru dan admin yang boleh mengubah status aktif/nonaktif
$user = current_user();
if (($user['role'] ?? '') === 'siswa') {
    set_flash_message('danger', 'Akses ditolak.');
    redirect('index.php');
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('modules/siswa/index.php');
}

require_csrf();

$id = (int) ($_POST['id'] ?? 0);
if ($id <= 0) {
    set_flash_message('danger', 'ID siswa tidak valid.');
    redirect('modules/siswa/index.php');
}

$stmt = db()->prepare('SELECT id, nama, is_active FROM students WHERE id = :id LIMIT 1');
$stmt->execute([':id' => $id]);
$siswa = $stmt->fetch();

if (!$siswa) {
    set_flash_message('danger', 'Data siswa tidak ditemukan.');
    redirect('modules/siswa/index.php');
}

$newStatus = ((int) ($siswa['is_active'] ?? 1) === 1) ? 0 : 1;
$statusLabel = $newStatus === 1 ? 'diaktifkan' : 'dinonaktifkan';

try {
    $upd = db()->prepare('UPDATE students SET is_active = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :id');
    $upd->execute([':status' => $newStatus, ':id' => $id]);

    set_flash_message(
        $newStatus === 1 ? 'success' : 'warning',
        'Status siswa "' . $siswa['nama'] . '" berhasil ' . $statusLabel . '.'
    );
} catch (PDOException $e) {
    set_flash_message('danger', 'Gagal memperbarui status siswa: ' . $e->getMessage());
}

$referer = $_SERVER['HTTP_REFERER'] ?? '';
if (str_contains($referer, 'modules/siswa/detail.php')) {
    redirect('modules/siswa/detail.php?id=' . $id);
}
redirect('modules/siswa/index.php');
