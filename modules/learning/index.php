<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config/config.php';
require_once ROOT_PATH . '/config/database.php';
require_once ROOT_PATH . '/modules/auth/guard.php';
require_once ROOT_PATH . '/modules/learning/functions.php';
require_role('guru');
$pdo=db(); $user=current_user(); $teacher=current_teacher($pdo,$user); $today=date('Y-m-d'); $day=learning_day_name();
$schedules=$teacher?teacher_schedule($pdo,(int)$teacher['id'],$day,$today):[];
$dutyRows=$teacher?teacher_duty_rosters($pdo,(int)$teacher['id'],$today):[];
$pageTitle='Jadwal Mengajar'; $activeMenu='dashboard';
require ROOT_PATH . '/include/header.php'; require ROOT_PATH . '/include/navbar.php'; require ROOT_PATH . '/include/sidebar.php';
?>
<main class="main-content"><div class="container-fluid p-3 p-md-4">
<?php display_flash_message(); ?>
<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4"><div><h1 class="h3 fw-bold mb-1">Selamat datang, <?=e($teacher['nama']??$user['nama'])?></h1><p class="text-secondary mb-0"><?=e($day)?>, <?=e(date('d/m/Y'))?> · Jadwal mengajar hari ini</p></div><span class="badge bg-primary-subtle text-primary border p-2"><i class="bi bi-calendar3 me-1"></i><?=e(date('d M Y'))?></span></div>
<?php if ($dutyRows): ?><div class="card border-0 shadow-sm mb-3"><div class="card-header bg-white d-flex justify-content-between align-items-center"><h2 class="h6 fw-bold mb-0"><i class="bi bi-person-workspace me-1 text-primary"></i>Roster Piket Hari Ini</h2><a class="small text-decoration-none" href="<?=e(base_url('modules/learning/duty.php?date='.$today))?>">Lihat detail</a></div><div class="list-group list-group-flush"><?php foreach ($dutyRows as $duty): ?><div class="list-group-item d-flex flex-wrap justify-content-between align-items-center gap-2"><div><strong class="font-monospace"><?=e(substr($duty['start_time'],0,5))?>–<?=e(substr($duty['end_time'],0,5))?></strong><span class="text-secondary ms-2"><?=e($duty['duty_type'])?></span></div><span class="badge bg-light text-dark border"><i class="bi bi-geo-alt me-1"></i><?=e($duty['location'])?></span></div><?php endforeach; ?></div></div><?php endif; ?>
<div class="card border-0 shadow-sm"><div class="card-header bg-white py-3"><h2 class="h6 fw-bold mb-0">Jadwal Mengajar Hari Ini</h2></div><div class="card-body p-0"><div class="list-group list-group-flush">
<?php if(!$schedules): ?><div class="p-4 text-center text-secondary">Tidak ada jadwal mengajar pada hari ini.</div><?php endif; ?>
<?php foreach($schedules as $row): ?><div class="list-group-item p-3"><div class="d-flex flex-wrap justify-content-between align-items-center gap-3"><div><div class="fw-bold font-monospace text-primary"><?=e(substr($row['start_time'],0,5))?>–<?=e(substr($row['end_time'],0,5))?> <span class="text-secondary fw-normal">· Les <?=e($row['lesson_number'])?></span></div><div class="fs-5 fw-semibold mt-1"><?=e($row['nama_kelas'])?></div><div class="text-secondary"><?=e($row['nama_mata_pelajaran'])?></div></div><div><?php if($row['session_id']): ?><a class="btn btn-outline-primary" href="<?=e(base_url('modules/learning/session.php?id='.$row['session_id']))?>"><i class="bi bi-clipboard-check me-1"></i><?= $row['session_status']==='OPEN'?'Lanjut Absensi':'Lihat Sesi' ?></a><?php else: ?><a class="btn btn-primary" href="<?=e(base_url('modules/learning/session.php?schedule_id='.$row['id']))?>"><i class="bi bi-unlock me-1"></i>Buka Absensi</a><?php endif; ?></div></div></div><?php endforeach; ?>
</div></div></div></div></main>
<?php require ROOT_PATH . '/include/footer.php'; ?>
