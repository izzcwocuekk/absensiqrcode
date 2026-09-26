<?php
declare(strict_types=1);

/**
 * Helper upload foto siswa Fase 05.
 * Return: nama file tersimpan (relatif assets/img/) atau null bila tidak ada file.
 * Throw RuntimeException bila validasi gagal.
 */
function upload_foto_siswa(string $nis): ?string
{
    if (!isset($_FILES['foto']) || ($_FILES['foto']['error'] ?? UPLOAD_ERR_NO_FILE) === UPLOAD_ERR_NO_FILE) {
        return null;
    }

    $file = $_FILES['foto'];
    if (($file['error'] ?? UPLOAD_ERR_OK) !== UPLOAD_ERR_OK) {
        throw new RuntimeException('Upload foto gagal (kode ' . (int) $file['error'] . ').');
    }

    if ((int) ($file['size'] ?? 0) > 2 * 1024 * 1024) {
        throw new RuntimeException('Ukuran foto maksimal 2MB.');
    }

    $allowedExt = ['jpg' => 'image/jpeg', 'jpeg' => 'image/jpeg', 'png' => 'image/png'];
    $ext = strtolower((string) pathinfo((string) ($file['name'] ?? ''), PATHINFO_EXTENSION));
    if (!isset($allowedExt[$ext])) {
        throw new RuntimeException('Format foto harus JPG/JPEG/PNG.');
    }

    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mime = (string) $finfo->file((string) $file['tmp_name']);
    if (!in_array($mime, ['image/jpeg', 'image/png'], true)) {
        throw new RuntimeException('Isi file bukan gambar yang valid.');
    }

    $safeNis = preg_replace('/[^A-Za-z0-9_-]/', '', $nis) ?: 'siswa';
    $filename = $safeNis . '-' . time() . '.' . $ext;
    $targetDir = ROOT_PATH . '/assets/img';
    if (!is_dir($targetDir)) {
        mkdir($targetDir, 0755, true);
    }

    if (!move_uploaded_file((string) $file['tmp_name'], $targetDir . '/' . $filename)) {
        throw new RuntimeException('Gagal menyimpan file foto.');
    }

    return $filename;
}

/** Hapus file foto lama (abaikan bila tidak ada). */
function hapus_foto_siswa(?string $filename): void
{
    if ($filename === null || $filename === '') {
        return;
    }
    $path = ROOT_PATH . '/assets/img/' . basename($filename);
    if (is_file($path)) {
        @unlink($path);
    }
}
