<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';

require_role('admin');
$pdo = db();
if (!learning_tables_ready($pdo)) {
    set_flash_message('warning', 'Schema absensi pembelajaran belum diimport. Jalankan database/learning_schema.sql terlebih dahulu.');
    redirect('index.php');
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_csrf();
    $action = (string) ($_POST['action'] ?? '');
    try {
        if ($action === 'teacher') {
            $nama = trim((string) ($_POST['nama'] ?? ''));
            $username = trim((string) ($_POST['username'] ?? ''));
            $nip = trim((string) ($_POST['nip'] ?? '')) ?: null;
            $password = trim((string) ($_POST['password'] ?? '')) ?: 'guru123';
            if ($nama === '' || $username === '') throw new RuntimeException('Nama dan username guru wajib diisi.');
            $pdo->beginTransaction();
            $u = $pdo->prepare('INSERT INTO users (nama,username,email,password,role) VALUES (?,?,?,?,"guru")');
            $u->execute([$nama, $username, $username . '@guru.local', password_hash($password, PASSWORD_DEFAULT)]);
            $pdo->prepare('INSERT INTO teachers (user_id,nama,nip) VALUES (?,?,?)')->execute([(int) $pdo->lastInsertId(), $nama, $nip]);
            $pdo->commit();
        } elseif ($action === 'reset_password') {
            $teacherId = (int) ($_POST['teacher_id'] ?? 0);
            $password = trim((string) ($_POST['password'] ?? ''));
            if ($teacherId < 1 || $password === '') throw new RuntimeException('Guru dan password baru wajib diisi.');
            $q = $pdo->prepare('UPDATE users u JOIN teachers t ON t.user_id=u.id SET u.password=? WHERE t.id=?');
            $q->execute([password_hash($password, PASSWORD_DEFAULT), $teacherId]);
        } elseif ($action === 'subject') {
            $nama = trim((string) ($_POST['nama_mata_pelajaran'] ?? ''));
            if ($nama === '') throw new RuntimeException('Nama mata pelajaran wajib diisi.');
            $pdo->prepare('INSERT INTO subjects (nama_mata_pelajaran) VALUES (?)')->execute([$nama]);
        } elseif ($action === 'schedule') {
            $les = max(1, min(9, (int) ($_POST['lesson_number'] ?? 0)));
            $days = ['Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'];
            $day = trim((string) ($_POST['day'] ?? ''));
            if (!in_array($day, $days, true)) throw new RuntimeException('Hari jadwal tidak valid.');
            $starts = ['07:00:00','07:45:00','08:30:00','09:15:00','10:00:00','10:45:00','11:30:00','12:15:00','13:00:00'];
            $ends = ['07:45:00','08:30:00','09:15:00','10:00:00','10:45:00','11:30:00','12:15:00','13:00:00','13:45:00'];
            $pdo->prepare('INSERT INTO schedules (teacher_id,class_id,subject_id,day,start_time,end_time,lesson_number,source_sheet) VALUES (?,?,?,?,?,?,?,"ADMIN")')->execute([(int) $_POST['teacher_id'], (int) $_POST['class_id'], (int) $_POST['subject_id'], $day, $starts[$les - 1], $ends[$les - 1], $les]);
        } elseif ($action === 'delete_schedule') {
            $id = (int) ($_POST['id'] ?? 0);
            $q = $pdo->prepare('SELECT COUNT(*) FROM attendance_sessions WHERE schedule_id=?'); $q->execute([$id]);
            if ((int) $q->fetchColumn() > 0) throw new RuntimeException('Jadwal yang sudah memiliki sesi tidak dapat dihapus.');
            $pdo->prepare('DELETE FROM schedules WHERE id=?')->execute([$id]);
        } else {
            throw new RuntimeException('Aksi tidak dikenali.');
        }
        set_flash_message('success', 'Data berhasil disimpan.');
    } catch (Throwable $e) {
        if ($pdo->inTransaction()) $pdo->rollBack();
        $duplicate = $e instanceof PDOException && (int) ($e->errorInfo[1] ?? 0) === 1062;
        set_flash_message('danger', $duplicate ? 'Data dengan nilai yang sama sudah ada.' : $e->getMessage());
    }
    redirect('modules/learning/admin.php');
}

