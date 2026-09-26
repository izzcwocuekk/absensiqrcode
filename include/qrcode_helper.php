<?php
// Rendering: embed hasil qrcode.php murni PHP + tangan sendiri.
function qrcode_png_gd(object $qr, int $scale, int $margin): string
{
    $n = $qr->getModuleCount();
    $size = ($n + $margin * 2) * $scale;
    $img = imagecreatetruecolor($size, $size);
    $white = imagecolorallocate($img, 255, 255, 255);
    $black = imagecolorallocate($img, 0, 0, 0);
    imagefill($img, 0, 0, $white);
    for ($y = 0; $y < $n; $y++) {
        for ($x = 0; $x < $n; $x++) {
            if ($qr->isDark($y, $x)) {
                imagefilledrectangle(
                    $img,
                    ($x + $margin) * $scale,
                    ($y + $margin) * $scale,
                    ($x + $margin + 1) * $scale - 1,
                    ($y + $margin + 1) * $scale - 1,
                    $black
                );
            }
        }
    }
    ob_start();
    imagepng($img);
    $png = (string) ob_get_clean();
    imagedestroy($img);
    return $png;
}

// Pilihan auto-typeNumber + convert error PHP (trigger_error) jadi exception
// agar bisa coba type lebih besar. Token 32 hex butuh type >= 3 (mode 8-bit,
// ECC M). Loop 1..10; fallback ECC L bila M tidak muat.
function qrcode_make(string $text): object
{
    require_once __DIR__ . '/lib-qrcode/qrcode.php';
    $handler = static function (int $no, string $str): bool {
        throw new RuntimeException($str);
        return true;
    };
    foreach ([QR_ERROR_CORRECT_LEVEL_M, QR_ERROR_CORRECT_LEVEL_L] as $ecc) {
        for ($type = 1; $type <= 10; $type++) {
            set_error_handler($handler);
            try {
                $qr = new QRCode();
                $qr->setTypeNumber($type);
                $qr->setErrorCorrectLevel($ecc);
                $qr->addData($text);
                $qr->make();
                restore_error_handler();
                return $qr;
            } catch (Throwable $e) {
                restore_error_handler();
                continue;
            }
        }
    }
    throw new RuntimeException('Data terlalu panjang untuk QR.');
}

// API dipakai qr.php + generate.php: pastikan file PNG ada (idempoten).
function qrcode_ensure_file(string $token): string
{
    $token = trim($token);
    if ($token === '') {
        throw new RuntimeException('Token QR tidak valid.');
    }
    $safeFile = preg_replace('/[^A-Za-z0-9_-]/', '_', $token) ?? '';
    if ($safeFile === '') {
        throw new RuntimeException('Token QR tidak valid.');
    }
    $dir = ROOT_PATH . '/assets/qrcode/generate';
    if (!is_dir($dir)) {
        mkdir($dir, 0755, true);
    }
    $path = $dir . '/' . $safeFile . '.png';
    if (!is_file($path)) {
        $png = qrcode_png_gd(qrcode_make($token), 6, 2);
        file_put_contents($path, $png);
    }
    return $path;
}
