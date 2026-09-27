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
$teacher = $isAdmin ? null : current_teacher($pdo, $user);
$dateStart = (string) ($_GET['dari'] ?? date('Y-m-01'));
$dateEnd = (string) ($_GET['sampai'] ?? date('Y-m-d'));
$validDate = static function (string $date): bool {
    $parsed = DateTimeImmutable::createFromFormat('!Y-m-d', $date);
    return $parsed !== false && $parsed->format('Y-m-d') === $date;
};
if (!$validDate($dateStart)) $dateStart = date('Y-m-01');
if (!$validDate($dateEnd)) $dateEnd = date('Y-m-d');
if ($dateStart > $dateEnd) [$dateStart, $dateEnd] = [$dateEnd, $dateStart];
$filterTeacherId = $isAdmin ? (int) ($_GET['teacher_id'] ?? 0) : (int) ($teacher['id'] ?? 0);
$teachers = $isAdmin ? $pdo->query('SELECT id,nama FROM teachers WHERE is_active=1 ORDER BY nama')->fetchAll() : [];

$where = ['x.date BETWEEN :date_start AND :date_end'];
$params = [':date_start' => $dateStart, ':date_end' => $dateEnd];
if ($filterTeacherId > 0) { $where[] = 'x.teacher_id = :teacher_id'; $params[':teacher_id'] = $filterTeacherId; }
elseif (!$isAdmin) { $where[] = 'x.teacher_id = :teacher_id'; $params[':teacher_id'] = 0; }
$sql = 'SELECT x.id,x.date,x.opened_at,x.closed_at,x.status,x.lesson_number,
               t.nama AS nama_guru,c.nama_kelas,sub.nama_mata_pelajaran,
               sc.start_time,sc.end_time,
               COUNT(DISTINCT ar.id) AS students_present,
               (SELECT COUNT(*) FROM students st WHERE st.class_id=x.class_id AND st.is_active=1) AS students_total
        FROM attendance_sessions x
        JOIN schedules sc ON sc.id=x.schedule_id
        JOIN teachers t ON t.id=x.teacher_id
        JOIN classes c ON c.id=x.class_id
        JOIN subjects sub ON sub.id=x.subject_id
        LEFT JOIN attendance_records ar ON ar.session_id=x.id
        WHERE ' . implode(' AND ', $where) . '
        GROUP BY x.id,x.date,x.opened_at,x.closed_at,x.status,x.lesson_number,t.nama,c.nama_kelas,sub.nama_mata_pelajaran,sc.start_time,sc.end_time
        ORDER BY x.date DESC,sc.lesson_number DESC,sc.start_time DESC';
$q = $pdo->prepare($sql);
$q->execute($params);
$rows = $q->fetchAll();

if (($_GET['export'] ?? '') === 'csv') {
    $filename = 'absensi-guru-' . $dateStart . '-' . $dateEnd . '.csv';
    header('Content-Type: text/csv; charset=utf-8');
    header('Content-Disposition: attachment; filename="' . $filename . '"');
    $out = fopen('php://output', 'wb');
    fwrite($out, "\xEF\xBB\xBF");
    fputcsv($out, ['No', 'Tanggal', 'Nama Guru', 'Kelas', 'Mata Pelajaran', 'Les', 'Jadwal', 'Jam Absen Guru', 'Status Sesi', 'Siswa Hadir', 'Jumlah Siswa']);
    foreach ($rows as $index => $row) {
        fputcsv($out, [$index + 1,$row['date'],$row['nama_guru'],$row['nama_kelas'],$row['nama_mata_pelajaran'],$row['lesson_number'],substr((string)$row['start_time'],0,5) . '-' . substr((string)$row['end_time'],0,5),substr((string)$row['opened_at'],11,5),$row['status'],$row['students_present'],$row['students_total']]);
    }
    fclose($out);
    exit;
}

