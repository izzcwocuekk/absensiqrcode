<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';
require_once ROOT_PATH . '/include/qrcode_helper.php';

require_staff();
$pdo = db();
$user = current_user();
$sessionId = (int) ($_GET['id'] ?? 0);
$scheduleId = (int) ($_GET['schedule_id'] ?? 0);
if (!$sessionId && $scheduleId) {
    $q = $pdo->prepare('SELECT id FROM attendance_sessions WHERE schedule_id=? AND date=?');
    $q->execute([$scheduleId, date('Y-m-d')]);
    $sessionId = (int) $q->fetchColumn();
}
$session = $sessionId ? session_detail($pdo, $sessionId, (int) $user['id']) : null;
$schedule = null;
if (!$session && $scheduleId) {
    $q = $pdo->prepare('SELECT sc.*, c.nama_kelas, s.nama_mata_pelajaran FROM schedules sc JOIN classes c ON c.id=sc.class_id JOIN subjects s ON s.id=sc.subject_id WHERE sc.id=?');
    $q->execute([$scheduleId]);
    $schedule = $q->fetch() ?: null;
}
$records = [];
$students = [];
$missingStudents = [];
$summary = ['HADIR' => 0, 'IZIN' => 0, 'SAKIT' => 0, 'ALPA' => 0];
if ($session) {
    $q = $pdo->prepare('SELECT ar.*, s.nis, s.nama FROM attendance_records ar JOIN students s ON s.id=ar.student_id WHERE ar.session_id=? ORDER BY s.nama');
    $q->execute([$sessionId]);
    $records = $q->fetchAll();
    $q = $pdo->prepare('SELECT id, nis, nama FROM students WHERE class_id=? AND is_active=1 ORDER BY nama');
    $q->execute([(int) $session['class_id']]);
    $students = $q->fetchAll();
    foreach ($students as &$student) $student = learning_normalize_student($pdo, $student);
    unset($student);
    $presentIds = array_map('intval', array_column($records, 'student_id'));
    foreach ($students as $student) if (!in_array((int) $student['id'], $presentIds, true)) $missingStudents[] = $student;
    $q = $pdo->prepare('SELECT status,COUNT(*) total FROM attendance_records WHERE session_id=? GROUP BY status');
    $q->execute([$sessionId]);
    foreach ($q->fetchAll() as $stat) $summary[$stat['status']] = (int) $stat['total'];
}
$qrUrl = null;
if ($session && !empty($session['session_token'])) {
    $qrFile = qrcode_ensure_file((string) $session['session_token']);
    $qrUrl = base_url('assets/qrcode/generate/' . basename($qrFile));
}
$pageTitle = 'Absensi Sesi';
$activeMenu = 'scan';
require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content"><div class="container-fluid p-3 p-md-4">
<?php display_flash_message(); ?>
<?php if (!$session): ?>
    <div class="card border-0 shadow-sm"><div class="card-body p-4">
        <h1 class="h4 fw-bold">Buka Sesi Absensi</h1>
        <p class="text-secondary">Sesi membuat QR khusus yang dipindai oleh siswa kelas ini.</p>
        <form id="openForm" method="post" action="<?= e(base_url('modules/learning/open_session.php')) ?>">
            <?php csrf_field(); ?><input type="hidden" name="schedule_id" value="<?= e((string) $scheduleId) ?>"><input type="hidden" name="latitude" id="lat"><input type="hidden" name="longitude" id="lng">
            <div class="alert alert-info small">Jadwal: <strong><?= e($schedule['nama_kelas'] ?? '-') ?></strong> · <?= e($schedule['nama_mata_pelajaran'] ?? '-') ?> · Les <?= e($schedule['lesson_number'] ?? '-') ?></div>
            <button class="btn btn-primary w-100" id="openBtn"><i class="bi bi-qr-code me-1"></i>Buka Absensi & Buat QR Sesi</button>
        </form>
    </div></div>
    <script>
    document.getElementById('openForm').addEventListener('submit', function (event) {
        event.preventDefault(); const form=this, button=document.getElementById('openBtn'); button.disabled=true;
        const submit=()=>fetch(form.action,{method:'POST',headers:{'Accept':'application/json','X-Requested-With':'XMLHttpRequest'},body:new FormData(form)}).then(r=>r.json()).then(x=>{if(x.ok)location.href='<?= e(base_url('modules/learning/session.php')) ?>?id='+x.id;else{alert(x.message||'Sesi gagal dibuka');button.disabled=false;}}).catch(()=>{alert('Lokasi atau jaringan tidak tersedia');button.disabled=false;});
        if(navigator.geolocation)navigator.geolocation.getCurrentPosition(p=>{document.getElementById('lat').value=p.coords.latitude;document.getElementById('lng').value=p.coords.longitude;submit();},()=>submit(),{enableHighAccuracy:true,timeout:7000});else submit();
    });
    </script>
