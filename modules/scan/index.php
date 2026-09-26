<?php
declare(strict_types=1);

/**
 * Modul Absensi Siswa Menggunakan QR Code.
 * Terintegrasi penuh dengan kamera scanner, validasi server-side, 
 * audio feedback, modal konfirmasi, dan input manual cadangan.
 */

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';

require_login();

$pageTitle = 'Absensi Siswa';
$activeMenu = 'scan';
$today = date('Y-m-d');

// Ambil riwayat absensi terbaru hari ini untuk feedback visual guru/piket
$recentStmt = db()->prepare(
    'SELECT a.*, s.student_id, s.nis, s.nama, s.foto, c.nama_kelas
     FROM attendances a
     JOIN students s ON (s.student_id = a.student_id OR s.id = a.student_id)
     LEFT JOIN classes c ON c.id = s.class_id
     WHERE a.tanggal = :tgl
     ORDER BY a.id DESC LIMIT 10'
);
$recentStmt->execute([':tgl' => $today]);
$recentRows = $recentStmt->fetchAll();

$hasilSession = $_SESSION['hasil_scan'] ?? null;
unset($_SESSION['hasil_scan']);

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>

        <!-- Header Halaman -->
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
            <div>
                <h1 class="h3 fw-bold mb-1 text-dark">Absensi Siswa</h1>
                <p class="text-secondary mb-0">Scan QR Code untuk mencatat kehadiran</p>
            </div>
            <div class="d-flex align-items-center gap-2">
                <span class="badge bg-white text-secondary border px-3 py-2">
                    <i class="bi bi-clock me-1 text-primary"></i>Batas Tepat Waktu: <strong>07:00 WIB</strong>
                </span>
                <span class="badge bg-white text-secondary border px-3 py-2 d-none d-sm-inline-block">
                    <i class="bi bi-calendar3 me-1 text-primary"></i><?= e(date('d M Y')) ?>
                </span>
            </div>
        </div>

        <div class="row g-4">
            <!-- Kolom Kiri: Scanner Kamera -->
            <div class="col-12 col-lg-7">
                <div class="card scanner-card border-0 shadow-sm">
                    <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-camera-video text-primary fs-5"></i>
                            <h2 class="h6 fw-bold mb-0">Scanner Kamera</h2>
                        </div>
                        <div id="cameraStatusPill" class="camera-indicator bg-light text-secondary">
                            <span class="pulse-dot"></span>
                            <span id="cameraStatusText">Memulai Kamera...</span>
                        </div>
                    </div>

                    <div class="card-body p-3 p-md-4">
                        <!-- Area Viewport Scanner -->
                        <div class="scanner-viewport-wrapper mb-3" id="scannerViewport">
                            <div id="qr-reader"></div>

                            <!-- Target Box / Viewfinder Overlay -->
                            <div class="scanner-overlay" id="scannerOverlay">
                                <div class="viewfinder-corner top-left"></div>
                                <div class="viewfinder-corner top-right"></div>
                                <div class="viewfinder-corner bottom-left"></div>
                                <div class="viewfinder-corner bottom-right"></div>
                                <div class="scanner-laser" id="scannerLaser"></div>
                            </div>
                        </div>

                        <!-- Keterangan & Instruksi -->
                        <div class="text-center mb-3">
                            <p class="fw-semibold text-dark mb-1">
                                <i class="bi bi-crosshair me-1 text-primary"></i>Posisikan QR Code di dalam area
                            </p>
                            <p class="text-secondary small mb-0" id="scannerPromptText">
                                Menunggu QR Code...
                            </p>
                        </div>

                        <!-- Baris Kontrol Kamera -->
                        <div class="d-flex flex-wrap justify-content-center align-items-center gap-2 pt-2 border-top">
                            <button id="btnSwitchCamera" class="btn btn-sm btn-outline-secondary d-none" type="button">
                                <i class="bi bi-arrow-repeat me-1"></i>Ganti Kamera
                            </button>
                            <button id="btnRestartCamera" class="btn btn-sm btn-outline-primary" type="button">
                                <i class="bi bi-arrow-clockwise me-1"></i>Restart Kamera
                            </button>
                            <button id="btnScanFile" class="btn btn-sm btn-outline-success" type="button"
                                title="Ambil QR dari gambar/foto screenshot kartu">
                                <i class="bi bi-image me-1"></i>Scan dari Foto
                            </button>
                            <input type="file" id="qrFileInput" accept="image/*" hidden>
                            <span class="text-secondary small ms-sm-2 text-center text-sm-start w-100 w-sm-auto mt-2 mt-sm-0">
                                <i class="bi bi-info-circle me-1"></i>Pastikan kamera memiliki izin untuk digunakan.
                            </span>
                        </div>

                        <!-- Pesan Error Kamera (Disembunyikan secara default) -->
                        <div id="cameraErrorBox" class="alert alert-warning mt-3 mb-0 d-none" role="alert">
                            <div class="d-flex align-items-start gap-2">
                                <i class="bi bi-exclamation-triangle-fill fs-5 flex-shrink-0"></i>
                                <div>
                                    <h3 class="h6 fw-bold mb-1">Akses Kamera Terkendala</h3>
                                    <p class="small mb-2" id="cameraErrorMessage">
                                        Browser belum memiliki izin kamera atau kamera sedang digunakan aplikasi lain.
                                    </p>
                                    <button id="btnRetryCamera" class="btn btn-sm btn-warning" type="button">
                                        <i class="bi bi-arrow-clockwise me-1"></i>Coba Lagi
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Kolom Kanan: Input Manual & Info Cepat -->
            <div class="col-12 col-lg-5">
                <!-- Card Input Manual -->
                <div class="card border-0 shadow-sm mb-4">
                    <div class="card-header bg-white border-bottom py-3">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-keyboard text-primary fs-5"></i>
                            <h2 class="h6 fw-bold mb-0">Input Manual (NIS / Token)</h2>
                        </div>
                    </div>
                    <div class="card-body p-3 p-md-4">
                        <p class="text-secondary small mb-3">
                            Jika kartu siswa rusak atau kamera bermasalah, masukkan NIS atau token QR siswa:
                        </p>
                        <form id="formManual" method="post" action="<?= e(base_url('modules/scan/proses.php')) ?>">
                            <?php csrf_field(); ?>
                            <div class="mb-3">
                                <label class="form-label small fw-semibold text-secondary" for="manualKode">ID Siswa / QR Code / NIS</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light"><i class="bi bi-upc-scan text-secondary"></i></span>
                                    <input class="form-control font-monospace" type="text" id="manualKode" name="kode" required
                                        placeholder="cth: STU001, QR-STU001, atau NIS" autocomplete="off">
                                    <button class="btn btn-primary" type="submit" id="btnManualSubmit">
                                        <i class="bi bi-check2-circle me-1"></i>Catat
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Card Riwayat Absensi Terkini Hari Ini -->
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-clock-history text-primary fs-5"></i>
                            <h2 class="h6 fw-bold mb-0">Absensi Terkini Hari Ini</h2>
                        </div>
                        <a href="<?= e(base_url('modules/absensi/index.php')) ?>" class="small text-decoration-none fw-semibold">
                            Lihat Semua <i class="bi bi-chevron-right"></i>
                        </a>
                    </div>
                    <div class="card-body p-0">
                        <div id="recentScansContainer" class="list-group list-group-flush">
                            <?php if ($recentRows === []): ?>
                                <div class="p-4 text-center text-secondary small empty-recent-msg">
                                    <i class="bi bi-person-check fs-2 text-muted d-block mb-1"></i>
                                    Belum ada absensi tercatat hari ini.
                                </div>
                            <?php else: ?>
                                <?php foreach ($recentRows as $r): ?>
                                    <div class="list-group-item d-flex align-items-center justify-content-between py-2 px-3">
                                        <div class="d-flex align-items-center gap-2">
                                            <?php if (!empty($r['foto']) && is_file(ROOT_PATH . '/assets/img/' . basename((string) $r['foto']))): ?>
                                                <img src="<?= e(base_url('assets/img/' . basename((string) $r['foto']))) ?>"
                                                    alt="Foto" width="34" height="34" class="rounded-circle object-fit-cover">
                                            <?php else: ?>
                                                <span class="rounded-circle bg-light text-primary d-inline-grid place-items-center" style="width:34px; height:34px;">
                                                    <i class="bi bi-person"></i>
                                                </span>
                                            <?php endif; ?>
                                            <div>
                                                <p class="fw-semibold mb-0 small text-dark d-flex align-items-center gap-1">
                                                    <?php if (!empty($r['student_id'])): ?>
                                                        <span class="badge bg-primary-subtle text-primary border font-monospace" style="font-size:0.65rem"><?= e($r['student_id']) ?></span>
                                                    <?php endif; ?>
                                                    <?= e($r['nama']) ?>
                                                </p>
                                                <span class="text-secondary" style="font-size:0.75rem">
                                                    <?= e($r['nama_kelas'] ?? '-') ?> &middot; <?= e(substr((string) ($r['jam_masuk'] ?? ''), 0, 5)) ?> WIB
                                                </span>
                                            </div>
                                        </div>
                                        <span class="badge badge-<?= strtolower((string) $r['status']) ?>">
                                            <?= e($r['status']) ?>
                                        </span>
                                    </div>
                                <?php endforeach; ?>
                            <?php endif; ?>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<!-- Hidden form untuk fallback submit otomatis jika AJAX gagal -->
