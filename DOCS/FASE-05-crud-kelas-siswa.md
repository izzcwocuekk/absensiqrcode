# Prompt Fase 05 — CRUD Kelas + Siswa

> Sumber: `DOCS/PRD.md` #6, #7, #9 | Prasyarat: Fase 04 selesai

## Konteks
PHP 8 native, pola layout `header/navbar/sidebar/footer`,
helper `e(), base_url(), redirect(), db(), flash`, guard `require_login()`,
`require_role('admin')` dari Fase 04.

## Tugas
```
Buatkan CRUD Kelas + Siswa mengikuti DOCS/PRD.md.

1. Tambah ke database/schema.sql (CREATE TABLE IF NOT EXISTS):
   kelas(id INT AI PK, nama_kelas VARCHAR(50), tingkat VARCHAR(20),
   wali_kelas VARCHAR(100)) dan siswa(id INT AI PK, nis VARCHAR(20) UNIQUE,
   nama VARCHAR(100), kelas_id INT FK kelas(id), qr_code VARCHAR(64) UNIQUE,
   foto VARCHAR(255) NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP).
   qr_code diisi random 32 hex saat insert (bin2hex(random_bytes(16))).
2. Buat modules/kelas/index.php, tambah.php, ubah.php, hapus.php:
   tabel Bootstrap + tombol Tambah/Ubah/Hapus, validasi required,
   flash sukses/gagal, hapus konfirmasi + tolak bila masih ada siswa.
3. Buat modules/siswa/index.php, tambah.php, ubah.php, hapus.php:
   form NIS, nama, dropdown kelas, upload foto opsional ke assets/img/
   (validasi ekstensi jpg/png, max 2MB, rename nis-timestamp).
   Tampilkan badge qr_code (belum gambar QR, baru teks kode).
4. Aktifkan menu sidebar siswa + kelas (enabled true, url asli),
   proteksi semua halaman admin-only kecuali index dibaca guru.
Kriteria terima: CRUD jalan, NIS ganda ditolak, FK aman,
foto tersimpan rapi, semua echo via e(), tanpa error PHP.
```