<?php else: ?>
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3"><div><h1 class="h4 fw-bold mb-1"><?= e($session['nama_kelas']) ?> · <?= e($session['nama_mata_pelajaran']) ?></h1><p class="text-secondary mb-0">Les <?= e((string) $session['lesson_number']) ?> · <?= e(substr((string) $session['opened_at'], 11, 5)) ?> WIB · Sesi <?= e($session['status']) ?></p></div><form method="post" action="<?= e(base_url('modules/learning/attendance.php')) ?>"><?php csrf_field(); ?><input type="hidden" name="session_id" value="<?= e((string) $sessionId) ?>"><input type="hidden" name="action" value="close"><button class="btn btn-outline-danger" <?= $session['status'] !== 'OPEN' ? 'disabled' : '' ?>><i class="bi bi-lock me-1"></i>Tutup Sesi</button></form></div>
    <div class="row g-3 mb-3">
        <div class="col-12 col-lg-4"><div class="card border-0 shadow-sm h-100 text-center p-3"><h2 class="h6 fw-bold">QR Sesi Pembelajaran</h2><?php if ($qrUrl): ?><img src="<?= e($qrUrl) ?>" alt="QR sesi absensi" width="220" height="220" class="img-fluid mx-auto mb-2"><div class="small text-secondary">Siswa membuka <strong>Scan QR Sesi</strong>, lalu arahkan kamera ke QR ini.</div><?php else: ?><div class="alert alert-warning small mb-0">Token QR belum tersedia. Jalankan migration learning schema.</div><?php endif; ?><a class="btn btn-sm btn-outline-primary mt-3" href="<?= e(base_url('modules/learning/student_scan.php')) ?>">Buka halaman scan siswa</a></div></div>
        <div class="col-12 col-lg-8"><div class="row g-2"><div class="col-6 col-md-3"><div class="card border-0 shadow-sm text-center p-2"><small>Hadir</small><strong class="text-success fs-4"><?= e((string) $summary['HADIR']) ?></strong></div></div><div class="col-6 col-md-3"><div class="card border-0 shadow-sm text-center p-2"><small>Izin</small><strong class="text-info fs-4"><?= e((string) $summary['IZIN']) ?></strong></div></div><div class="col-6 col-md-3"><div class="card border-0 shadow-sm text-center p-2"><small>Sakit</small><strong class="text-warning fs-4"><?= e((string) $summary['SAKIT']) ?></strong></div></div><div class="col-6 col-md-3"><div class="card border-0 shadow-sm text-center p-2"><small>Alpa</small><strong class="text-danger fs-4"><?= e((string) $summary['ALPA']) ?></strong></div></div></div><div class="card border-0 shadow-sm mt-3"><div class="card-body"><h2 class="h6 fw-bold">Keterangan Sesi</h2><div class="row small"><div class="col-md-4"><span class="text-secondary">Guru</span><br><strong><?= e($session['nama_guru']) ?></strong></div><div class="col-md-4"><span class="text-secondary">Tanggal</span><br><strong><?= e($session['date']) ?></strong></div><div class="col-md-4"><span class="text-secondary">Rentang jam</span><br><strong><?= e(substr((string) ($session['start_time'] ?? ''), 0, 5)) ?>–<?= e(substr((string) ($session['end_time'] ?? ''), 0, 5)) ?></strong></div></div></div></div></div>
    </div>
    <div class="row g-3"><div class="col-12 col-lg-5"><div class="card border-0 shadow-sm"><div class="card-header bg-white"><h2 class="h6 fw-bold mb-0">Scan QR Siswa / Input Kode</h2></div><div class="card-body"><div id="qr-reader" class="mb-3"></div><form id="scanForm" method="post" action="<?= e(base_url('modules/learning/attendance.php')) ?>"><?php csrf_field(); ?><input type="hidden" name="session_id" value="<?= e((string) $sessionId) ?>"><input type="hidden" name="kode" id="kode"><input type="hidden" name="status" value="HADIR"><div class="input-group"><input class="form-control" id="manualCode" placeholder="Token QR siswa / NIS" autocomplete="off"><button class="btn btn-primary">Catat</button></div></form><div id="scanMessage" class="small mt-3"></div></div></div></div><div class="col-12 col-lg-7"><div class="card border-0 shadow-sm"><div class="card-header bg-white d-flex justify-content-between"><h2 class="h6 fw-bold mb-0">Siswa Hadir</h2><span class="badge bg-primary-subtle text-primary" id="count"><?= count($records) ?> / <?= count($students) ?></span></div><div class="table-responsive"><table class="table align-middle mb-0"><thead><tr><th>Siswa</th><th>Status</th><th>Waktu</th></tr></thead><tbody id="records"><tr><td colspan="3" class="text-secondary small">Memuat daftar...</td></tr></tbody></table></div></div></div></div>
    <div class="card border-0 shadow-sm mt-3"><div class="card-header bg-white d-flex justify-content-between"><h2 class="h6 fw-bold mb-0">Daftar Siswa Belum Hadir</h2><span class="badge bg-warning-subtle text-warning-emphasis"><?= count($missingStudents) ?> siswa</span></div><div class="card-body"><div class="row g-2"><?php if (!$missingStudents): ?><div class="col-12 small text-success">Semua siswa sudah tercatat.</div><?php else: foreach ($missingStudents as $student): ?><div class="col-12 col-md-6 col-xl-4"><span class="badge bg-light text-dark border w-100 text-start py-2"><?= e($student['student_id']) ?> · <?= e($student['nama']) ?></span></div><?php endforeach; endif; ?></div></div></div>
    <div class="card border-0 shadow-sm mt-3"><div class="card-header bg-white"><h2 class="h6 fw-bold mb-0">Koreksi Manual</h2></div><div class="card-body"><form class="row g-2 align-items-end" method="post" action="<?= e(base_url('modules/learning/attendance.php')) ?>"><?php csrf_field(); ?><input type="hidden" name="action" value="manual"><input type="hidden" name="session_id" value="<?= e((string) $sessionId) ?>"><div class="col-12 col-md-4"><label class="form-label small">Siswa</label><select class="form-select" name="student_db_id" required><option value="">Pilih siswa</option><?php foreach ($students as $student): ?><option value="<?= e((string) $student['id']) ?>"><?= e($student['nama']) ?> (<?= e($student['student_id']) ?>)</option><?php endforeach; ?></select></div><div class="col-6 col-md-3"><label class="form-label small">Status</label><select class="form-select" name="status"><option>HADIR</option><option>IZIN</option><option>SAKIT</option><option>ALPA</option></select></div><div class="col-6 col-md-3"><label class="form-label small">Catatan</label><input class="form-control" name="notes" placeholder="Opsional"></div><div class="col-12 col-md-2"><button class="btn btn-outline-primary w-100">Simpan</button></div></form></div></div>
    <script src="<?= e(base_url('assets/js/html5-qrcode.min.js')) ?>"></script><script>
    const form=document.getElementById('scanForm'), code=document.getElementById('kode'), manual=document.getElementById('manualCode'), msg=document.getElementById('scanMessage');
    function submitCode(value){if(!value)return;code.value=value;fetch(form.action,{method:'POST',headers:{'Accept':'application/json','X-Requested-With':'XMLHttpRequest'},body:new FormData(form)}).then(r=>r.json()).then(x=>{msg.className='small mt-3 '+(x.ok?'text-success':'text-danger');msg.textContent=x.message||'';if(x.ok)loadRecords();manual.value='';}).catch(()=>{msg.className='small mt-3 text-danger';msg.textContent='Gagal terhubung ke server.';});}
    form.addEventListener('submit',e=>{e.preventDefault();submitCode(manual.value.trim());});
    function loadRecords(){fetch('<?= e(base_url('modules/learning/session_records.php?id=' . $sessionId)) ?>').then(r=>r.json()).then(rows=>{document.getElementById('count').textContent=rows.length+' / <?= count($students) ?>';document.getElementById('records').innerHTML=rows.map(x=>'<tr><td><strong>'+String(x.nama||'').replace(/[<>&]/g,'')+'</strong><br><small class="text-secondary">'+String(x.student_id||'').replace(/[<>&]/g,'')+' · '+String(x.nis||'').replace(/[<>&]/g,'')+'</small></td><td><span class="badge bg-success-subtle text-success">'+String(x.status||'').replace(/[<>&]/g,'')+'</span></td><td>'+((x.check_in_time||'').substring(11,16))+'</td></tr>').join('')||'<tr><td colspan="3" class="text-secondary small">Belum ada catatan.</td></tr>';});} loadRecords();
    if(window.Html5Qrcode){const qr=new Html5Qrcode('qr-reader');qr.start({facingMode:'environment'},{fps:10,qrbox:220},text=>{qr.pause(true);submitCode(text);setTimeout(()=>qr.resume(),1000)},()=>{}).catch(()=>{});}
    </script>
<?php endif; ?></div></main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
