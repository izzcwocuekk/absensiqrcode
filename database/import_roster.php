<?php
declare(strict_types=1);

/**
 * Import roster XLSX ke teachers, classes, subjects, dan schedules.
 * Pemakaian: php database/import_roster.php "C:\\path\\roster.xlsx"
 */
require_once __DIR__ . '/../config/config.php';
require_once ROOT_PATH . '/config/database.php';

$path = $argv[1] ?? '';
if ($path === '' || !is_file($path)) {
    fwrite(STDERR, "File XLSX tidak ditemukan. Pemakaian: php database/import_roster.php <file.xlsx>\n");
    exit(2);
}

$pdo = db();
$pdo->exec(file_get_contents(__DIR__ . '/learning_schema.sql'));

function xlsx_strings(ZipArchive $zip): array
{
    $xml = $zip->getFromName('xl/sharedStrings.xml');
    if ($xml === false) return [];
    $root = simplexml_load_string($xml);
    $out = [];
    foreach ($root->si as $si) {
        $parts = [];
        foreach ($si->xpath('.//*[local-name()="t"]') ?: [] as $t) $parts[] = (string) $t;
        $out[] = implode('', $parts);
    }
    return $out;
}

function xlsx_rows(ZipArchive $zip, string $target, array $shared): array
{
    $xml = $zip->getFromName('xl/' . ltrim($target, '/'));
    if ($xml === false) return [];
    $root = simplexml_load_string($xml);
    $rows = [];
    foreach ($root->xpath('//*[local-name()="sheetData"]/*[local-name()="row"]') ?: [] as $row) {
        $values = [];
        foreach ($row->xpath('./*[local-name()="c"]') ?: [] as $cell) {
            $ref = (string) $cell['r'];
            preg_match('/^([A-Z]+)/', $ref, $m);
            $col = $m[1] ?? '';
            $v = isset($cell->v) ? (string) $cell->v : '';
            if ((string) $cell['t'] === 's' && $v !== '') $v = $shared[(int) $v] ?? '';
            if ((string) $cell['t'] === 'inlineStr') $v = implode('', array_map('strval', $cell->xpath('.//*[local-name()="t"]') ?: []));
            $values[$col] = trim(str_replace("\xc2\xa0", ' ', $v));
        }
        $rows[] = $values;
    }
    return $rows;
}

function excel_col(string $col): int
{
    $n = 0; foreach (str_split($col) as $ch) $n = $n * 26 + ord($ch) - 64; return $n;
}

function normalize_name(string $name): string
{
    $name = preg_replace('/\s*\(\d+\)\s*$/', '', trim($name)) ?? trim($name);
    return preg_replace('/\s+/', ' ', $name) ?? $name;
}

function slug_username(string $name): string
{
    $s = strtolower(normalize_name($name));
    $s = preg_replace('/[^a-z0-9]+/', '.', $s) ?? 'guru';
    return trim($s, '.') ?: 'guru';
}

function roster_class_name(string $grade, string $code): string
{
    // Kode R1/R2/R3 pada roster adalah rombel RPL. Samakan dengan nama
    // kelas yang sudah dipakai aplikasi lama agar data siswa tetap terhubung.
    if (preg_match('/^R([1-3])$/', $code, $m)) return $grade . ' RPL ' . $m[1];
    return $grade . ' ' . $code;
}

$zip = new ZipArchive();
if ($zip->open($path) !== true) throw new RuntimeException('XLSX tidak dapat dibuka.');
$shared = xlsx_strings($zip);
$workbook = simplexml_load_string((string) $zip->getFromName('xl/workbook.xml'));
$rels = simplexml_load_string((string) $zip->getFromName('xl/_rels/workbook.xml.rels'));
$relMap = [];
foreach ($rels->Relationship as $rel) $relMap[(string) $rel['Id']] = (string) $rel['Target'];

$days = ['Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'];
$dayBases = [7,17,27,37,47,54];
$slotStart = ['07:00:00','07:45:00','08:30:00','09:15:00','10:00:00','10:45:00','11:30:00','12:15:00','13:00:00'];
$slotEnd   = ['07:45:00','08:30:00','09:15:00','10:00:00','10:45:00','11:30:00','12:15:00','13:00:00','13:45:00'];
$teacherStmt = $pdo->prepare('SELECT id FROM teachers WHERE nama = ? LIMIT 1');
$classStmt = $pdo->prepare('SELECT id FROM classes WHERE nama_kelas = ? LIMIT 1');
$subjectStmt = $pdo->prepare('SELECT id FROM subjects WHERE nama_mata_pelajaran = ? LIMIT 1');
$scheduleStmt = $pdo->prepare('INSERT IGNORE INTO schedules (teacher_id,class_id,subject_id,day,start_time,end_time,lesson_number,source_sheet) VALUES (?,?,?,?,?,?,?,?)');
$count = ['teachers'=>0,'classes'=>0,'subjects'=>0,'schedules'=>0];