<form id="formScanFallback" method="post" action="<?= e(base_url('modules/scan/proses.php')) ?>" class="d-none">
    <?php csrf_field(); ?>
    <input type="hidden" name="kode" id="kodeScanFallback">
</form>

<!-- Modal Konfirmasi Absensi Modern -->
<div class="modal fade attendance-modal" id="modalAttendance" tabindex="-1" aria-labelledby="modalAttendanceTitle" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered modal-sm" style="max-width: 380px;">
        <div class="modal-content text-center p-3">
            <div class="modal-body p-2">
                <!-- Status Icon -->
                <div id="modalResultIcon" class="modal-result-icon success mb-3">
                    <i class="bi bi-check-lg" id="modalResultIconSymbol"></i>
                </div>

                <!-- Judul Hasil Sesuai Arahan -->
                <h3 class="h5 fw-bold mb-1 text-success" id="modalAttendanceTitle">&#10003; Absensi Berhasil</h3>
                <p class="text-secondary small mb-3" id="modalAttendanceSubtitle">Kehadiran tercatat ke sistem</p>

                <!-- Identitas Siswa -->
                <div class="mb-3">
                    <div id="modalAvatarBox" class="mb-2">
                        <div class="student-avatar-placeholder mx-auto" id="modalAvatarPlaceholder">
                            <i class="bi bi-person"></i>
                        </div>
                        <img src="" alt="Foto Siswa" class="student-avatar-lg mx-auto d-none" id="modalAvatarImg">
                    </div>
                    <h4 class="h5 fw-bold mb-1 text-dark" id="modalStudentName">-</h4>
                    <div class="d-flex align-items-center justify-content-center gap-1 mb-1">
                        <span class="badge bg-primary-subtle text-primary border font-monospace" id="modalStudentId">STU001</span>
                        <span class="text-secondary small">&middot;</span>
                        <span class="text-secondary small fw-semibold font-monospace" id="modalStudentNis">NIS: -</span>
                    </div>
                    <p class="text-secondary small mb-0 fw-semibold" id="modalStudentClass">-</p>
                </div>

                <!-- Detail Kartu Info Sesuai Format -->
                <div class="attendance-detail-box mb-3 text-start">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-secondary small">Jam Masuk:</span>
                        <span class="fw-bold small text-dark font-monospace" id="modalScanTime">-</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-secondary small">Status:</span>
                        <span class="badge" id="modalStatusBadge">-</span>
                    </div>
                </div>

                <!-- Tombol Aksi -->
                <div class="d-grid gap-2">
                    <button type="button" class="btn btn-primary fw-semibold" id="btnModalNextScan" data-bs-dismiss="modal">
                        <i class="bi bi-qr-code-scan me-1"></i>Scan Berikutnya
                    </button>
                    <button type="button" class="btn btn-light btn-sm text-secondary" data-bs-dismiss="modal">
                        Tutup
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Informasi Sudah Absen / Peringatan -->
<div class="modal fade attendance-modal" id="modalAttendanceWarning" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered modal-sm" style="max-width: 380px;">
        <div class="modal-content text-center p-3">
            <div class="modal-body p-2">
                <div class="modal-result-icon warning mb-3">
                    <i class="bi bi-info-circle-fill"></i>
                </div>
                <h3 class="h5 fw-bold mb-1 text-dark" id="modalWarningTitle">Absensi hari ini sudah tercatat.</h3>
                <p class="text-secondary small mb-3" id="modalWarningMessage">Siswa telah melakukan presensi sebelumnya hari ini.</p>

                <div class="attendance-detail-box mb-3 text-start">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-secondary small">Student ID:</span>
                        <span class="badge bg-primary-subtle text-primary border font-monospace" id="modalWarningStudentId">STU001</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-secondary small">Nama Siswa:</span>
                        <span class="fw-semibold small text-dark" id="modalWarningName">-</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-secondary small">Kelas:</span>
                        <span class="small text-dark" id="modalWarningClass">-</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-secondary small">Waktu Absen:</span>
                        <span class="fw-bold small text-dark font-monospace" id="modalWarningTime">-</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-secondary small">Status:</span>
                        <span class="badge" id="modalWarningBadge">-</span>
                    </div>
                </div>

                <div class="d-grid">
                    <button type="button" class="btn btn-primary" data-bs-dismiss="modal">
                        Mengerti
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Error / Invalid QR -->
<div class="modal fade attendance-modal" id="modalAttendanceError" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered modal-sm" style="max-width: 360px;">
        <div class="modal-content text-center p-3">
            <div class="modal-body p-2">
                <div class="modal-result-icon danger mb-3">
                    <i class="bi bi-x-circle-fill"></i>
                </div>
                <h3 class="h5 fw-bold mb-1 text-dark">QR Tidak Valid</h3>
                <p class="text-secondary small mb-3" id="modalErrorMessage">
                    QR Code tidak valid atau sudah tidak berlaku.
                </p>
                <div class="d-grid">
                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal">
                        Coba Lagi
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="<?= e(base_url('assets/js/html5-qrcode.min.js')) ?>"></script>
<script>
'use strict';

