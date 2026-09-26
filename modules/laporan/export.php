<?php
declare(strict_types=1);

/**
 * Fase 08 — Ekspor CSV laporan (parameter filter sama dengan index).
 * BOM \xEF\xBB\xBF agar rapi dibuka di Excel Indonesia.
 */

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/laporan/fungsi_laporan.php';

require_staff();

$filter = laporan_filter();
[$rows] = laporan_data(db(), $filter['dari'], $filter['sampai'], $filter['kelas_id'], $filter['teacher_id'], $filter['subject_id'], $filter['lesson_number'], $filter['status']);

$namaFile = 'laporan-absensi-' . $filter['dari'] . '-' . $filter['sampai'] . '.csv';
header('Content-Type: text/csv; charset=utf-8');
header('Content-Disposition: attachment; filename="' . $namaFile . '"');

$out = fopen('php://output', 'wb');
fwrite($out, "\xEF\xBB\xBF");
fputcsv($out, ['No', 'Student ID', 'Tanggal', 'NIS', 'Nama', 'Kelas', 'Guru', 'Mata Pelajaran', 'Les', 'Jam', 'Status', 'Keterangan']);
foreach ($rows as $i => $row) {
    fputcsv($out, [
        $i + 1,
        $row['student_id'] ?? '-',
        $row['tanggal'],
        $row['nis'],
        $row['nama'],
        $row['nama_kelas'] ?? '-',
        $row['nama_guru'] ?? '-',
        $row['nama_mata_pelajaran'] ?? '-',
        $row['lesson_number'] ?? '-',
        $row['jam_masuk'] !== null ? (strlen((string)$row['jam_masuk']) > 10 ? substr((string)$row['jam_masuk'], 11, 5) : substr((string)$row['jam_masuk'], 0, 5)) : '-',
        $row['status'],
        $row['keterangan'] ?? '-',
    ]);
}
fclose($out);
exit;
