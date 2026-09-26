<?php
declare(strict_types=1);

/**
 * Modul Pengaturan Sistem & Jam Presensi (Admin Only).
 * Konfigurasi nama sekolah, jam masuk, dan batas toleransi terlambat.
 */

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';

require_role('admin');

$pageTitle = 'Pengaturan Sistem';
$activeMenu = 'pengaturan';
$settings = get_attendance_settings();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_csrf();
    $schoolName = trim((string) ($_POST['school_name'] ?? ''));
    $schoolAddress = trim((string) ($_POST['school_address'] ?? ''));
    $startTime = trim((string) ($_POST['school_start_time'] ?? '07:00'));
    $lateAfter = trim((string) ($_POST['late_after'] ?? '07:00'));
    $schoolLat = trim((string) ($_POST['school_latitude'] ?? ''));
    $schoolLng = trim((string) ($_POST['school_longitude'] ?? ''));
    $radius = max(1, (int) ($_POST['location_radius_m'] ?? 150));
    $requireLocation = isset($_POST['require_school_location']) ? 1 : 0;

    if ($schoolName === '') {
        set_flash_message('danger', 'Nama sekolah wajib diisi.');
    } else {
        // Format waktu menjadi HH:MM:00
        $formattedStartTime = strlen($startTime) === 5 ? $startTime . ':00' : $startTime;
        $formattedLateAfter = strlen($lateAfter) === 5 ? $lateAfter . ':00' : $lateAfter;

        try {
            $upd = db()->prepare(
                'UPDATE attendance_settings 
                 SET school_name = :sname, school_address = :saddr, school_start_time = :stime, late_after = :lafter,
                     school_latitude = :lat, school_longitude = :lng, location_radius_m = :radius,
                     require_school_location = :required
                 WHERE id = :id'
            );
            $upd->execute([
                ':sname' => $schoolName,
                ':saddr' => $schoolAddress,
                ':stime' => $formattedStartTime,
                ':lafter' => $formattedLateAfter,
                ':lat' => $schoolLat === '' ? null : (float) $schoolLat,
                ':lng' => $schoolLng === '' ? null : (float) $schoolLng,
                ':radius' => $radius,
                ':required' => $requireLocation,
                ':id' => (int) ($settings['id'] ?? 1),
            ]);

            set_flash_message('success', 'Pengaturan sistem sekolah dan jam presensi berhasil disimpan.');
            redirect('modules/pengaturan/index.php');
        } catch (PDOException $e) {
            set_flash_message('danger', 'Gagal menyimpan pengaturan: ' . $e->getMessage());
        }
    }
}

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content">
    <div class="container-fluid p-3 p-md-4">
        <?php display_flash_message(); ?>

        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
            <div>
                <h1 class="h3 fw-bold mb-1 text-dark">Pengaturan Sistem</h1>
                <p class="text-secondary mb-0">Konfigurasi identitas sekolah dan parameter waktu presensi.</p>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-12 col-lg-8">
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-white border-bottom py-3">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-sliders text-primary fs-5"></i>
                            <h2 class="h6 fw-bold mb-0 text-dark">Identitas Sekolah & Presensi</h2>
                        </div>
                    </div>
                    <div class="card-body p-4">
                        <form method="post" action="<?= e(base_url('modules/pengaturan/index.php')) ?>">
                            <?php csrf_field(); ?>

                            <div class="mb-3">
                                <label class="form-label small fw-semibold text-secondary" for="school_name">Nama Sekolah</label>
                                <input class="form-control" type="text" id="school_name" name="school_name" required
                                    value="<?= e($settings['school_name'] ?? 'SMK TRITECH INFORMATIKA MEDAN') ?>">
                            </div>

                            <div class="mb-3">
                                <label class="form-label small fw-semibold text-secondary" for="school_address">Alamat Sekolah</label>
                                <input class="form-control" type="text" id="school_address" name="school_address"
                                    value="<?= e($settings['school_address'] ?? 'Jl. Bhayangkara No. 434, Medan') ?>">
                            </div>

                            <div class="row g-3 mb-4">
                                <div class="col-12 col-md-6">
                                    <label class="form-label small fw-semibold text-secondary" for="school_start_time">
                                        <i class="bi bi-clock me-1 text-primary"></i>Jam Masuk Sekolah
                                    </label>
                                    <input class="form-control" type="time" id="school_start_time" name="school_start_time" required
                                        value="<?= e(substr((string) ($settings['school_start_time'] ?? '07:00:00'), 0, 5)) ?>">
                                    <small class="text-muted" style="font-size: 0.72rem;">Jam resmi kegiatan belajar mengajar dimulai.</small>
                                </div>

                                <div class="col-12 col-md-6">
                                    <label class="form-label small fw-semibold text-secondary" for="late_after">
                                        <i class="bi bi-alarm me-1 text-warning"></i>Batas Toleransi Terlambat
                                    </label>
                                    <input class="form-control" type="time" id="late_after" name="late_after" required
                                        value="<?= e(substr((string) ($settings['late_after'] ?? '07:00:00'), 0, 5)) ?>">
                                    <small class="text-muted" style="font-size: 0.72rem;">Scan lewat jam ini otomatis berstatus Terlambat.</small>
                                </div>
                            </div>

                            <div class="border rounded-3 p-3 mb-4 bg-light-subtle">
                                <h3 class="h6 fw-bold mb-2"><i class="bi bi-geo-alt me-1 text-primary"></i>Validasi Lokasi Guru</h3>
                                <div class="row g-2">
                                    <div class="col-md-4"><label class="form-label small" for="school_latitude">Latitude</label><input class="form-control" id="school_latitude" name="school_latitude" type="number" step="any" value="<?=e($settings['school_latitude']??'')?>"></div>
                                    <div class="col-md-4"><label class="form-label small" for="school_longitude">Longitude</label><input class="form-control" id="school_longitude" name="school_longitude" type="number" step="any" value="<?=e($settings['school_longitude']??'')?>"></div>
                                    <div class="col-md-4"><label class="form-label small" for="location_radius_m">Radius (meter)</label><input class="form-control" id="location_radius_m" name="location_radius_m" type="number" min="1" value="<?=e($settings['location_radius_m']??150)?>"></div>
                                </div>
                                <div class="form-check mt-2"><input class="form-check-input" type="checkbox" id="require_school_location" name="require_school_location" value="1" <?=!empty($settings['require_school_location'])?'checked':''?>><label class="form-check-label small" for="require_school_location">Wajib berada di dalam radius sekolah saat membuka sesi</label></div>
                            </div>

                            <div class="d-flex justify-content-end">
                                <button class="btn btn-primary fw-semibold px-4" type="submit">
                                    <i class="bi bi-save me-1"></i>Simpan Perubahan
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-12 col-lg-4">
                <div class="card border-0 shadow-sm text-center p-4">
                    <img src="<?= e(base_url('assets/img/logo.png')) ?>" alt="Logo Sekolah" width="90" height="90" class="mx-auto rounded-circle p-1 bg-white border shadow-sm mb-3">
                    <h3 class="h6 fw-bold mb-1 text-dark"><?= e($settings['school_name'] ?? 'SMK TRITECH INFORMATIKA MEDAN') ?></h3>
                    <p class="text-secondary small mb-3"><?= e($settings['school_address'] ?? 'Jl. Bhayangkara No. 434, Medan') ?></p>
                    <div class="alert alert-info py-2 px-3 small text-start mb-0">
                        <i class="bi bi-info-circle-fill me-1"></i>
                        Parameter jam diatur dinamis di database sehingga tidak ada nilai waktu yang di-hardcode di kode aplikasi.
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
