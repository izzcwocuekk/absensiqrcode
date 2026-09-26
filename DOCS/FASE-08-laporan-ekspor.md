# Prompt Fase 08 — Laporan + Ekspor

> Sumber: `DOCS/PRD.md` #6, #7, #10 | Prasyarat: Fase 07 selesai

## Konteks
Data absensi sudah ada. Fokus filter cepat + unduh 2 klik.

## Tugas
```
Buatkan Laporan + Ekspor mengikuti DOCS/PRD.md.

1. Buat modules/laporan/index.php: filter GET (tanggal_dari,
   tanggal_sampai default bulan ini, kelas_id dropdown Semua),
   tabel rekap (No, Tanggal, NIS, Nama, Kelas, Jam, Status badge warna,
   Keterangan), ringkasan count per status di atas tabel.
2. Buat modules/laporan/export.php: parameter sama, output CSV
   (header text/csv + Content-Disposition attachment),
   gunakan fputcsv + BOM agar rapi di Excel, semua via prepared statement.
3. Tambah tombol Print (window.print, CSS print sembunyikan filter)
   dan tombol Ekspor CSV yang membawa filter aktif via query string.
4. Aktifkan menu laporan di sidebar, require_login.
Kriteria terima: filter akurat, CSV terbuka rapi di Excel,
cetak hanya tabel, query tetap cepat dengan index tanggal.
```
