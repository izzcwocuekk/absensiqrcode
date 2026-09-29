<?php
declare(strict_types=1);

$pageTitle = $pageTitle ?? null;
$appSettings = get_attendance_settings();
$schoolName = $appSettings['school_name'] ?? 'SMK TRITECH INFORMATIKA MEDAN';
?>
<!doctype html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <meta name="theme-color" content="#007a3d">
    <meta name="description" content="Sistem Informasi Absensi Siswa Berbasis QR Code - <?= e($schoolName) ?>">
    
    <title><?= e(page_title($pageTitle)) ?></title>

    <!-- Favicon & PWA Icons -->
    <link rel="icon" type="image/png" href="<?= e(base_url('assets/img/logo.png')) ?>">
    <link rel="apple-touch-icon" href="<?= e(base_url('assets/img/logo.png')) ?>">
    <link rel="manifest" href="<?= e(base_url('manifest.json')) ?>">

    <!-- Google Fonts: Plus Jakarta Sans -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 & Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    
    <!-- Custom Style Design System -->
    <link href="<?= e(base_url('assets/css/style.css?v=' . (string) @filemtime(ROOT_PATH . '/assets/css/style.css'))) ?>" rel="stylesheet">
</head>
<body>
