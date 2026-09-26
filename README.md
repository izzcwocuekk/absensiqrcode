# Absensi Siswa QR Code

Aplikasi web absensi siswa berbasis QR Code (PHP 8 native + MySQL).
Tiap siswa punya QR unik (`qr_token`), guru/piket scan untuk mencatat
kehadiran harian. Dibangun bertahap Fase 03–09 mengikuti `DOCS/PRD.md`.

## Syarat

- Laragon/XAMPP: PHP 8.0+ dengan ekstensi `gd`, `mbstring`, `pdo_mysql`
- MySQL 5.7+/8.x, browser modern + kamera (untuk scan)

## Instalasi 10 Menit (Laragon)

1. Simpan folder sebagai `C:\laragon\www\absensi-siswa-qrcode`,
   jalankan Laragon (Start All).
2. Aktifkan GD bila belum: buka `C:\xampp\php\php.ini`,
   ubah `;extension=gd` menjadi `extension=gd`, restart Laragon.
3. Buat database `absensi_siswa`, import berurutan:
   - `database/schema.sql` (wajib; idempoten, data lama aman)
   - `database/seed_demo.sql` (opsional demo: 5 kelas, 20 siswa,
     absensi 5 hari kerja)
4. (Opsional hosting) salin `config/config.local.example.php`
   menjadi `config/config.local.php` lalu isi `BASE_URL` + kredensial DB.
5. Buka `http://localhost/absensi-siswa-qrcode/modules/auth/login.php`

## Login Default

- Admin: `admin` / `admin123` (semua modul + hapus)
- Guru: `guru` / `guru123` (lihat, scan, koreksi Izin/Sakit/Alpa)

## Alur Pakai

1. Admin: Data Kelas > Tambah > Data Siswa > Tambah (QR token otomatis).
2. Admin: Data Siswa > Generate Semua QR > tombol QR/Kartu > Cetak kartu.
3. Guru: Scan QR > izinkan kamera > scan kartu (atau ketik NIS manual).
   Tepat waktu s/d 07:00 = Hadir, lewat = Terlambat, ganda ditolak.
4. Semua: Absensi (filter tanggal, koreksi manual) >
   Laporan (filter periode + kelas) > Ekspor CSV / Cetak.

## Struktur

- `config/` — `config.php` (+override `config.local.php`), `database.php`
- `include/` — layout + helper `e()`, flash, guard, `qrcode_helper.php`,
  library QR `lib-qrcode/qrcode.php` (MIT Kazuhiko Arase)
- `modules/auth,kelas,siswa,scan,absensi,laporan,error/` — fitur Fase 04–09
- `database/` — `schema.sql`, `seed_demo.sql`
- `DOCS/` — `PRD.md` + prompt per fase, `assets/` — css/js/img/qrcode

## Keamanan

Prepared statement semua query, `e()` semua echo, `password_hash()`,
guard login + role, CSRF token semua form POST, upload foto JPG/PNG max
2MB + cek MIME, nama file disanitasi, session httponly + samesite Lax.
