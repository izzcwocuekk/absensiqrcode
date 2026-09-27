<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';

require_role('siswa');
$student = current_student(db(), current_user() ?? []);
$pageTitle = 'Scan QR Sesi';
$activeMenu = 'student_scan';
require ROOT_PATH . '/include/header.php';
require ROOT_PATH . '/include/navbar.php';
require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content"><div class="container-fluid p-3 p-md-4">
    <?php display_flash_message(); ?>
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4"><div><h1 class="h3 fw-bold mb-1">Scan QR Sesi Pembelajaran</h1><p class="text-secondary mb-0">Gunakan kamera untuk memindai QR yang ditampilkan guru.</p></div><span class="badge bg-primary-subtle text-primary border p-2"><?= e($student['nama_kelas'] ?? 'Kelas belum terhubung') ?></span></div>
    <?php if (!$student): ?>
        <div class="alert alert-warning">Akun ini belum terhubung ke data siswa. Hubungi admin agar username siswa dihubungkan ke NIS atau student_id.</div>
    <?php else: ?>
        <div class="row g-3"><div class="col-12 col-lg-7"><div class="card border-0 shadow-sm"><div class="card-header bg-white"><h2 class="h6 fw-bold mb-0">Kamera</h2></div><div class="card-body"><div id="qr-reader" class="mb-3"></div><div id="result" class="alert d-none" role="status"></div><button type="button" class="btn btn-outline-primary w-100" id="restart">Mulai ulang kamera</button></div></div></div><div class="col-12 col-lg-5"><div class="card border-0 shadow-sm"><div class="card-header bg-white"><h2 class="h6 fw-bold mb-0">Input manual</h2></div><div class="card-body"><p class="small text-secondary">Masukkan token sesi bila kamera tidak tersedia.</p><form id="manualForm"><input class="form-control mb-2" id="manualToken" placeholder="Token QR sesi"><button class="btn btn-primary w-100">Kirim Absensi</button></form><div class="small text-secondary mt-3">Siswa: <strong><?= e($student['nama']) ?></strong><br>Kelas: <strong><?= e($student['nama_kelas'] ?? '-') ?></strong></div></div></div></div></div>
        <form id="csrfForm" class="d-none"><?php csrf_field(); ?></form>
        <script src="<?= e(base_url('assets/js/html5-qrcode.min.js')) ?>"></script><script>
        const result=document.getElementById('result'), token=document.getElementById('manualToken'), csrf=document.querySelector('#csrfForm input[name="_csrf"]'); let busy=false;
        function show(ok,text){result.className='alert '+(ok?'alert-success':'alert-danger');result.textContent=text;result.classList.remove('d-none');}
        function send(value){value=String(value||'').trim();if(!value||busy)return;busy=true;const data=new FormData();data.append('session_token',value);if(csrf)data.append('_csrf',csrf.value);fetch('<?= e(base_url('modules/learning/student_attendance.php')) ?>',{method:'POST',headers:{'Accept':'application/json'},body:data}).then(r=>r.json()).then(x=>{show(!!x.ok,x.message||'Permintaan selesai.');token.value='';}).catch(()=>show(false,'Gagal terhubung ke server.')).finally(()=>{busy=false;});}
        document.getElementById('manualForm').addEventListener('submit',e=>{e.preventDefault();send(token.value);});
        let scanner=null;function start(){if(!window.Html5Qrcode)return;scanner=new Html5Qrcode('qr-reader');scanner.start({facingMode:'environment'},{fps:10,qrbox:240},text=>{scanner.pause(true);send(text);setTimeout(()=>scanner.resume(),1200)},()=>{}).catch(()=>show(false,'Kamera tidak tersedia. Gunakan input manual.'));} document.getElementById('restart').addEventListener('click',()=>{if(scanner)scanner.stop().catch(()=>{}).finally(start);else start();});start();
        </script>
    <?php endif; ?>
</div></main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
