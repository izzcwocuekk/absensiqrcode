# Prompt Fase 06 — Generate & Cetak QR

> Sumber: `DOCS/PRD.md` #6, #7 | Prasyarat: Fase 05 selesai

## Konteks
Tabel siswa sudah punya kolom qr_code unik (teks).
Folder target: `assets/qrcode/generate/`. Tanpa composer bila bisa.

## Tugas
```
Buatkan Generate & Cetak QR mengikuti DOCS/PRD.md.

1. Tanpa library luar bila memungkinkan: gunakan API internal
   modules/siswa/qr.php?code=XXX yang me-render QR via library ringan
   (mis. salin file qrcode-generator tunggal ke include/qrcode.php)
   atau bila offline gunakan GD membuat gambar kode + teks NIS.
   Dokumentasikan pilihan di header file.
2. Buat modules/siswa/kartu.php?id=: kartu siswa ukuran cetak
   (foto, nama, NIS, kelas, gambar QR dari qr.php), tombol Print
   dengan CSS @media print, 1 kartu per halaman A6.
3. Tambah tombol di modules/siswa/index.php: [QR] [Kartu] [Cetak Ulang].
   File gambar disimpan di assets/qrcode/generate/{qr_code}.png
   dan di-regenerate bila file belum ada.
4. Proteksi require_login, semua output via e(), nama file sanitasi.
Kriteria terima: tiap siswa punya 1 file PNG valid, di-scan manual
terbaca kodenya, cetak rapi di Chrome, regenerate idempoten.
```
