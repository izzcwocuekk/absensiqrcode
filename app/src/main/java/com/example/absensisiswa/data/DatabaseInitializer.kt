package com.example.absensisiswa.data

import com.example.absensisiswa.data.entity.AttendanceEntity
import com.example.absensisiswa.data.entity.AttendanceRecordEntity
import com.example.absensisiswa.data.entity.AttendanceSessionEntity
import com.example.absensisiswa.data.entity.AttendanceSettingsEntity
import com.example.absensisiswa.data.entity.ClassEntity
import com.example.absensisiswa.data.entity.ScheduleEntity
import com.example.absensisiswa.data.entity.StudentEntity
import com.example.absensisiswa.data.entity.SubjectEntity
import com.example.absensisiswa.data.entity.TeacherEntity
import com.example.absensisiswa.data.entity.UserEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatabaseInitializer {

    suspend fun populateInitialData(database: AppDatabase) {
        val classDao = database.classDao()
        val studentDao = database.studentDao()
        val attendanceDao = database.attendanceDao()
        val settingsDao = database.settingsDao()
        val userDao = database.userDao()
        val teacherDao = database.teacherDao()
        val subjectDao = database.subjectDao()
        val scheduleDao = database.scheduleDao()
        val sessionDao = database.attendanceSessionDao()
        val recordDao = database.attendanceRecordDao()

        // 1. Settings
        val defaultSettings = AttendanceSettingsEntity(
            id = 1,
            schoolName = "SMK TRITECH INFORMATIKA MEDAN",
            schoolStartTime = "07:00:00",
            lateAfter = "07:00:00",
            schoolAddress = "Jl. Bhayangkara No. 434, Medan"
        )
        settingsDao.insertSettings(defaultSettings)

        // 2. Classes
        val classes = listOf(
            ClassEntity(id = 1, namaKelas = "X RPL 1", jurusan = "Rekayasa Perangkat Lunak", tingkat = "X"),
            ClassEntity(id = 2, namaKelas = "X RPL 2", jurusan = "Rekayasa Perangkat Lunak", tingkat = "X"),
            ClassEntity(id = 3, namaKelas = "XI RPL 1", jurusan = "Rekayasa Perangkat Lunak", tingkat = "XI"),
            ClassEntity(id = 4, namaKelas = "XII TKJ 1", jurusan = "Teknik Komputer & Jaringan", tingkat = "XII")
        )
        classDao.insertAll(classes)

        // 3. 25 Students
        val students = listOf(
            StudentEntity(id = 1, nis = "R.0400.26", nisn = "0116250064", nama = "ADE VITO MAULANA TANJUNG", classId = 1, jenisKelamin = "L", qrToken = "2c9e936726c32b53058842b5053f241b"),
            StudentEntity(id = 2, nis = "R.0403.26", nisn = "0119555647", nama = "AIRLANGGA RIZKY RAMADHAN", classId = 1, jenisKelamin = "L", qrToken = "c77943d084d9cf55febde57580f743d2"),
            StudentEntity(id = 3, nis = "R.0406.26", nisn = "0118710366", nama = "AKMAL SYAFIQ SYAUQI ALI", classId = 1, jenisKelamin = "L", qrToken = "dd35c07e30df87f8e779f4b12a3684ec"),
            StudentEntity(id = 4, nis = "R.0409.26", nisn = "0112459876", nama = "ALGRIBY WINATA", classId = 1, jenisKelamin = "L", qrToken = "46951e7cf78df23c2c73482c897dcad9"),
            StudentEntity(id = 5, nis = "R.0410.26", nisn = "0114941004", nama = "ALIKA DEA ANANDA", classId = 1, jenisKelamin = "P", qrToken = "da8cfeb63b4ed1ec0e39b65257dd2525"),
            StudentEntity(id = 6, nis = "R.0415.26", nisn = "0114016518", nama = "DAI ZAMZAMI SAID", classId = 1, jenisKelamin = "L", qrToken = "4f964344443ef21f2f81412d9c02ff43"),
            StudentEntity(id = 7, nis = "R.0418.26", nisn = "3118901136", nama = "DECCO AUFAA DZAMAAR", classId = 1, jenisKelamin = "L", qrToken = "f207ea37c94fa2dfc950a3ee41a12002"),
            StudentEntity(id = 8, nis = "R.0419.26", nisn = "0105495936", nama = "DEVIA ANGELICA BR.SILITONGA", classId = 1, jenisKelamin = "P", qrToken = "9599553f1ff7ebf0516fc413009ec5d8"),
            StudentEntity(id = 9, nis = "R.0422.26", nisn = "0014273374", nama = "FAIZ DHABIT HARFANDA MANURUNG", classId = 1, jenisKelamin = "L", qrToken = "6bf33f78e0c8b67eb49a888c3a968fd2"),
            StudentEntity(id = 10, nis = "R.0425.26", nisn = "0114783889", nama = "FIQRI DHIO DINATA", classId = 1, jenisKelamin = "L", qrToken = "933a0172e2cf54b3cc2d21272719a6ee"),
            StudentEntity(id = 11, nis = "R.0429.26", nisn = "0119107648", nama = "HAMDAN SULAIMAN PULUNGAN", classId = 1, jenisKelamin = "L", qrToken = "4aa485800d5a372aeec3b8d6e902b786"),
            StudentEntity(id = 12, nis = "R.0430.26", nisn = "3112330259", nama = "HIRZI ESHA DAMAIS", classId = 1, jenisKelamin = "L", qrToken = "a19bc5a4db52cf14798e9fc78c7b8006"),
            StudentEntity(id = 13, nis = "R.0433.26", nisn = null, nama = "IMAM HANIFAH MARGOLANG", classId = 1, jenisKelamin = "L", qrToken = "1cf7df3567ce3e302968a575e5f0c419"),
            StudentEntity(id = 14, nis = "R.0434.26", nisn = "3117211061", nama = "IRZA", classId = 1, jenisKelamin = "L", qrToken = "6ea4823485f795db2837bc21fbfb9826"),
            StudentEntity(id = 15, nis = "R.0439.26", nisn = "109021787", nama = "M ALIF MAULA", classId = 1, jenisKelamin = "L", qrToken = "40ea9039ddda25df040bf55a2ee04c86"),
            StudentEntity(id = 16, nis = "R.0442.26", nisn = "0111791550", nama = "M.LUTHFI AL WAHIDI", classId = 1, jenisKelamin = "L", qrToken = "02813589b275f0a4f5f54f15d2f6fa72"),
            StudentEntity(id = 17, nis = "R.0446.26", nisn = "0113396354", nama = "MHD. FIQRY HAIKAL", classId = 1, jenisKelamin = "L", qrToken = "b1ff1aa6cb17e88c0378036db772f883"),
            StudentEntity(id = 18, nis = "R.0449.26", nisn = "0115938983", nama = "MHD. IRSYAD ALFATIH", classId = 1, jenisKelamin = "L", qrToken = "2b627cf90bcf8c84d72855146c26bbd9"),
            StudentEntity(id = 19, nis = "R.0453.26", nisn = "0127253784", nama = "MUHAMMAD ATHALLAH PUTRA IRSYI", classId = 1, jenisKelamin = "L", qrToken = "c679fb653fc6aeb8ea8d3b7cf8951838"),
            StudentEntity(id = 20, nis = "R.0455.26", nisn = "0114438347", nama = "MUHAMMAD FACHRURROZI", classId = 1, jenisKelamin = "L", qrToken = "ea4c51480d19273c5d642672bf32d9f4"),
            StudentEntity(id = 21, nis = "R.0460.26", nisn = "3112373899", nama = "NABILA AQILA ARIFAH", classId = 1, jenisKelamin = "P", qrToken = "b7f04126786c4784918e3881dfec38bc"),
            StudentEntity(id = 22, nis = "R.0461.26", nisn = "0119533929", nama = "OKA PAHLEFI", classId = 1, jenisKelamin = "L", qrToken = "4f96d98129df4b4b20a0625345799cb3"),
            StudentEntity(id = 23, nis = "R.0464.26", nisn = "3110252470", nama = "RASYA AL BUQORI", classId = 1, jenisKelamin = "L", qrToken = "b2a1a1f0a149c95d9859f714275cbccf"),
            StudentEntity(id = 24, nis = "R.0467.26", nisn = "0111193618", nama = "RIZKY PRATAMA", classId = 1, jenisKelamin = "L", qrToken = "4441b80c57ffad864ae02213706037eb"),
            StudentEntity(id = 25, nis = "R.0472.26", nisn = "0117360875", nama = "TEGUH HARIYADI", classId = 1, jenisKelamin = "L", qrToken = "dcb8f8b3bc1dfc3330dc743c395bcfe2")
        )
        studentDao.insertAll(students)

        // 4. Teachers
        val teachers = listOf(
            TeacherEntity(
                id = 1,
                userId = 2,
                nama = "Aditya Pratama, S.Kom (Pak Adit)",
                nip = "198804122015031002",
                noHp = "081234567890",
                mataPelajaranUtama = "Informatika & Pemrograman",
                email = "aditya.pratama@tritech.sch.id"
            ),
            TeacherEntity(
                id = 2,
                userId = 4,
                nama = "Siti Rahmawati, M.Pd",
                nip = "199008232018022001",
                noHp = "081398765432",
                mataPelajaranUtama = "Bahasa Indonesia",
                email = "siti.rahmawati@tritech.sch.id"
            ),
            TeacherEntity(
                id = 3,
                userId = 5,
                nama = "Budi Santoso, S.Pd",
                nip = "198501152010011003",
                noHp = "081287654321",
                mataPelajaranUtama = "Matematika",
                email = "budi.santoso@tritech.sch.id"
            ),
            TeacherEntity(
                id = 4,
                userId = 6,
                nama = "Dewi Lestari, S.Kom",
                nip = "199203112019032004",
                noHp = "081976543210",
                mataPelajaranUtama = "Basis Data",
                email = "dewi.lestari@tritech.sch.id"
            )
        )
        teacherDao.insertAll(teachers)

        // 5. Subjects
        val subjects = listOf(
            SubjectEntity(id = 1, kode = "INF", namaMataPelajaran = "Informatika", deskripsi = "Dasar-dasar komputasi, algoritma dan pemikiran komputasional"),
            SubjectEntity(id = 2, kode = "PBO", namaMataPelajaran = "Pemrograman Berorientasi Objek", deskripsi = "Konsep OOP Java, Kotlin dan arsitektur perangkat lunak"),
            SubjectEntity(id = 3, kode = "BIND", namaMataPelajaran = "Bahasa Indonesia", deskripsi = "Tata bahasa, literasi teknis dan komunikasi profesional"),
            SubjectEntity(id = 4, kode = "MTK", namaMataPelajaran = "Matematika", deskripsi = "Logika matematika, aljabar dan statistika terapan"),
            SubjectEntity(id = 5, kode = "BD", namaMataPelajaran = "Basis Data", deskripsi = "Perancangan database relasional, SQL dan optimasi query"),
            SubjectEntity(id = 6, kode = "PWPB", namaMataPelajaran = "Pemrograman Web & Mobile", deskripsi = "Front-end, back-end dan pengembangan aplikasi Android")
        )
        subjectDao.insertAll(subjects)

        // 6. Schedules for every day (so testing works on any day)
        val todayDayName = SimpleDateFormat("EEEE", Locale("id", "ID")).format(Date())
        val daysOfWeek = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

        val schedulesList = mutableListOf<ScheduleEntity>()
        var schedId = 1L

        daysOfWeek.forEach { dayName ->
            // Les 1: 07:00 - 07:45 (Pak Adit - Informatika - X RPL 1)
            schedulesList.add(
                ScheduleEntity(id = schedId++, teacherId = 1, classId = 1, subjectId = 1, day = dayName, startTime = "07:00", endTime = "07:45", lessonNumber = 1, room = "Lab RPL 1")
            )
            // Les 2: 07:45 - 08:30 (Bu Siti - Bahasa Indonesia - X RPL 1)
            schedulesList.add(
                ScheduleEntity(id = schedId++, teacherId = 2, classId = 1, subjectId = 3, day = dayName, startTime = "07:45", endTime = "08:30", lessonNumber = 2, room = "R. 201")
            )
            // Les 3: 08:30 - 09:15 (Pak Adit - PBO - X RPL 2)
            schedulesList.add(
                ScheduleEntity(id = schedId++, teacherId = 1, classId = 2, subjectId = 2, day = dayName, startTime = "08:30", endTime = "09:15", lessonNumber = 3, room = "Lab RPL 2")
            )
            // Les 4: 09:30 - 10:15 (Pak Budi - Matematika - X RPL 1)
            schedulesList.add(
                ScheduleEntity(id = schedId++, teacherId = 3, classId = 1, subjectId = 4, day = dayName, startTime = "09:30", endTime = "10:15", lessonNumber = 4, room = "R. 201")
            )
            // Les 5: 10:15 - 11:00 (Bu Dewi - Basis Data - X RPL 1)
            schedulesList.add(
                ScheduleEntity(id = schedId++, teacherId = 4, classId = 1, subjectId = 5, day = dayName, startTime = "10:15", endTime = "11:00", lessonNumber = 5, room = "Lab RPL 1")
            )
            // Les 6: 11:00 - 11:45 (Pak Adit - PWPB - XI RPL 1)
            schedulesList.add(
                ScheduleEntity(id = schedId++, teacherId = 1, classId = 3, subjectId = 6, day = dayName, startTime = "11:00", endTime = "11:45", lessonNumber = 6, room = "Lab RPL 1")
            )
        }
        scheduleDao.insertAll(schedulesList)

        // 7. Default Users
        val defaultUsers = listOf(
            UserEntity(id = 1, username = "admin", password = "admin123", nama = "Administrator", role = "admin"),
            UserEntity(id = 2, username = "guru", password = "guru123", nama = "Aditya Pratama, S.Kom (Pak Adit)", role = "guru", teacherId = 1),
            UserEntity(id = 3, username = "siswa", password = "siswa123", nama = "FAIZ DHABIT HARFANDA MANURUNG", role = "siswa", studentId = 9),
            UserEntity(id = 4, username = "guru_siti", password = "guru123", nama = "Siti Rahmawati, M.Pd", role = "guru", teacherId = 2),
            UserEntity(id = 5, username = "guru_budi", password = "guru123", nama = "Budi Santoso, S.Pd", role = "guru", teacherId = 3)
        )
        userDao.insertAll(defaultUsers)

        // 8. Seed sample initial demo attendances for today
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sampleAttendances = listOf(
            AttendanceEntity(studentId = 1, tanggal = today, jamMasuk = "06:45:12", status = "Hadir", distanceFromSchool = 25f, locationVerified = true),
            AttendanceEntity(studentId = 2, tanggal = today, jamMasuk = "06:50:33", status = "Hadir", distanceFromSchool = 42f, locationVerified = true),
            AttendanceEntity(studentId = 3, tanggal = today, jamMasuk = "06:55:01", status = "Hadir", distanceFromSchool = 18f, locationVerified = true),
            AttendanceEntity(studentId = 4, tanggal = today, jamMasuk = "07:05:44", status = "Terlambat", keterangan = "Kesiangan motor mogok", distanceFromSchool = 35f, locationVerified = true),
            AttendanceEntity(studentId = 5, tanggal = today, jamMasuk = "07:12:18", status = "Terlambat", keterangan = "Macet gerbang", distanceFromSchool = 60f, locationVerified = true)
        )
        attendanceDao.insertAll(sampleAttendances)

        // 9. Seed a sample completed session and active session
        // Find schedule for today Les 1 (Informatika Pak Adit X RPL 1)
        val todayFirstSchedule = schedulesList.firstOrNull { it.day.equals(todayDayName, ignoreCase = true) && it.lessonNumber == 1 && it.teacherId == 1L }
            ?: schedulesList.first()

        val sampleSession = AttendanceSessionEntity(
            id = 1,
            scheduleId = todayFirstSchedule.id,
            teacherId = 1,
            classId = 1,
            subjectId = 1,
            date = today,
            lessonNumber = 1,
            startTime = "07:00",
            endTime = "07:45",
            openedAt = "07:02:15",
            closedAt = null,
            status = "DIBUKA",
            latitude = 3.606200,
            longitude = 98.697400,
            distanceFromSchool = 15f,
            locationVerified = true,
            notes = "Materi Pengenalan Algoritma"
        )
        sessionDao.insertSession(sampleSession)

        // Seed initial attendance records for all 25 students of X RPL 1 in this session
        val sampleRecords = students.filter { it.classId == 1L }.mapIndexed { index, st ->
            val stStatus = when {
                index < 18 -> "HADIR"
                index == 18 -> "IZIN"
                index == 19 -> "SAKIT"
                else -> "HADIR"
            }
            AttendanceRecordEntity(
                id = 0,
                sessionId = sampleSession.id,
                studentId = st.id,
                status = stStatus,
                checkInTime = if (stStatus == "HADIR") "07:05:%02d".format((index * 2) % 60) else null,
                notes = if (stStatus == "IZIN") "Izin urusan keluarga" else if (stStatus == "SAKIT") "Demam" else null,
                verifiedByQr = index < 15,
                distanceFromSchool = 20f + (index * 2)
            )
        }
        recordDao.insertAllRecords(sampleRecords)
    }
}
