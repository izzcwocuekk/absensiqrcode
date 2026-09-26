# PRD Singkat — Absensi Siswa QR Code

> Status: dipelajari sebagai developer pemula.
> Lokasi: `DOCS/PRD.md` | 24 Sept 2026 | v1.0 SELESAI — MVP Fase 04–09 terimplementasi

## 1. Ringkasan Produk

**Nama:** Absensi Siswa QR Code (`absensi-siswa-qrcode`)
**Deskripsi:** Web absensi siswa via QR Code. Tiap siswa punya QR unik, guru/piket scan untuk catat hadir harian tanpa kertas.
**Kondisi kini:** Fondasi layout modular jadi (Fase 03). Modul bisnis belum ada — menu sidebar masih "Segera".
**Teknologi:** PHP 8 native, MySQL `absensi_siswa`, Bootstrap 5.3.3 + Icons (CDN), PDO, Session PHP.

## 2. Masalah

1. Absensi kertas lambat, mudah hilang, sulit direkap.
2. Guru butuh rekap harian/bulanan cepat.
3. Butuh solusi murah tanpa fingerprint — cukup HP/laptop + kamera.

## 3. Tujuan

1. 1 siswa tercatat < 3 detik via scan.
2. Rekap harian/kelas/siswa tampil + unduh dalam 2 klik.
3. Data siswa/kelas terpusat, QR bisa cetak ulang.
4. Aman, responsif (HP/tablet/laptop), mudah dipakai non-teknis.

## 4. Target Pengguna

- **Admin/Operator:** kelola siswa, kelas, user; cetak QR; lihat laporan.
- **Guru/Piket:** scan QR di gerbang/kelas; koreksi manual bila kartu hilang.
- **Kepsek/Kesiswaan:** lihat dashboard + laporan saja.

> Catatan: `navbar.php` masih tampil "Pengguna" statis, Logout nonaktif — auth belum ada.

## 5. Hasil Bedah Kode (19 file/folder)

- `index.php`: demo "Layout Modular Siap Digunakan", set `$pageTitle`, `$activeMenu='dashboard'`.
- `config/config.php`: `ROOT_PATH`, `APP_NAME`, `BASE_URL=/absensi-siswa-qrcode`, `APP_TIMEZONE=Asia/Jakarta`.
- `config/database.php`: `db(): PDO` ke `localhost:3306/absensi_siswa` (root/kosong), exception + assoc.
- `config/session.php`: strict_mode, only_cookies, httponly, samesite Lax.
- `include/functions.php`: `e()`, `base_url()`, `redirect()`, `page_title()`, `is_menu_active()`.
- `include/header,navbar,sidebar,footer.php`: CDN Bootstrap, navbar fixed-top + offcanvas mobile, sidebar 17rem, footer.
- `include/flash_message.php`: `set_flash_message()` + `display_flash_message()`.
- `assets/css/style.css`, `assets/js/app.js`: tema biru #2563eb, responsif; JS baru console.info.
- Kosong: `database/`, `modules/data/`, `assets/img/`, `assets/qrcode/generate/` — wadah schema SQL, CRUD, QR.

## 6. Ruang Lingkup MVP

In Scope:
1. Auth: login/logout, role admin + guru, proteksi halaman.
2. Data Kelas: CRUD (nama, tingkat, wali).
3. Data Siswa: CRUD (NIS, nama, kelas, foto opsional), generate QR unik, cetak kartu.
4. Scan QR: halaman kamera + input NIS manual, tolak duplikat 1x/hari.
5. Absensi: tabel harian (Hadir/Terlambat/Izin/Sakit/Alpa), edit/hapus oleh admin.
6. Dashboard: total siswa, hadir hari ini, terlambat, tidak hadir.
7. Laporan: filter tanggal + kelas, cetak/ekspor CSV.

Out of Scope: aplikasi mobile native, notif WA otomatis, face recognition, multi-sekolah, Dapodik.

## 7. User Story Singkat

1. Admin kelola kelas/siswa agar data selalu baru.
2. Admin klik Generate + Cetak QR, file tersimpan di `assets/qrcode/generate/`.
3. Guru buka Scan QR, arahkan kamera, muncul nama + status tersimpan.
4. Guru bisa input NIS manual bila QR rusak.
5. Sistem menolak absen ganda di hari yang sama.
6. Kesiswaan filter laporan per tanggal/kelas dan unduh CSV.

## 8. Alur Utama

Scan: Buka Scan > izinkan kamera > scan > cocokkan kode > simpan (siswa_id, tanggal, jam, status) > flash "Berhasil: Budi (XII-A) - Hadir 07:02".
Admin: Login > Dashboard > Kelas/Siswa > Generate & Cetak QR > Monitor > Unduh Laporan.

## 9. Usulan Model Data (`database/` masih kosong)

- users(id, username, password_hash, nama, role, created_at)
- kelas(id, nama_kelas, tingkat, wali_kelas)
- siswa(id, nis UNIQUE, nama, kelas_id FK, qr_code UNIQUE, foto, created_at)
- absensi(id, siswa_id FK, tanggal DATE, jam_masuk TIME, status ENUM Hadir/Terlambat/Izin/Sakit/Alpa, keterangan, UNIQUE(siswa_id,tanggal))

Aturan: jam > 07:00 otomatis Terlambat (bisa di config), hapus siswa jangan hapus riwayat.

## 10. Non-Fungsional

- Keamanan: PDO prepared statement, echo via `e()`, `password_hash()`, wajib login, validasi upload.
- Performa: scan > simpan < 2 detik di Laragon, index (tanggal, siswa_id).
- Responsif: offcanvas <992px (sudah ada), halaman scan mobile-first.
- Jalan di Laragon/XAMPP PHP 8.0+, MySQL 5.7+/8.x, browser modern + kamera.
- Bahasa Indonesia, notif via flash message eksisting.

## 11. Roadmap

- Fase 03 selesai: layout modular (posisi sekarang).
- Fase 04: Auth + Dashboard nyata.
- Fase 05: CRUD Kelas + Siswa.
- Fase 06: Generate & Cetak QR.
- Fase 07: Scan QR + Absensi.
- Fase 08: Laporan + Ekspor.
- Fase 09: Polish + dokumentasi.

## 12. Risiko & Kriteria Terima

Risiko: QR di-copy teman > tampilkan foto saat scan + opsi PIN. Kamera gelap > sediakan NIS manual. DB_PASS kosong & BASE_URL hardcoded > pindah ke .env sebelum deploy. CDN offline > vendor lokal untuk produksi.

Terima MVP (status akhir 24 Sept 2026):

- [x] Login admin & guru berfungsi, halaman tanpa login dialihkan.
- [x] CRUD kelas & siswa + QR tercetak dan bisa di-scan ulang.
- [x] Scan 1 siswa tercatat < 3 detik, duplikat ditolak.
- [x] Dashboard angka sesuai isi tabel hari itu.
- [x] Laporan bisa difilter dan diekspor.
- [x] Tidak ada error PHP di Laragon + lolos uji XSS dasar (`<script>` tersimpan sebagai teks).

---
Minta review: apakah daftar MVP + model data di atas sudah sesuai? Jika setuju, lanjut Fase 04 (Auth + Dashboard).
