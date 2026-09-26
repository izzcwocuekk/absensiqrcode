<?php
declare(strict_types=1);

/**
 * Endpoint penerima data scan QR dan input manual absensi.
 * Mendukung respon JSON (AJAX) untuk scanner interaktif tanpa reload
 * dan fallback POST standar untuk kompatibilitas form biasa.
 */

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/scan/fungsi_scan.php';

require_login();

$isAjax = (!empty($_SERVER['HTTP_ACCEPT']) && str_contains($_SERVER['HTTP_ACCEPT'], 'application/json')) ||
          (!empty($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) === 'xmlhttprequest');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    if ($isAjax) {
        http_response_code(405);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['success' => false, 'pesan' => 'Metode request tidak diizinkan.']);
        exit;
    }
    redirect('modules/scan/index.php');
}

require_csrf();

$kode = trim((string) ($_POST['kode'] ?? ''));
$today = date('Y-m-d');
$now = date('H:i:s');

try {
    $hasil = catat_scan(db(), $kode, $today, $now);

    if ($isAjax) {
        header('Content-Type: application/json; charset=utf-8');
        $s = $hasil['siswa'] ?? null;
        $fotoUrl = (!empty($s['foto']) && is_file(ROOT_PATH . '/assets/img/' . basename((string) $s['foto'])))
            ? base_url('assets/img/' . basename((string) $s['foto']))
            : null;

        echo json_encode([
            'success' => $hasil['ok'],
            'code' => $hasil['code'] ?? ($hasil['ok'] ? 'SUCCESS' : 'ERROR'),
            'pesan' => $hasil['pesan'],
            'data' => [
                'student_id' => is_array($s) ? ($s['student_id'] ?? '-') : '-',
                'nama' => is_array($s) ? ($s['nama'] ?? '-') : '-',
                'nis' => is_array($s) ? ($s['nis'] ?? '-') : $kode,
                'kelas' => is_array($s) ? ($s['nama_kelas'] ?? '-') : '-',
                'qr_code' => is_array($s) ? ($s['qr_code'] ?? '-') : '-',
                'foto' => $fotoUrl,
                'status' => $hasil['status'] ?? '-',
                'jam' => $hasil['jam'] ?? substr($now, 0, 5) . ' WIB',
                'tanggal' => $today,
            ],
        ]);
        exit;
    }

    if ($hasil['ok']) {
        $s = $hasil['siswa'];
        $_SESSION['hasil_scan'] = [
            'ok' => true,
            'code' => 'SUCCESS',
            'student_id' => $s['student_id'] ?? '-',
            'nama' => $s['nama'],
            'nis' => $s['nis'],
            'kelas' => $s['nama_kelas'] ?? '-',
            'foto' => is_string($s['foto'] ?? null) ? $s['foto'] : null,
            'status' => $hasil['status'],
            'jam' => $hasil['jam'],
            'tanggal' => $today,
            'pesan' => $hasil['pesan'],
        ];
        set_flash_message('success', 'Absensi berhasil: ' . $s['nama'] . ' (' . $hasil['status'] . ' ' . $hasil['jam'] . ')');
    } else {
        $s = $hasil['siswa'] ?? null;
        $_SESSION['hasil_scan'] = [
            'ok' => false,
            'code' => $hasil['code'] ?? 'ERROR',
            'nama' => is_array($s) ? ($s['nama'] ?? '-') : '-',
            'nis' => is_array($s) ? ($s['nis'] ?? '-') : $kode,
            'kelas' => is_array($s) ? ($s['nama_kelas'] ?? '-') : '-',
            'foto' => (is_array($s) && is_string($s['foto'] ?? null)) ? $s['foto'] : null,
            'status' => $hasil['status'] ?? null,
            'jam' => $hasil['jam'] ?? null,
            'tanggal' => $today,
            'pesan' => $hasil['pesan'],
        ];
        set_flash_message('warning', $hasil['pesan']);
    }
} catch (PDOException $e) {
    $msg = ((int) $e->getCode() === 23000)
        ? 'Absensi hari ini sudah tercatat.'
        : 'Terjadi kesalahan saat menyimpan absensi. Silakan coba lagi.';

    if ($isAjax) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'success' => false,
            'code' => 'ALREADY_ATTENDED',
            'pesan' => $msg,
            'data' => [
                'nama' => '-',
                'nis' => $kode,
                'kelas' => '-',
                'foto' => null,
                'status' => 'Sudah Absen',
                'jam' => substr($now, 0, 5) . ' WIB',
                'tanggal' => $today,
            ],
        ]);
        exit;
    }

    set_flash_message('warning', $msg);
} catch (Throwable $e) {
    $msg = 'Terjadi kendala sistem. Silakan coba beberapa saat lagi.';
    if ($isAjax) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['success' => false, 'code' => 'SERVER_ERROR', 'pesan' => $msg]);
        exit;
    }
    set_flash_message('danger', $msg);
}

redirect('modules/scan/index.php');