foreach ($workbook->sheets->sheet as $sheet) {
    $sheetName = (string) $sheet['name'];
    $rid = (string) $sheet->attributes('r', true)->id;
    $rows = xlsx_rows($zip, $relMap[$rid] ?? '', $shared);
    $subject = '';
    foreach ($rows as $row) {
        $teacher = normalize_name((string) ($row['C'] ?? ''));
        $subjectCell = trim((string) ($row['E'] ?? ''));
        $grade = trim((string) ($row['F'] ?? ''));
        if ($teacher === '' || $teacher === 'MATA PELAJARAN UMUM' || $teacher === 'MATA PELAJARAN KEJURUAN') continue;
        if ($subjectCell !== '' && !in_array(strtoupper($subjectCell), ['MATA PELAJARAN UMUM','MATA PELAJARAN KEJURUAN','MATA PELAJARAN PILIHAN','MUATAN LOKAL'], true)) $subject = preg_replace('/\s+/', ' ', str_replace("\n", ' ', $subjectCell)) ?? $subjectCell;
        if ($subject === '' || $grade === '' || !in_array($grade, ['X','XI','XII'], true)) continue;
        $teacherStmt->execute([$teacher]); $teacherId = (int) ($teacherStmt->fetchColumn() ?: 0);
        if ($teacherId === 0) {
            $username = slug_username($teacher); $base = $username; $i = 2;
            while (true) { $q = $pdo->prepare('SELECT id FROM users WHERE username = ?'); $q->execute([$username]); if (!$q->fetchColumn()) break; $username = $base . $i++; }
            $u = $pdo->prepare('INSERT INTO users (nama,username,email,password,role) VALUES (?,?,?,?,\'guru\')');
            $u->execute([$teacher,$username,$username.'@guru.local',password_hash('guru123', PASSWORD_DEFAULT)]);
            $teacherInsert = $pdo->prepare('INSERT INTO teachers (user_id,nama) VALUES (?,?)'); $teacherInsert->execute([(int)$pdo->lastInsertId(),$teacher]);
            $teacherId = (int) $pdo->lastInsertId(); $count['teachers']++;
        }
        $subjectStmt->execute([$subject]); $subjectId = (int) ($subjectStmt->fetchColumn() ?: 0);
        if ($subjectId === 0) { $s = $pdo->prepare('INSERT INTO subjects (nama_mata_pelajaran) VALUES (?)'); $s->execute([$subject]); $subjectId = (int) $pdo->lastInsertId(); $count['subjects']++; }
        for ($idx=0; $idx<6; $idx++) {
            $base = $dayBases[$idx]; // G:O, Q:Y, AA:AI, AK:AS, AU:BC, BB:BJ (grid separators are skipped)
            for ($lesson=1; $lesson<=9; $lesson++) {
                $colNum = $base + $lesson - 1;
                $letters=''; for($n=$colNum;$n>0;$n=intdiv($n-1,26)) $letters=chr(65+(($n-1)%26)).$letters;
                $code = trim((string) ($row[$letters] ?? ''));
                if ($code === '') continue;
                $className = roster_class_name($grade, $code);
                $classStmt->execute([$className]); $classId = (int) ($classStmt->fetchColumn() ?: 0);
                if ($classId === 0) { $c=$pdo->prepare('INSERT INTO classes (nama_kelas,jurusan,tingkat) VALUES (?,?,?)'); $c->execute([$className,'Roster '.$code,$grade]); $classId=(int)$pdo->lastInsertId(); $count['classes']++; }
                $scheduleStmt->execute([$teacherId,$classId,$subjectId,$days[$idx],$slotStart[$lesson-1],$slotEnd[$lesson-1],$lesson,$sheetName]);
                $count['schedules'] += $scheduleStmt->rowCount();
            }
        }
    }
}
$zip->close();
// Akun demo guru lama tetap dapat membuka jadwal pertama untuk instalasi cepat.
$firstTeacher = (int) $pdo->query('SELECT id FROM teachers ORDER BY id LIMIT 1')->fetchColumn();
$guruUser = (int) $pdo->query("SELECT id FROM users WHERE username='guru' LIMIT 1")->fetchColumn();
if ($firstTeacher && $guruUser) {
    // Akun demo guru adalah pintu masuk kompatibilitas instalasi lama.
    $pdo->prepare('UPDATE teachers SET user_id=NULL WHERE user_id=?')->execute([$guruUser]);
    $pdo->prepare('UPDATE teachers SET user_id=? WHERE id=?')->execute([$guruUser,$firstTeacher]);
}
echo json_encode($count, JSON_PRETTY_PRINT|JSON_UNESCAPED_UNICODE) . PHP_EOL;
