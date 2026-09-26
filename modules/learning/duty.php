<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';

require_staff();
$pdo = db();
$user = current_user();
$isAdmin = ($user['role'] ?? '') === 'admin';
$activeMenu = 'duty_roster';
$pageTitle = 'Roster Piket Guru';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_role('admin');
    require_csrf();
    $action = (string) ($_POST['action'] ?? '');
    try {
        if (!duty_roster_ready($pdo)) {
            throw new RuntimeException('Tabel roster piket belum tersedia. Jalankan database/learning_schema.sql terlebih dahulu.');
        }
        if ($action === 'create') {
            $teacherId = (int) ($_POST['teacher_id'] ?? 0);
            $date = trim((string) ($_POST['duty_date'] ?? ''));
            $day = trim((string) ($_POST['day'] ?? ''));
            $start = trim((string) ($_POST['start_time'] ?? ''));
            $end = trim((string) ($_POST['end_time'] ?? ''));
            $location = trim((string) ($_POST['location'] ?? ''));
            $type = trim((string) ($_POST['duty_type'] ?? 'Piket Sekolah')) ?: 'Piket Sekolah';
            $notes = trim((string) ($_POST['notes'] ?? '')) ?: null;
            $validDays = ['Senin','Selasa','Rabu','Kamis','Jumat','Sabtu','Minggu'];
            if ($teacherId < 1 || $start === '' || $end === '' || $location === '' || ($date === '' && !in_array($day, $validDays, true))) {
                throw new RuntimeException('Guru, waktu, lokasi, dan tanggal atau hari wajib diisi.');
            }
            if ($date !== '' && !preg_match('/^\d{4}-\d{2}-\d{2}$/', $date)) throw new RuntimeException('Tanggal piket tidak valid.');
            if ($start >= $end) throw new RuntimeException('Jam selesai harus lebih besar dari jam mulai.');
            $q = $pdo->prepare('INSERT INTO teacher_duty_rosters (teacher_id,duty_date,day,start_time,end_time,location,duty_type,notes) VALUES (?,?,?,?,?,?,?,?)');
            $q->execute([$teacherId, $date !== '' ? $date : null, $date !== '' ? null : $day, $start, $end, $location, $type, $notes]);
            set_flash_message('success', 'Roster piket berhasil ditambahkan.');
        } elseif ($action === 'cancel') {
            $id = (int) ($_POST['id'] ?? 0);
            $q = $pdo->prepare('UPDATE teacher_duty_rosters SET status = "CANCELLED" WHERE id = ?');
            $q->execute([$id]);
            set_flash_message('success', 'Roster piket dibatalkan.');
        }
    } catch (Throwable $e) {
        set_flash_message('danger', $e instanceof PDOException && (int) ($e->errorInfo[1] ?? 0) === 1452 ? 'Guru tidak ditemukan.' : $e->getMessage());
    }
    redirect('modules/learning/duty.php');
}

$filterDate = (string) ($_GET['date'] ?? date('Y-m-d'));
if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $filterDate)) $filterDate = date('Y-m-d');
$filterTeacher = (int) ($_GET['teacher_id'] ?? 0);
$teachers = $isAdmin ? $pdo->query('SELECT id, nama FROM teachers WHERE is_active = 1 ORDER BY nama')->fetchAll() : [];
$teacher = $isAdmin ? null : current_teacher($pdo, $user);
$rows = teacher_duty_rosters($pdo, $isAdmin ? $filterTeacher : (int) ($teacher['id'] ?? 0), $filterDate);

