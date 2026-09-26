# Prompt Fase 07 — Scan QR + Absensi Harian

> Sumber: `DOCS/PRD.md` #6, #7, #8, #9 | Prasyarat: Fase 05-06 selesai

## Konteks
Tabel absensi belum ada. Halaman harus mobile-first, latihan kamera.

## Tugas
```
Buatkan Scan QR + Absensi Harian mengikuti DOCS/PRD.md.

1. Tambah ke schema: absensi(id AI PK, siswa_id FK, tanggal DATE,
   jam_masuk TIME, status ENUM Hadir/Terlambat/Izin/Sakit/Alpa,
   keterangan TEXT NULL, UNIQUE(siswa_id,tanggal),
   INDEX(tanggal, siswa_id)). Aturan: jam > 07:00:00 = Terlambat.
2. Buat modules/scan/index.php: gunakan html5-qrcode CDN + fallback
   form input NIS/qr_code manual. Hasil scan POST ke
   modules/scan/proses.php: cocokkan qr_code/NIS, cegah duplikat
   (SELECT hari ini dulu), insert, tampilkan kartu hasil
   (foto, nama, kelas, jam, status) + flash.
3. Buat modules/absensi/index.php: tabel hari ini (filter tanggal default
   today), tombol Izin/Sakit/Alpa manual + edit/hapus admin.
4. Aktifkan menu scan + absensi di sidebar, require_login (guru boleh scan).
Kriteria terima: 1 scan < 3 detik, duplikat ditolak dengan pesan jelas,
kamera HP + laptop jalan, NIS manual jalan saat kamera mati.
```