(function () {
    // CSRF & Konfigurasi
    const csrfToken = <?= json_encode(csrf_token()) ?>;
    const prosesUrl = <?= json_encode(base_url('modules/scan/proses.php')) ?>;
    const sessionHasil = <?= json_encode($hasilSession) ?>;

    // Elemen DOM
    const cameraStatusPill = document.getElementById('cameraStatusPill');
    const cameraStatusText = document.getElementById('cameraStatusText');
    const scannerPromptText = document.getElementById('scannerPromptText');
    const cameraErrorBox = document.getElementById('cameraErrorBox');
    const cameraErrorMessage = document.getElementById('cameraErrorMessage');
    const btnSwitchCamera = document.getElementById('btnSwitchCamera');
    const btnRestartCamera = document.getElementById('btnRestartCamera');
    const btnRetryCamera = document.getElementById('btnRetryCamera');
    const formManual = document.getElementById('formManual');
    const manualKodeInput = document.getElementById('manualKode');
    const btnManualSubmit = document.getElementById('btnManualSubmit');
    const recentScansContainer = document.getElementById('recentScansContainer');

    // Instance Modals
    const modalSuccess = new bootstrap.Modal(document.getElementById('modalAttendance'));
    const modalWarning = new bootstrap.Modal(document.getElementById('modalAttendanceWarning'));
    const modalError = new bootstrap.Modal(document.getElementById('modalAttendanceError'));

    let html5QrCode = null;
    let isProcessing = false;
    let currentCameraId = null;
    let availableCameras = [];
    let isCameraRunning = false;

    // Sumber library scanner: file lokal dulu (jalan tanpa internet/CDN),
    // lalu fallback ke CDN bila file lokal gagal dimuat.
    const scannerLibUrls = [
        <?= json_encode(base_url('assets/js/html5-qrcode.min.js')) ?>,
        'https://unpkg.com/html5-qrcode@2.3.8/html5-qrcode.min.js',
        'https://cdn.jsdelivr.net/npm/html5-qrcode@2.3.8/html5-qrcode.min.js',
    ];

    function loadScannerLib(idx, done) {
        if (typeof Html5Qrcode !== 'undefined') {
            done(true);
            return;
        }
        if (idx >= scannerLibUrls.length) {
            done(false);
            return;
        }
        const el = document.createElement('script');
        el.src = scannerLibUrls[idx];
        el.onload = () => {
            if (typeof Html5Qrcode !== 'undefined') {
                done(true);
            } else {
                loadScannerLib(idx + 1, done);
            }
        };
        el.onerror = () => loadScannerLib(idx + 1, done);
        document.head.appendChild(el);
    }

    // Audio Feedback Generator via Web Audio API (tanpa dependensi file mp3 luar)
    function playAudioTone(type) {
        try {
            const ctx = new (window.AudioContext || window.webkitAudioContext)();
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();
            osc.connect(gain);
            gain.connect(ctx.destination);

            if (type === 'success') {
                // Melodi pendek ceria (D5 -> G5)
                osc.type = 'sine';
                osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
                osc.frequency.setValueAtTime(783.99, ctx.currentTime + 0.09); // G5
                gain.gain.setValueAtTime(0.2, ctx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);
                osc.start(ctx.currentTime);
                osc.stop(ctx.currentTime + 0.35);
            } else if (type === 'warning') {
                // Dua beep pendek datar
                osc.type = 'triangle';
                osc.frequency.setValueAtTime(440, ctx.currentTime);
                gain.gain.setValueAtTime(0.2, ctx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.12);
                osc.start(ctx.currentTime);
                osc.stop(ctx.currentTime + 0.12);
            } else {
                // Nada rendah error
                osc.type = 'sawtooth';
                osc.frequency.setValueAtTime(220, ctx.currentTime);
                gain.gain.setValueAtTime(0.2, ctx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.25);
                osc.start(ctx.currentTime);
                osc.stop(ctx.currentTime + 0.25);
            }
        } catch (e) {
            // Abaikan jika browser membatasi autoplay audio
        }
    }

    // Update Status Pill
    function setCameraStatus(state, text) {
        if (!cameraStatusPill || !cameraStatusText) return;
        cameraStatusText.textContent = text;
        const dot = cameraStatusPill.querySelector('.pulse-dot');
        
        if (state === 'active') {
            cameraStatusPill.className = 'camera-indicator bg-success-subtle text-success border border-success-subtle';
            if (dot) dot.style.backgroundColor = '#059669';
        } else if (state === 'processing') {
            cameraStatusPill.className = 'camera-indicator bg-primary-subtle text-primary border border-primary-subtle';
            if (dot) dot.style.backgroundColor = '#2563eb';
        } else if (state === 'error') {
            cameraStatusPill.className = 'camera-indicator bg-danger-subtle text-danger border border-danger-subtle';
            if (dot) dot.style.backgroundColor = '#dc2626';
        } else {
            cameraStatusPill.className = 'camera-indicator bg-light text-secondary border';
            if (dot) dot.style.backgroundColor = '#94a3b8';
        }
    }

    // Jalankan Scanner Kamera
    function startScanner() {
        if (typeof Html5Qrcode === 'undefined') {
            // Coba muat ulang (lokal -> CDN) sebelum menyerah
            setCameraStatus('loading', 'Memuat Library Scanner...');
            loadScannerLib(0, (ok) => {
                if (ok) {
                    startScanner();
                } else {
                    showCameraError('Library QR scanner gagal dimuat. Periksa koneksi internet atau gunakan tombol "Scan dari Foto" dan input manual NIS di samping.');
                }
            });
            return;
        }

        // Browser memblokir getUserMedia pada konteks tidak aman (HTTP via IP LAN)
        if (!navigator.mediaDevices || typeof navigator.mediaDevices.getUserMedia !== 'function') {
            showCameraError(window.isSecureContext === false
                ? 'Kamera diblokir karena halaman dibuka lewat HTTP (bukan HTTPS/localhost). Gunakan tombol "Scan dari Foto" atau input NIS manual, atau akses aplikasi via https:// / localhost.'
                : 'Browser ini tidak mendukung akses kamera. Gunakan tombol "Scan dari Foto" atau input NIS manual.');
            return;
        }

        cameraErrorBox.classList.add('d-none');
        setCameraStatus('loading', 'Menghubungkan Kamera...');
        scannerPromptText.textContent = 'Meminta izin kamera...';

        if (!html5QrCode) {
            html5QrCode = new Html5Qrcode('qr-reader');
        }

        // Ambil daftar kamera perangkat
        Html5Qrcode.getCameras().then((devices) => {
            if (!devices || devices.length === 0) {
                showCameraError('Tidak ditemukan perangkat kamera pada perangkat ini.');
                return;
            }

            availableCameras = devices;
            if (devices.length > 1) {
                btnSwitchCamera.classList.remove('d-none');
            }

            // Prioritaskan kamera belakang (environment) di ponsel
            const backCamera = devices.find(d => /back|rear|belakang|environment/i.test(d.label));
            currentCameraId = backCamera ? backCamera.id : devices[0].id;

            const config = {
                fps: 12,
                qrbox: (viewWidth, viewHeight) => {
                    const minDim = Math.min(viewWidth, viewHeight);
                    const size = Math.max(200, Math.floor(minDim * 0.75));
                    return { width: size, height: size };
                },
                aspectRatio: 1.0,
            };

            html5QrCode.start(
                currentCameraId,
                config,
                onQrScanSuccess,
                onQrScanError
            ).then(() => {
                isCameraRunning = true;
                setCameraStatus('active', 'Kamera Aktif');
                scannerPromptText.textContent = 'Menunggu QR Code...';
            }).catch((err) => {
                showCameraError('Gagal mengakses kamera: ' + (err.message || err));
            });
        }).catch((err) => {
            showCameraError('Izin akses kamera ditolak atau tidak didukung.');
        });
    }

    // Callback ketika QR berhasil dibaca oleh kamera
    function onQrScanSuccess(decodedText) {
        if (isProcessing) return;
        processAttendance(decodedText.trim());
    }

    function onQrScanError() {
        // Frame kosong atau scan in-progress - normal
    }

    // Tampilkan pesan error kamera
    function showCameraError(msg) {
        isCameraRunning = false;
        setCameraStatus('error', 'Kamera Mati');
        scannerPromptText.textContent = 'Kamera tidak aktif.';
        if (cameraErrorMessage) cameraErrorMessage.textContent = msg;
        cameraErrorBox.classList.remove('d-none');
    }

    // Hentikan kamera
    function stopScanner() {
        if (html5QrCode && isCameraRunning) {
            html5QrCode.stop().then(() => {
                isCameraRunning = false;
                setCameraStatus('inactive', 'Kamera Berhenti');
            }).catch(() => {});
        }
    }

    // Proses Absensi Siswa via AJAX
    function processAttendance(kode) {
        if (!kode) return;
        isProcessing = true;
        setCameraStatus('processing', 'Memvalidasi...');
        scannerPromptText.textContent = 'Memeriksa identitas siswa...';

        // Efek visual laser scanner
        const laser = document.getElementById('scannerLaser');
        if (laser) laser.style.background = '#2563eb';

        fetch(prosesUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'application/json',
                'X-CSRF-TOKEN': csrfToken,
            },
            body: new URLSearchParams({
                '_csrf': csrfToken,
                'kode': kode,
            }),
        })
        .then((res) => {
            if (!res.ok && res.status !== 419) {
                // Lanjut parse json jika tersedia
            }
            return res.json();
        })
        .then((res) => {
            handleAttendanceResponse(res, kode);
        })
        .catch((err) => {
            playAudioTone('error');
            const errTitle = document.querySelector('#modalAttendanceError h3');
            if (errTitle) errTitle.textContent = (!navigator.onLine ? 'Tidak Ada Koneksi' : 'Gangguan Jaringan');
            const errMsg = document.getElementById('modalErrorMessage');
            if (errMsg) {
                errMsg.textContent = !navigator.onLine
                    ? 'Tidak ada koneksi. Absensi belum tercatat.'
                    : 'Terjadi gangguan jaringan saat memproses absensi. Silakan coba lagi.';
            }
            modalError.show();
        });
    }

    let autoResumeTimer = null;

    // Handle Hasil Validasi Absensi
    function handleAttendanceResponse(res, kode) {
        if (autoResumeTimer) {
            clearTimeout(autoResumeTimer);
            autoResumeTimer = null;
        }

        if (res.success) {
            // 1. ABSENSI BERHASIL
            playAudioTone('success');
            const d = res.data;

            const titleEl = document.getElementById('modalAttendanceTitle');
            if (titleEl) titleEl.textContent = '✓ Absensi berhasil';
            document.getElementById('modalAttendanceSubtitle').textContent = 'Kehadiran berhasil dicatat ke sistem';
            document.getElementById('modalResultIcon').className = 'modal-result-icon success mb-3';
            document.getElementById('modalResultIconSymbol').className = 'bi bi-check-lg';

            document.getElementById('modalStudentName').textContent = d.nama || '-';
            const sidEl = document.getElementById('modalStudentId');
            if (sidEl) sidEl.textContent = d.student_id || '-';
            const snisEl = document.getElementById('modalStudentNis');
            if (snisEl) snisEl.textContent = 'NIS: ' + (d.nis || '-');
            document.getElementById('modalStudentClass').textContent = d.kelas || 'X RPL 1';
            document.getElementById('modalScanTime').textContent = d.jam || '-';

            const badge = document.getElementById('modalStatusBadge');
            badge.textContent = d.status || 'Hadir';
            badge.className = 'badge ' + (d.status === 'Terlambat' ? 'badge-terlambat' : 'badge-hadir');

            const avatarImg = document.getElementById('modalAvatarImg');
            const avatarPlaceholder = document.getElementById('modalAvatarPlaceholder');
            if (d.foto) {
                avatarImg.src = d.foto;
                avatarImg.classList.remove('d-none');
                avatarPlaceholder.classList.add('d-none');
            } else {
                avatarImg.classList.add('d-none');
                avatarPlaceholder.classList.remove('d-none');
            }

            modalSuccess.show();
            prependRecentScan(d);

            // Auto-dismiss modal setelah 3 detik agar scanner siap kembali untuk siswa berikutnya
            autoResumeTimer = setTimeout(() => {
                modalSuccess.hide();
            }, 3000);
        } else if (res.code === 'ALREADY_ATTENDED') {
            // 2. SUDAH ABSEN HARI INI
            playAudioTone('warning');
            const d = res.data || {};
            document.getElementById('modalWarningTitle').textContent = 'Absensi hari ini sudah tercatat.';
            document.getElementById('modalWarningMessage').textContent = 'Siswa telah melakukan presensi sebelumnya hari ini.';
            const warnSid = document.getElementById('modalWarningStudentId');
            if (warnSid) warnSid.textContent = d.student_id || '-';
            document.getElementById('modalWarningName').textContent = d.nama || '-';
            document.getElementById('modalWarningClass').textContent = d.kelas || '-';
            document.getElementById('modalWarningTime').textContent = (d.jam || '-') + ' WIB';
            
            const badge = document.getElementById('modalWarningBadge');
            badge.textContent = d.status || 'Sudah Absen';
            badge.className = 'badge ' + (d.status === 'Terlambat' ? 'badge-terlambat' : 'badge-hadir');

            modalWarning.show();

            // Auto-dismiss setelah 3.5 detik
            autoResumeTimer = setTimeout(() => {
                modalWarning.hide();
            }, 3500);
        } else if (res.code === 'STUDENT_INACTIVE') {
            // 3. SISWA TIDAK AKTIF
            playAudioTone('error');
            const errTitle = document.querySelector('#modalAttendanceError h3');
            if (errTitle) errTitle.textContent = 'Siswa Tidak Aktif';
            document.getElementById('modalErrorMessage').textContent = res.pesan || 'Siswa ini berstatus tidak aktif. Silakan hubungi admin atau tata usaha.';
            modalError.show();
        } else {
            // 4. QR TIDAK VALID / ERROR LAIN
            playAudioTone('error');
            const errTitle = document.querySelector('#modalAttendanceError h3');
            if (errTitle) errTitle.textContent = 'QR Tidak Valid';
            document.getElementById('modalErrorMessage').textContent = res.pesan || 'QR Code tidak valid atau sudah tidak berlaku.';
            modalError.show();
        }
    }

    // Tambah item ke daftar Absensi Terkini di UI tanpa reload
    function prependRecentScan(d) {
        if (!recentScansContainer) return;
        const emptyMsg = recentScansContainer.querySelector('.empty-recent-msg');
        if (emptyMsg) emptyMsg.remove();

        const badgeClass = d.status === 'Terlambat' ? 'badge-terlambat' : 'badge-hadir';
        const item = document.createElement('div');
        item.className = 'list-group-item d-flex align-items-center justify-content-between py-2 px-3 bg-light-subtle';
        item.innerHTML = `
            <div class="d-flex align-items-center gap-2">
                ${d.foto ? `<img src="${d.foto}" alt="Foto" width="34" height="34" class="rounded-circle object-fit-cover">` : 
                `<span class="rounded-circle bg-light text-primary d-inline-grid place-items-center" style="width:34px; height:34px;"><i class="bi bi-person"></i></span>`}
                <div>
                    <p class="fw-semibold mb-0 small text-dark d-flex align-items-center gap-1">
                        ${d.student_id ? `<span class="badge bg-primary-subtle text-primary border font-monospace" style="font-size:0.65rem">${d.student_id}</span>` : ''}
                        ${d.nama}
                    </p>
                    <span class="text-secondary" style="font-size:0.75rem">${d.kelas} · ${d.jam}</span>
                </div>
            </div>
            <span class="badge ${badgeClass}">${d.status}</span>
        `;
        recentScansContainer.prepend(item);
    }

    // Lanjutkan Scanner Setelah Modal Ditutup
    function resumeScanner() {
        isProcessing = false;
        if (isCameraRunning) {
            setCameraStatus('active', 'Kamera Aktif');
            scannerPromptText.textContent = 'Menunggu QR Code...';
        }
        const laser = document.getElementById('scannerLaser');
        if (laser) laser.style.background = 'linear-gradient(90deg, transparent, #38bdf8, transparent)';
    }

    // Modal dismiss events
    ['modalAttendance', 'modalAttendanceWarning', 'modalAttendanceError'].forEach((modalId) => {
        const modalEl = document.getElementById(modalId);
        if (modalEl) {
            modalEl.addEventListener('hidden.bs.modal', () => {
                if (autoResumeTimer) {
                    clearTimeout(autoResumeTimer);
                    autoResumeTimer = null;
                }
                if (modalId === 'modalAttendanceError') {
                    const errTitle = document.querySelector('#modalAttendanceError h3');
                    if (errTitle) errTitle.textContent = 'QR Tidak Valid';
                }
                setTimeout(resumeScanner, 400);
            });
        }
    });

    // Form Manual Submit via AJAX
    if (formManual) {
        formManual.addEventListener('submit', (ev) => {
            ev.preventDefault();
            const val = manualKodeInput.value.trim();
            if (!val) return;
            btnManualSubmit.disabled = true;
            btnManualSubmit.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Memproses...';

            processAttendance(val);

            setTimeout(() => {
                btnManualSubmit.disabled = false;
                btnManualSubmit.innerHTML = '<i class="bi bi-check2-circle me-1"></i>Catat';
                manualKodeInput.value = '';
            }, 800);
        });
    }

    // Scan QR dari file gambar (fallback saat kamera tidak tersedia)
    const btnScanFile = document.getElementById('btnScanFile');
    const qrFileInput = document.getElementById('qrFileInput');

    if (btnScanFile && qrFileInput) {
        btnScanFile.addEventListener('click', () => {
            qrFileInput.click();
        });

        qrFileInput.addEventListener('change', (ev) => {
            const file = ev.target.files && ev.target.files[0];
            if (!file) return;

            const runScan = () => {
                if (typeof Html5Qrcode === 'undefined') {
                    loadScannerLib(0, (ok) => {
                        if (ok) {
                            runScan();
                        } else {
                            showFileScanError('Library QR scanner belum siap. Muat ulang halaman atau gunakan input NIS manual.');
                        }
                    });
                    return;
                }

                if (!html5QrCode) {
                    html5QrCode = new Html5Qrcode('qr-reader');
                }

                html5QrCode.scanFile(file, false)
                    .then((decodedText) => {
                        qrFileInput.value = '';
                        processAttendance(String(decodedText).trim());
                    })
                    .catch(() => {
                        qrFileInput.value = '';
                        showFileScanError('QR tidak terbaca dari foto. Pastikan gambar tajam, QR terpotong, dan pencahayaan cukup.');
                    });
            };

            // Hentikan kamera dulu bila sedang aktif agar scanFile tidak bentrok
            if (isCameraRunning && html5QrCode) {
                html5QrCode.stop().then(() => {
                    isCameraRunning = false;
                    runScan();
                }).catch(runScan);
            } else {
                runScan();
            }
        });
    }

    // Pesan error khusus pemindaian file (tidak menyalahkan kamera)
    function showFileScanError(msg) {
        playAudioTone('error');
        const errTitle = document.querySelector('#modalAttendanceError h3');
        if (errTitle) errTitle.textContent = 'Gagal Membaca Foto';
        const errMsg = document.getElementById('modalErrorMessage');
        if (errMsg) errMsg.textContent = msg;
        modalError.show();
    }

    // Tombol Restart Kamera
    if (btnRestartCamera) {
        btnRestartCamera.addEventListener('click', () => {
            stopScanner();
            setTimeout(startScanner, 300);
        });
    }

    if (btnRetryCamera) {
        btnRetryCamera.addEventListener('click', () => {
            stopScanner();
            setTimeout(startScanner, 300);
        });
    }

    // Tombol Ganti Kamera (Depan / Belakang)
    if (btnSwitchCamera) {
        btnSwitchCamera.addEventListener('click', () => {
            if (availableCameras.length <= 1) return;
            const currentIdx = availableCameras.findIndex(d => d.id === currentCameraId);
            const nextIdx = (currentIdx + 1) % availableCameras.length;
            currentCameraId = availableCameras[nextIdx].id;

            stopScanner();
            setTimeout(startScanner, 300);
        });
    }

    // Berhenti saat halaman ditinggalkan
    window.addEventListener('beforeunload', () => {
        stopScanner();
    });

    // Inisialisasi awal
    startScanner();

    // Jika ada session hasil scan (dari fallback submit biasa), tampilkan modalnya
    if (sessionHasil && typeof sessionHasil === 'object') {
        setTimeout(() => {
            handleAttendanceResponse({
                success: !!sessionHasil.ok,
                code: sessionHasil.code || (sessionHasil.ok ? 'SUCCESS' : 'ERROR'),
                pesan: sessionHasil.pesan || '',
                data: sessionHasil,
            }, sessionHasil.nis || '');
        }, 500);
    }
})();
</script>
<?php require ROOT_PATH . '/include/footer.php'; ?>