require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content"><div class="container-fluid p-3 p-md-4">
    <?php display_flash_message(); ?>
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div><h1 class="h3 fw-bold mb-1">Roster Piket Guru</h1><p class="text-secondary mb-0">Jadwal piket terpisah dari jadwal mengajar dan sesi absensi.</p></div>
        <span class="badge bg-primary-subtle text-primary border p-2"><i class="bi bi-calendar3 me-1"></i><?= e(date('d M Y', strtotime($filterDate))) ?></span>
    </div>

    <div class="card border-0 shadow-sm mb-3"><div class="card-body"><form class="row g-2 align-items-end" method="get">
        <div class="col-12 col-md-4"><label class="form-label small fw-semibold" for="date">Tanggal</label><input class="form-control" type="date" id="date" name="date" value="<?= e($filterDate) ?>"></div>
        <?php if ($isAdmin): ?><div class="col-12 col-md-5"><label class="form-label small fw-semibold" for="teacher_id">Guru</label><select class="form-select" id="teacher_id" name="teacher_id"><option value="0">Semua Guru</option><?php foreach ($teachers as $item): ?><option value="<?= e((string) $item['id']) ?>" <?= $filterTeacher === (int) $item['id'] ? 'selected' : '' ?>><?= e($item['nama']) ?></option><?php endforeach; ?></select></div><?php endif; ?>
        <div class="col-12 col-md-3"><button class="btn btn-primary w-100"><i class="bi bi-funnel me-1"></i>Tampilkan</button></div>
    </form></div></div>

    <?php if ($isAdmin): ?><div class="card border-0 shadow-sm mb-3"><div class="card-header bg-white"><h2 class="h6 fw-bold mb-0">Tambah Roster Piket</h2></div><div class="card-body"><form class="row g-2" method="post">
        <?php csrf_field(); ?><input type="hidden" name="action" value="create">
        <div class="col-12 col-md-4"><label class="form-label small">Guru</label><select class="form-select" name="teacher_id" required><option value="">Pilih guru</option><?php foreach ($teachers as $item): ?><option value="<?= e((string) $item['id']) ?>"><?= e($item['nama']) ?></option><?php endforeach; ?></select></div>
        <div class="col-6 col-md-3"><label class="form-label small">Tanggal khusus</label><input class="form-control" type="date" name="duty_date"></div>
        <div class="col-6 col-md-3"><label class="form-label small">Atau hari mingguan</label><select class="form-select" name="day"><option value="">Pilih hari</option><?php foreach (['Senin','Selasa','Rabu','Kamis','Jumat','Sabtu','Minggu'] as $day): ?><option><?= e($day) ?></option><?php endforeach; ?></select></div>
        <div class="col-6 col-md-2"><label class="form-label small">Jenis</label><input class="form-control" name="duty_type" value="Piket Sekolah"></div>
        <div class="col-6 col-md-3"><label class="form-label small">Mulai</label><input class="form-control" type="time" name="start_time" required></div>
        <div class="col-6 col-md-3"><label class="form-label small">Selesai</label><input class="form-control" type="time" name="end_time" required></div>
        <div class="col-6 col-md-3"><label class="form-label small">Lokasi</label><input class="form-control" name="location" placeholder="Gerbang utama" required></div>
        <div class="col-12 col-md-3"><label class="form-label small">Catatan</label><input class="form-control" name="notes" placeholder="Opsional"></div>
        <div class="col-12"><button class="btn btn-primary"><i class="bi bi-plus-lg me-1"></i>Simpan Roster</button></div>
    </form></div></div><?php endif; ?>

    <div class="card border-0 shadow-sm"><div class="card-header bg-white d-flex justify-content-between align-items-center"><h2 class="h6 fw-bold mb-0">Jadwal Piket Aktif</h2><span class="badge bg-light text-secondary border"><?= e((string) count($rows)) ?> jadwal</span></div><div class="table-responsive"><table class="table align-middle mb-0"><thead class="table-light"><tr><th>Guru</th><th>Waktu</th><th>Lokasi</th><th>Jenis</th><th>Catatan</th><?php if ($isAdmin): ?><th></th><?php endif; ?></tr></thead><tbody>
    <?php if ($rows === []): ?><tr><td colspan="6" class="text-center text-secondary py-4">Belum ada roster piket untuk tanggal ini.</td></tr><?php else: foreach ($rows as $row): ?><tr><td class="fw-semibold"><?= e($row['nama_guru']) ?></td><td><?= e($row['duty_date'] ?: ('Setiap ' . $row['day'])) ?><br><span class="font-monospace small text-secondary"><?= e(substr($row['start_time'], 0, 5)) ?>–<?= e(substr($row['end_time'], 0, 5)) ?></span></td><td><?= e($row['location']) ?></td><td><span class="badge bg-primary-subtle text-primary"><?= e($row['duty_type']) ?></span></td><td class="small text-secondary"><?= e($row['notes'] ?: '-') ?></td><?php if ($isAdmin): ?><td><form method="post" onsubmit="return confirm('Batalkan roster piket ini?')"><?php csrf_field(); ?><input type="hidden" name="action" value="cancel"><input type="hidden" name="id" value="<?= e((string) $row['id']) ?>"><button class="btn btn-sm btn-outline-danger"><i class="bi bi-x-lg"></i></button></form></td><?php endif; ?></tr><?php endforeach; endif; ?>
    </tbody></table></div></div>
</div></main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