$summary = ['sessions' => count($rows), 'present' => 0, 'classes' => []];
foreach ($rows as $row) { $summary['present'] += (int) $row['students_present']; $summary['classes'][$row['nama_kelas']] = true; }
$query = http_build_query(['dari' => $dateStart, 'sampai' => $dateEnd] + ($isAdmin ? ['teacher_id' => $filterTeacherId] : []));
$pageTitle = 'Absensi Guru';
$activeMenu = 'teacher_attendance';
require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content"><div class="container-fluid p-3 p-md-4">
    <?php display_flash_message(); ?>
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div><h1 class="h3 fw-bold mb-1"><?= $isAdmin ? 'Rekap Absen Guru' : 'Riwayat Absen Mengajar' ?></h1><p class="text-secondary mb-0">Setiap sesi dibuka menjadi catatan kehadiran guru berdasarkan kelas, mata pelajaran, les, dan waktu.</p></div>
        <a class="btn btn-success" href="<?= e(base_url('modules/learning/teacher_attendance.php?' . $query . '&export=csv')) ?>"><i class="bi bi-file-earmark-spreadsheet me-1"></i>Ekspor CSV</a>
    </div>
    <div class="card border-0 shadow-sm mb-3"><div class="card-body"><form class="row g-2 align-items-end" method="get">
        <div class="col-6 col-md-3"><label class="form-label small fw-semibold" for="dari">Dari tanggal</label><input class="form-control" type="date" id="dari" name="dari" value="<?= e($dateStart) ?>"></div>
        <div class="col-6 col-md-3"><label class="form-label small fw-semibold" for="sampai">Sampai tanggal</label><input class="form-control" type="date" id="sampai" name="sampai" value="<?= e($dateEnd) ?>"></div>
        <?php if ($isAdmin): ?><div class="col-12 col-md-4"><label class="form-label small fw-semibold" for="teacher_id">Guru</label><select class="form-select" id="teacher_id" name="teacher_id"><option value="0">Semua guru</option><?php foreach ($teachers as $item): ?><option value="<?= e((string)$item['id']) ?>" <?= $filterTeacherId === (int)$item['id'] ? 'selected' : '' ?>><?= e($item['nama']) ?></option><?php endforeach; ?></select></div><?php endif; ?>
        <div class="col-12 col-md-2"><button class="btn btn-primary w-100"><i class="bi bi-funnel me-1"></i>Tampilkan</button></div>
    </form></div></div>
    <div class="row g-3 mb-3"><div class="col-6 col-lg-4"><div class="card border-0 shadow-sm p-3"><span class="small text-secondary">Sesi mengajar tercatat</span><strong class="fs-3"><?= e((string)$summary['sessions']) ?></strong></div></div><div class="col-6 col-lg-4"><div class="card border-0 shadow-sm p-3"><span class="small text-secondary">Kelas yang dimasuki</span><strong class="fs-3"><?= e((string)count($summary['classes'])) ?></strong></div></div><div class="col-12 col-lg-4"><div class="card border-0 shadow-sm p-3"><span class="small text-secondary">Siswa tercatat pada sesi</span><strong class="fs-3"><?= e((string)$summary['present']) ?></strong></div></div></div>
    <div class="card border-0 shadow-sm"><div class="card-header bg-white d-flex justify-content-between align-items-center"><h2 class="h6 fw-bold mb-0">Detail Kehadiran Per Les</h2><span class="badge bg-light text-secondary border"><?= e($dateStart) ?> s/d <?= e($dateEnd) ?></span></div><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><?php if ($isAdmin): ?><th>Guru</th><?php endif; ?><th>Tanggal</th><th>Jam</th><th>Kelas</th><th>Mata Pelajaran</th><th>Les</th><th>Absen Guru</th><th>Status Sesi</th><th>Siswa Hadir</th></tr></thead><tbody>
        <?php if (!$rows): ?><tr><td colspan="<?= $isAdmin ? 9 : 8 ?>" class="text-center text-secondary py-4">Belum ada sesi mengajar tercatat pada periode ini.</td></tr><?php else: foreach ($rows as $row): ?><tr>
            <?php if ($isAdmin): ?><td><?= e($row['nama_guru']) ?></td><?php endif; ?>
            <td><?= e($row['date']) ?></td><td class="font-monospace"><?= e(substr((string)$row['start_time'],0,5)) ?>–<?= e(substr((string)$row['end_time'],0,5)) ?></td>
            <td class="fw-semibold"><?= e($row['nama_kelas']) ?></td><td><?= e($row['nama_mata_pelajaran']) ?></td><td>Les <?= e((string)$row['lesson_number']) ?></td>
            <td><?= e(substr((string)$row['opened_at'],11,5)) ?> WIB</td><td><span class="badge <?= $row['status'] === 'OPEN' ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary' ?>"><?= e($row['status']) ?></span></td>
            <td><?= e((string)$row['students_present']) ?> / <?= e((string)$row['students_total']) ?></td>
        </tr><?php endforeach; endif; ?>
    </tbody></table></div></div>
</div></main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
