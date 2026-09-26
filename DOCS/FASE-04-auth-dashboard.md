# Prompt Fase 04 — Auth + Dashboard Nyata

> Sumber: `DOCS/PRD.md` #6, #9, #11 | Prasyarat: Fase 03 selesai

## Konteks
Proyek PHP 8 native di `c:\laragon\www\absensi-siswa-qrcode`.
Pola halaman: set `$pageTitle` + `$activeMenu`, lalu
`require header.php / navbar.php / sidebar.php ... footer.php`.
Helper wajib: `e()`, `base_url()`, `redirect()`, `db(): PDO`, flash message.

## Tugas (copy-paste ke AI agent)
```
Buatkan modul Auth + Dashboard nyata untuk Absensi Siswa QR Code
mengikuti DOCS/PRD.md dan pola layout eksisting.

1. Buat database/schema.sql berisi tabel users(id, username UNIQUE,
   password_hash, nama, role ENUM admin/guru, created_at) + 2 user dummy
   (admin/admin123, guru/guru123 dengan password_hash()).
2. Buat modules/auth/login.php, logout.php, guard.php:
   - login.php: form username+password, Bootstrap card centered,
     validasi POST, password_verify, set $_SESSION['user'],
     set_flash_message + redirect.
   - guard.php: fungsi require_login() dan require_role('admin'),
     redirect ke modules/auth/login.php bila belum login.
   - logout.php: session_destroy + redirect login.
3. Update include/navbar.php: tampilkan $_SESSION['user']['nama'] + role,
   bukan "Pengguna" statis. Update sidebar.php: aktifkan menu dashboard,
   Logout jadi link asli ke logout.php.
4. Ubah index.php jadi dashboard nyata (guard require_login):
   4 kartu statistik (total siswa, hadir hari ini, terlambat, belum hadir)
   query dari tabel siswa/absensi; jika tabel belum ada tampilkan 0
   dengan try/catch agar tidak error sebelum Fase 05.
5. Aturan: semua query PDO prepared statement, semua echo via e(),
   password via password_hash/password_verify, Bahasa Indonesia.
Kriteria terima: login 2 role jalan, halaman tanpa login dialihkan,
logout membersihkan session, dashboard angka sesuai DB, tanpa error PHP.
Uji di Laragon: http://localhost/absensi-siswa-qrcode/index.php
```