$teachers = $pdo->query('SELECT t.id,t.nama,t.nip,t.is_active,u.username,GROUP_CONCAT(DISTINCT subj.nama_mata_pelajaran ORDER BY subj.nama_mata_pelajaran SEPARATOR ", ") AS subjects,GROUP_CONCAT(DISTINCT cls.nama_kelas ORDER BY cls.nama_kelas SEPARATOR ", ") AS classes FROM teachers t LEFT JOIN users u ON u.id=t.user_id LEFT JOIN schedules map_sc ON map_sc.teacher_id=t.id LEFT JOIN subjects subj ON subj.id=map_sc.subject_id LEFT JOIN classes cls ON cls.id=map_sc.class_id GROUP BY t.id,t.nama,t.nip,t.is_active,u.username ORDER BY t.nama')->fetchAll();
$subjects = $pdo->query('SELECT id,nama_mata_pelajaran FROM subjects ORDER BY nama_mata_pelajaran')->fetchAll();
$classes = $pdo->query('SELECT id,nama_kelas FROM classes ORDER BY nama_kelas')->fetchAll();
$schedules = $pdo->query('SELECT sc.id,sc.day,sc.lesson_number,sc.start_time,sc.end_time,sc.source_sheet,t.nama,c.nama_kelas,s.nama_mata_pelajaran FROM schedules sc JOIN teachers t ON t.id=sc.teacher_id JOIN classes c ON c.id=sc.class_id JOIN subjects s ON s.id=sc.subject_id ORDER BY FIELD(sc.day,"Senin","Selasa","Rabu","Kamis","Jumat","Sabtu"),sc.lesson_number,t.nama LIMIT 500')->fetchAll();
$sessionRows = $pdo->query('SELECT x.id,x.date,x.status,x.lesson_number,x.opened_at,t.nama nama_guru,c.nama_kelas,s.nama_mata_pelajaran,COUNT(ar.id) hadir FROM attendance_sessions x JOIN teachers t ON t.id=x.teacher_id JOIN classes c ON c.id=x.class_id JOIN subjects s ON s.id=x.subject_id LEFT JOIN attendance_records ar ON ar.session_id=x.id GROUP BY x.id,x.date,x.status,x.lesson_number,x.opened_at,t.nama,c.nama_kelas,s.nama_mata_pelajaran ORDER BY x.date DESC,x.id DESC LIMIT 50')->fetchAll();
$counts = ['teachers' => count($teachers), 'subjects' => count($subjects), 'classes' => count($classes), 'sessions' => (int) $pdo->query('SELECT COUNT(*) FROM attendance_sessions')->fetchColumn()];
$pageTitle = 'Guru & Jadwal'; $activeMenu = 'learning_admin';
require ROOT_PATH . '/include/header.php'; require ROOT_PATH . '/include/navbar.php'; require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content"><div class="container-fluid p-3 p-md-4">
<?php display_flash_message(); ?>
<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4"><div><h1 class="h3 fw-bold mb-1">Data Guru, Mapel & Roster</h1><p class="text-secondary mb-0">Sumber jadwal dapat berasal dari import roster XLSX atau input manual.</p></div><a class="btn btn-outline-primary" href="<?= e(base_url('modules/laporan/index.php')) ?>"><i class="bi bi-file-earmark-bar-graph me-1"></i>Rekap & Export</a></div>
<div class="row g-3 mb-3"><?php foreach ([['Guru',$counts['teachers'],'people'],['Kelas',$counts['classes'],'diagram-3'],['Mata Pelajaran',$counts['subjects'],'book'],['Sesi Tersimpan',$counts['sessions'],'clipboard-check']] as $card): ?><div class="col-6 col-lg-3"><div class="card border-0 shadow-sm p-3"><div class="text-secondary small"><?= e($card[0]) ?></div><div class="d-flex justify-content-between align-items-end"><strong class="fs-3"><?= e((string) $card[1]) ?></strong><i class="bi bi-<?= e($card[2]) ?> text-primary fs-4"></i></div></div></div><?php endforeach; ?></div>
<div class="alert alert-info small"><strong>Import roster lampiran:</strong> jalankan <code>php database/import_roster.php "C:\Users\odadi\Downloads\Rev.1 - FINAL - ROSTER KELAS - (XII, XI & X) - T.A 2026-2027 (Ganjil) 21-07-2026.xlsx"</code>. Import bersifat idempoten; akun guru baru memakai password awal <code>guru123</code>.</div>
<div class="row g-3"><div class="col-12 col-lg-4"><div class="card border-0 shadow-sm mb-3"><div class="card-header bg-white fw-bold">Tambah Guru</div><div class="card-body"><form method="post"><?php csrf_field(); ?><input type="hidden" name="action" value="teacher"><input class="form-control mb-2" name="nama" placeholder="Nama guru" required><input class="form-control mb-2" name="nip" placeholder="NIP (opsional)"><input class="form-control mb-2" name="username" placeholder="Username" required><input class="form-control mb-3" name="password" placeholder="Password (default guru123)"><button class="btn btn-primary w-100">Simpan Guru</button></form></div></div><div class="card border-0 shadow-sm"><div class="card-header bg-white fw-bold">Tambah Mata Pelajaran</div><div class="card-body"><form method="post"><?php csrf_field(); ?><input type="hidden" name="action" value="subject"><div class="input-group"><input class="form-control" name="nama_mata_pelajaran" placeholder="Nama mata pelajaran" required><button class="btn btn-outline-primary">Tambah</button></div></form></div></div></div>
<div class="col-12 col-lg-8"><div class="card border-0 shadow-sm"><div class="card-header bg-white fw-bold">Tambah Jadwal Manual</div><div class="card-body"><form method="post" class="row g-2"><?php csrf_field(); ?><input type="hidden" name="action" value="schedule"><div class="col-md-6"><select class="form-select" name="teacher_id" required><option value="">Pilih guru</option><?php foreach($teachers as $t): ?><option value="<?= e((string)$t['id']) ?>"><?= e($t['nama']) ?></option><?php endforeach; ?></select></div><div class="col-md-6"><select class="form-select" name="class_id" required><option value="">Pilih kelas</option><?php foreach($classes as $c): ?><option value="<?= e((string)$c['id']) ?>"><?= e($c['nama_kelas']) ?></option><?php endforeach; ?></select></div><div class="col-md-6"><select class="form-select" name="subject_id" required><option value="">Pilih mata pelajaran</option><?php foreach($subjects as $s): ?><option value="<?= e((string)$s['id']) ?>"><?= e($s['nama_mata_pelajaran']) ?></option><?php endforeach; ?></select></div><div class="col-md-3"><select class="form-select" name="day"><?php foreach(['Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'] as $d): ?><option><?= e($d) ?></option><?php endforeach; ?></select></div><div class="col-md-3"><select class="form-select" name="lesson_number"><?php for($i=1;$i<=9;$i++): ?><option value="<?= $i ?>">Les <?= $i ?></option><?php endfor; ?></select></div><div class="col-12"><button class="btn btn-primary">Simpan Jadwal</button></div></form></div></div></div></div>
<div class="card border-0 shadow-sm mt-3"><div class="card-header bg-white fw-bold">Akun Guru dan Penugasan</div><div class="table-responsive"><table class="table align-middle mb-0"><thead class="table-light"><tr><th>Guru</th><th>Username</th><th>Mata Pelajaran</th><th>Kelas</th><th>Reset password</th></tr></thead><tbody><?php foreach($teachers as $t): ?><tr><td><?= e($t['nama']) ?><br><small class="text-secondary"><?= e($t['nip'] ?: 'NIP belum diisi') ?></small></td><td><code><?= e($t['username'] ?? '-') ?></code></td><td class="small"><?= e($t['subjects'] ?: '-') ?></td><td class="small"><?= e($t['classes'] ?: '-') ?></td><td><form method="post" class="d-flex gap-2"><input type="hidden" name="action" value="reset_password"><input type="hidden" name="teacher_id" value="<?= e((string)$t['id']) ?>"><input type="text" class="form-control form-control-sm" name="password" placeholder="Password baru" required><button class="btn btn-sm btn-outline-secondary">Simpan</button><?php csrf_field(); ?></form></td></tr><?php endforeach; ?></tbody></table></div></div>
<div class="card border-0 shadow-sm mt-3"><div class="card-header bg-white fw-bold">Jadwal Terdaftar (<?= count($schedules) ?>)</div><div class="table-responsive"><table class="table table-sm align-middle mb-0"><thead class="table-light"><tr><th>Hari/Les</th><th>Guru</th><th>Kelas</th><th>Mata Pelajaran</th><th></th></tr></thead><tbody><?php foreach($schedules as $sc): ?><tr><td><?= e($sc['day']) ?> · <?= e($sc['lesson_number']) ?><br><small><?= e(substr($sc['start_time'],0,5)) ?>–<?= e(substr($sc['end_time'],0,5)) ?></small></td><td><?= e($sc['nama']) ?></td><td><?= e($sc['nama_kelas']) ?></td><td><?= e($sc['nama_mata_pelajaran']) ?><br><small class="text-secondary"><?= e($sc['source_sheet'] ?: '-') ?></small></td><td><form method="post" onsubmit="return confirm('Hapus jadwal ini?')"><?php csrf_field(); ?><input type="hidden" name="action" value="delete_schedule"><input type="hidden" name="id" value="<?= e((string)$sc['id']) ?>"><button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button></form></td></tr><?php endforeach; ?></tbody></table></div></div>
<div class="card border-0 shadow-sm mt-3"><div class="card-header bg-white fw-bold">Sesi Absensi Terbaru</div><div class="table-responsive"><table class="table table-sm align-middle mb-0"><thead class="table-light"><tr><th>Tanggal</th><th>Guru</th><th>Kelas/Mapel</th><th>Les</th><th>Status</th><th>Hadir</th></tr></thead><tbody><?php foreach($sessionRows as $row): ?><tr><td><?= e($row['date']) ?></td><td><?= e($row['nama_guru']) ?></td><td><?= e($row['nama_kelas']) ?><br><small><?= e($row['nama_mata_pelajaran']) ?></small></td><td><?= e((string)$row['lesson_number']) ?></td><td><?= e($row['status']) ?></td><td><?= e((string)$row['hadir']) ?></td></tr><?php endforeach; if (!$sessionRows): ?><tr><td colspan="6" class="text-center text-secondary py-3">Belum ada sesi.</td></tr><?php endif; ?></tbody></table></div></div>
</div></main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
