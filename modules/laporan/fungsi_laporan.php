<?php
declare(strict_types=1);

/**
 * Fase 08 — Logika filter laporan bersama (dipakai index.php + export.php).
 * Filter GET: dari, sampai (default bulan berjalan), kelas_id (0 = semua).
 */
function laporan_filter(): array
{
    $awalBulan = date('Y-m-01');
    $akhirBulan = date('Y-m-t');

    $dari = (string) ($_GET['dari'] ?? $awalBulan);
    $sampai = (string) ($_GET['sampai'] ?? $akhirBulan);
    if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $dari)) {
        $dari = $awalBulan;
    }
    if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $sampai)) {
        $sampai = $akhirBulan;
    }
    if ($dari > $sampai) {
        [$dari, $sampai] = [$sampai, $dari];
    }

    $kelasId = (int) ($_GET['kelas_id'] ?? 0);

    return ['dari' => $dari, 'sampai' => $sampai, 'kelas_id' => $kelasId];
}

/** Query rekap + ringkasan per status sesuai filter. */
function laporan_data(PDO $pdo, string $dari, string $sampai, int $kelasId): array
{
    $where = 'a.tanggal BETWEEN :dari AND :sampai';
    $params = [':dari' => $dari, ':sampai' => $sampai];
    if ($kelasId > 0) {
        $where .= ' AND s.class_id = :kelas';
        $params[':kelas'] = $kelasId;
    }

    $stmt = $pdo->prepare(
        "SELECT a.tanggal, a.jam_masuk, a.status, a.keterangan, s.student_id, s.nis, s.nama, c.nama_kelas
         FROM attendances a
         JOIN students s ON (s.student_id = a.student_id OR s.id = a.student_id)
         LEFT JOIN classes c ON c.id = s.class_id
         WHERE $where ORDER BY a.tanggal DESC, s.student_id ASC, s.nama ASC LIMIT 2000"
    );
    $stmt->execute($params);
    $rows = $stmt->fetchAll();

    $ringkas = ['Hadir' => 0, 'Terlambat' => 0, 'Izin' => 0, 'Sakit' => 0, 'Alfa' => 0];
    foreach ($rows as $row) {
        $st = (string) ($row['status'] ?? '');
        if ($st === 'Alpa') {
            $st = 'Alfa';
        }
        if (isset($ringkas[$st])) {
            $ringkas[$st]++;
        }
    }

    return [$rows, $ringkas];
}
