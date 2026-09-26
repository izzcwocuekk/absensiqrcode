# Prompt Fase 09 — Polish + Dokumentasi

> Sumber: `DOCS/PRD.md` #10, #12 | Prasyarat: Fase 04-08 selesai

## Konteks
Semua fitur MVP jadi. Fokus keamanan, kerapian, siap demo/hosting.

## Tugas
```
Lakukan Polish + Dokumentasi mengikuti DOCS/PRD.md.

1. Audit: pastikan semua echo via e(), semua SQL prepared,
   semua halaman modul pakai guard, upload validasi ketat,
   session regenerate saat login, CSRF token sederhana untuk hapus/ubah.
2. UX: samakan judul via page_title(), pesan flash konsisten
   Bahasa Indonesia, halaman 404 sederhana, sidebar menu semua aktif
   (hapus badge Segera), footer tahun otomatis (sudah ada).
3. Konfig: dukung config.local.php / .env untuk DB_PASS + BASE_URL
   (fallback ke config.php agar Laragon tetap jalan),
   sediakan database/seed_demo.sql (5 kelas, 20 siswa, absensi seminggu).
4. Dokumen: buat README.md (cara install Laragon, import schema,
   login default, alur scan) + update DOCS/PRD.md checklist terima
   menjadi centang. Uji akhir: tanpa error PHP, lolos XSS dasar,
   scan < 3 detik, laporan terekspor.
Kriteria terima: siap demo offline maupun hosting, README bisa diikuti
pemula dalam 10 menit.
```
