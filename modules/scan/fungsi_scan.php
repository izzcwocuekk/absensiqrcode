<?php
declare(strict_types=1);

/**
 * Logika Inti Pemindaian QR Absensi Siswa.
 *
 * Alur Server-side:
 * QR Token -> Validasi token -> Cari siswa -> Cek status aktif ->
 * Cek absensi hari ini (cegah duplikat di DB & aplikasi) ->
 * Tentukan status Hadir/Terlambat berdasarkan jam server -> Simpan absensi.
 */
function catat_scan(PDO $pdo, string $kode, string $nowDate, string $nowTime): array
{
    $kode = trim($kode);
    if ($kode === '') {
        return [
            'ok' => false,
            'code' => 'EMPTY_CODE',
            'pesan' => 'Kode QR tidak boleh kosong.',
        ];
    }

    // 1. Validasi token memakai resolver yang kompatibel dengan schema dasar
    // maupun migration student_id/qr_code. Fallback dipakai bila fungsi
    // learning belum dimuat pada instalasi lama.
    if (function_exists('resolve_student')) {
        $siswa = resolve_student($pdo, $kode);
    } else {
        $stmt = $pdo->prepare('SELECT s.*, c.nama_kelas, c.jurusan FROM students s LEFT JOIN classes c ON c.id=s.class_id WHERE s.id=:id OR s.qr_token=:token OR s.nis=:nis LIMIT 1');
        $stmt->execute([':id' => ctype_digit($kode) ? (int) $kode : 0, ':token' => $kode, ':nis' => $kode]);
        $siswa = $stmt->fetch() ?: null;
    }

    if (!$siswa) {
        return [
            'ok' => false,
            'code' => 'INVALID_CODE',
            'pesan' => 'QR Code tidak valid atau siswa tidak terdaftar.',
        ];
    }

    // Gunakan student_id sebagai identifier utama
    if (function_exists('learning_normalize_student')) $siswa = learning_normalize_student($pdo, $siswa);
    $targetStudentId = !empty($siswa['student_id']) ? (string) $siswa['student_id'] : (string) $siswa['id'];

    // 2. Cek apakah siswa berstatus aktif
    if (isset($siswa['is_active']) && (int) $siswa['is_active'] === 0) {
        return [
            'ok' => false,
            'code' => 'STUDENT_INACTIVE',
            'pesan' => 'Siswa berstatus tidak aktif.',
            'siswa' => $siswa,
        ];
    }

    // 3. Cek apakah siswa sudah melakukan absensi hari ini (Cegah Duplikasi berdasarkan student_id)
    $cek = $pdo->prepare(
        'SELECT id, status, jam_masuk, tanggal 
         FROM attendances 
         WHERE student_id = :sid AND tanggal = :tgl 
         LIMIT 1'
    );
    $cek->execute([':sid' => $targetStudentId, ':tgl' => $nowDate]);
    $sudah = $cek->fetch();

    $waktuFormatted = substr($nowTime, 0, 5) . ' WIB';

    if ($sudah) {
        $jamAbsenLalu = $sudah['jam_masuk'] !== null 
            ? substr((string) $sudah['jam_masuk'], 0, 5) . ' WIB' 
            : '-';

        return [
            'ok' => false,
            'code' => 'ALREADY_ATTENDED',
            'pesan' => 'Absensi hari ini sudah tercatat.',
            'siswa' => $siswa,
            'status' => $sudah['status'],
            'jam' => $jamAbsenLalu,
            'tanggal' => $sudah['tanggal'],
        ];
    }

    // 4. Ambil aturan batas waktu keterlambatan dari pengaturan database
    $settings = get_attendance_settings();
    $batasWaktu = $settings['late_after'] ?? '07:00:00';

    // 5. Tentukan status kehadiran berdasarkan waktu server
    $status = ($nowTime > $batasWaktu) ? 'Terlambat' : 'Hadir';

    // 6. Simpan absensi ke database dengan student_id unik
    try {
        $ins = $pdo->prepare(
            'INSERT INTO attendances (student_id, tanggal, jam_masuk, status) 
             VALUES (:sid, :tgl, :jam, :status)'
        );
        $ins->execute([
            ':sid' => $targetStudentId,
            ':tgl' => $nowDate,
            ':jam' => $nowTime,
            ':status' => $status,
        ]);
    } catch (PDOException $e) {
        // Jika terjadi balapan request ganda (duplicate key)
        if ((int) $e->getCode() === 23000 || str_contains($e->getMessage(), 'uq_attendances_student_date')) {
            $cekUlang = $pdo->prepare(
                'SELECT jam_masuk, status, tanggal FROM attendances WHERE student_id = :sid AND tanggal = :tgl LIMIT 1'
            );
            $cekUlang->execute([':sid' => $targetStudentId, ':tgl' => $nowDate]);
            $rowExist = $cekUlang->fetch();
            $jamLalu = ($rowExist && $rowExist['jam_masuk']) ? substr((string) $rowExist['jam_masuk'], 0, 5) . ' WIB' : $waktuFormatted;

            return [
                'ok' => false,
                'code' => 'ALREADY_ATTENDED',
                'pesan' => 'Absensi hari ini sudah tercatat.',
                'siswa' => $siswa,
                'data' => $siswa,
                'status' => $rowExist['status'] ?? $status,
                'jam' => $jamLalu,
                'tanggal' => $nowDate,
            ];
        }
        throw $e;
    }

    return [
        'ok' => true,
        'code' => 'SUCCESS',
        'pesan' => 'Absensi berhasil dicatat.',
        'status' => $status,
        'jam' => $waktuFormatted,
        'tanggal' => $nowDate,
        'siswa' => $siswa,
        'data' => $siswa,
    ];
}
