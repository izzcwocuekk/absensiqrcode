package com.example.absensisiswa.data

import com.example.absensisiswa.data.dao.AttendanceDao
import com.example.absensisiswa.data.dao.ClassDao
import com.example.absensisiswa.data.dao.SettingsDao
import com.example.absensisiswa.data.dao.StudentDao
import com.example.absensisiswa.data.dao.UserDao
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
import com.example.absensisiswa.data.model.AttendanceSessionWithDetails
import com.example.absensisiswa.data.model.AttendanceWithDetails
import com.example.absensisiswa.data.model.ScheduleWithDetails
import com.example.absensisiswa.data.model.StudentSessionRecord
import com.example.absensisiswa.data.model.StudentWithClass
import com.example.absensisiswa.util.LocationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class SessionScanResult {
    data class Success(
        val student: StudentEntity,
        val message: String,
        val checkInTime: String
    ) : SessionScanResult()

    data class AlreadyAttended(
        val student: StudentEntity,
        val message: String,
        val checkInTime: String?
    ) : SessionScanResult()

    data class WrongClass(
        val student: StudentEntity,
        val message: String
    ) : SessionScanResult()

    data class NotFound(
        val message: String
    ) : SessionScanResult()

    data class Error(
        val message: String
    ) : SessionScanResult()
}

sealed class ScanResult {
    data class Success(
        val student: StudentWithClass,
        val status: String,
        val jam: String,
        val tanggal: String,
        val message: String,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val accuracy: Float? = null,
        val distanceFromSchool: Float? = null,
        val locationVerified: Boolean = false
    ) : ScanResult()

    data class AlreadyAttended(
        val student: StudentWithClass,
        val status: String,
        val jam: String,
        val tanggal: String,
        val message: String
    ) : ScanResult()

    data class LocationRejected(
        val student: StudentWithClass,
        val message: String,
        val distanceMeters: Float?,
        val allowedRadiusMeters: Float,
        val accuracyMeters: Float?
    ) : ScanResult()

    data class StudentInactive(
        val student: StudentWithClass,
        val message: String
    ) : ScanResult()

    data class NotFound(
        val message: String
    ) : ScanResult()

    data class Error(
        val message: String
    ) : ScanResult()
}

class AttendanceRepository(
    private val classDao: ClassDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val settingsDao: SettingsDao,
    private val userDao: UserDao,
    private val database: AppDatabase
) {
    private val teacherDao = database.teacherDao()
    private val subjectDao = database.subjectDao()
    private val scheduleDao = database.scheduleDao()
    private val sessionDao = database.attendanceSessionDao()
    private val recordDao = database.attendanceRecordDao()

    val allClasses: Flow<List<ClassEntity>> = classDao.getAllClasses()
    val allStudents: Flow<List<StudentWithClass>> = studentDao.getAllStudentsWithClass()
    val settings: Flow<AttendanceSettingsEntity?> = settingsDao.getSettings()

    // Teachers
    val allTeachers: Flow<List<TeacherEntity>> = teacherDao.getAllTeachers()
    suspend fun getTeacherById(id: Long): TeacherEntity? = teacherDao.getTeacherById(id)
    suspend fun getTeacherByUserId(userId: Long): TeacherEntity? = teacherDao.getTeacherByUserId(userId)
    suspend fun insertTeacher(teacher: TeacherEntity): Long = teacherDao.insertTeacher(teacher)
    suspend fun updateTeacher(teacher: TeacherEntity) = teacherDao.updateTeacher(teacher)
    suspend fun deleteTeacher(teacher: TeacherEntity) = teacherDao.deleteTeacher(teacher)

    // Subjects
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    suspend fun getSubjectById(id: Long): SubjectEntity? = subjectDao.getSubjectById(id)
    suspend fun insertSubject(subject: SubjectEntity): Long = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = subjectDao.deleteSubject(subject)

    // Schedules
    val allSchedules: Flow<List<ScheduleWithDetails>> = scheduleDao.getAllSchedulesWithDetails()
    fun getSchedulesByTeacher(teacherId: Long): Flow<List<ScheduleWithDetails>> = scheduleDao.getSchedulesByTeacher(teacherId)
    fun getSchedulesByTeacherAndDay(teacherId: Long, day: String): Flow<List<ScheduleWithDetails>> = scheduleDao.getSchedulesByTeacherAndDay(teacherId, day)
    fun getSchedulesByDay(day: String): Flow<List<ScheduleWithDetails>> = scheduleDao.getSchedulesByDay(day)
    suspend fun getScheduleWithDetailsById(id: Long): ScheduleWithDetails? = scheduleDao.getScheduleWithDetailsById(id)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long = scheduleDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: ScheduleEntity) = scheduleDao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleEntity) = scheduleDao.deleteSchedule(schedule)

    // Attendance Sessions
    val allSessions: Flow<List<AttendanceSessionWithDetails>> = sessionDao.getAllSessionsWithDetails()
    fun getSessionsByDate(date: String): Flow<List<AttendanceSessionWithDetails>> = sessionDao.getSessionsWithDetailsByDate(date)
    fun getSessionsByTeacher(teacherId: Long): Flow<List<AttendanceSessionWithDetails>> = sessionDao.getSessionsWithDetailsByTeacher(teacherId)
    fun getSessionsByTeacherAndDate(teacherId: Long, date: String): Flow<List<AttendanceSessionWithDetails>> = sessionDao.getSessionsWithDetailsByTeacherAndDate(teacherId, date)
    suspend fun getSessionWithDetailsById(id: Long): AttendanceSessionWithDetails? = sessionDao.getSessionWithDetailsById(id)

    /**
     * Opens or retrieves an existing attendance session for a schedule on a specific date.
     * If the session is new, initializes records for all students in the class.
     */
    suspend fun openAttendanceSession(
        scheduleId: Long,
        teacherId: Long,
        classId: Long,
        subjectId: Long,
        date: String,
        lessonNumber: Int,
        startTime: String,
        endTime: String,
        latitude: Double? = null,
        longitude: Double? = null,
        distanceFromSchool: Float? = null,
        locationVerified: Boolean = false,
        notes: String? = null
    ): AttendanceSessionEntity {
        val existing = sessionDao.findSessionByScheduleAndDate(scheduleId, date)
        if (existing != null) {
            // Ensure student records exist
            val currentRecords = recordDao.getRecordsBySessionSync(existing.id)
            if (currentRecords.isEmpty()) {
                initializeStudentRecordsForSession(existing.id, classId)
            }
            return existing
        }

        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newSession = AttendanceSessionEntity(
            scheduleId = scheduleId,
            teacherId = teacherId,
            classId = classId,
            subjectId = subjectId,
            date = date,
            lessonNumber = lessonNumber,
            startTime = startTime,
            endTime = endTime,
            openedAt = nowTime,
            closedAt = null,
            status = "DIBUKA",
            latitude = latitude,
            longitude = longitude,
            distanceFromSchool = distanceFromSchool,
            locationVerified = locationVerified,
            notes = notes
        )
        val sessionId = sessionDao.insertSession(newSession)
        initializeStudentRecordsForSession(sessionId, classId)
        return newSession.copy(id = sessionId)
    }

    private suspend fun initializeStudentRecordsForSession(sessionId: Long, classId: Long) {
        val studentsInClass = studentDao.getStudentsByClassSync(classId)
        val initialRecords = studentsInClass.map { s ->
            AttendanceRecordEntity(
                sessionId = sessionId,
                studentId = s.id,
                status = "ALPA", // default before attendance check
                checkInTime = null,
                notes = null,
                verifiedByQr = false
            )
        }
        recordDao.insertAllRecords(initialRecords)
    }

    suspend fun closeAttendanceSession(sessionId: Long, notes: String? = null) {
        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        sessionDao.closeSession(sessionId, nowTime, notes)
    }

    suspend fun deleteSession(session: AttendanceSessionEntity) {
        recordDao.deleteRecordsBySession(session.id)
        sessionDao.deleteSession(session)
    }

    fun getStudentRecordsForSession(sessionId: Long): Flow<List<StudentSessionRecord>> {
        return recordDao.getRecordsBySession(sessionId).map { records ->
            records.mapNotNull { r ->
                val student = studentDao.getStudentById(r.studentId)
                if (student != null) StudentSessionRecord(student, r) else null
            }
        }
    }

    suspend fun updateStudentRecordStatus(
        sessionId: Long,
        studentId: Long,
        newStatus: String,
        notes: String? = null
    ) {
        val existing = recordDao.getRecordForStudent(sessionId, studentId)
        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        if (existing != null) {
            recordDao.updateRecord(
                existing.copy(
                    status = newStatus,
                    notes = notes ?: existing.notes,
                    checkInTime = if (newStatus == "HADIR") (existing.checkInTime ?: nowTime) else null
                )
            )
        } else {
            recordDao.insertRecord(
                AttendanceRecordEntity(
                    sessionId = sessionId,
                    studentId = studentId,
                    status = newStatus,
                    checkInTime = if (newStatus == "HADIR") nowTime else null,
                    notes = notes
                )
            )
        }
    }

    suspend fun setAllStudentsStatusInSession(sessionId: Long, status: String) {
        val records = recordDao.getRecordsBySessionSync(sessionId)
        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val updated = records.map {
            it.copy(
                status = status,
                checkInTime = if (status == "HADIR") nowTime else null
            )
        }
        recordDao.insertAllRecords(updated)
    }

    /**
     * QR Code validation & attendance check-in for a specific lesson session.
     * Enforces:
     * 1. Student exists
     * 2. Student is active
     * 3. Student belongs to THIS class (prevents cross-class attendance)
     * 4. Student is not already checked in (prevents duplicates)
     */
    suspend fun recordStudentQrInSession(
        sessionId: Long,
        codeOrNis: String,
        latitude: Double? = null,
        longitude: Double? = null,
        distanceFromSchool: Float? = null
    ): SessionScanResult {
        val trimmed = codeOrNis.trim()
        if (trimmed.isEmpty()) {
            return SessionScanResult.Error("Kode QR atau NIS tidak boleh kosong.")
        }

        val session = sessionDao.getSessionById(sessionId)
            ?: return SessionScanResult.Error("Sesi absensi tidak ditemukan.")

        val studentWithClass = studentDao.findByTokenOrNis(trimmed)
            ?: run {
                val asLong = trimmed.toLongOrNull()
                if (asLong != null) studentDao.getStudentWithClassById(asLong) else null
            }

        if (studentWithClass == null) {
            return SessionScanResult.NotFound("QR Code tidak valid atau siswa tidak terdaftar.")
        }

        val student = studentWithClass.student

        // Validate active status
        if (!student.isActive) {
            return SessionScanResult.Error("Siswa ${student.nama} berstatus TIDAK AKTIF.")
        }

        // Validate student belongs to the session's class!
        if (student.classId != session.classId) {
            val targetClass = classDao.getClassById(session.classId)?.namaKelas ?: "Kelas ini"
            val studentClass = studentWithClass.classEntity?.namaKelas ?: "Kelas lain"
            return SessionScanResult.WrongClass(
                student = student,
                message = "${student.nama} terdaftar di $studentClass, bukan di kelas sesi ($targetClass)."
            )
        }

        // Check if student is already HADIR
        val existingRecord = recordDao.getRecordForStudent(sessionId, student.id)
        if (existingRecord != null && existingRecord.status == "HADIR" && existingRecord.checkInTime != null) {
            return SessionScanResult.AlreadyAttended(
                student = student,
                message = "${student.nama} sudah tercatat HADIR pada pukul ${existingRecord.checkInTime}.",
                checkInTime = existingRecord.checkInTime
            )
        }

        // Record attendance as HADIR
        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        if (existingRecord != null) {
            recordDao.updateRecord(
                existingRecord.copy(
                    status = "HADIR",
                    checkInTime = nowTime,
                    verifiedByQr = true,
                    latitude = latitude,
                    longitude = longitude,
                    distanceFromSchool = distanceFromSchool
                )
            )
        } else {
            recordDao.insertRecord(
                AttendanceRecordEntity(
                    sessionId = sessionId,
                    studentId = student.id,
                    status = "HADIR",
                    checkInTime = nowTime,
                    verifiedByQr = true,
                    latitude = latitude,
                    longitude = longitude,
                    distanceFromSchool = distanceFromSchool
                )
            )
        }

        return SessionScanResult.Success(
            student = student,
            message = "Presensi berhasil dicatat: ${student.nama} [HADIR]",
            checkInTime = nowTime
        )
    }

    fun getStudentsByClass(classId: Long): Flow<List<StudentWithClass>> {
        return if (classId <= 0) studentDao.getAllStudentsWithClass() else studentDao.getStudentsByClass(classId)
    }

    fun getAttendancesByDate(tanggal: String): Flow<List<AttendanceWithDetails>> {
        return attendanceDao.getAttendancesByDate(tanggal)
    }

    fun getRecentAttendances(limit: Int = 10): Flow<List<AttendanceWithDetails>> {
        return attendanceDao.getRecentAttendances(limit)
    }

    fun getAttendancesByStudent(studentId: Long): Flow<List<AttendanceWithDetails>> {
        return attendanceDao.getAttendancesByStudent(studentId)
    }

    fun getAttendancesByDateRange(startDate: String, endDate: String): Flow<List<AttendanceWithDetails>> {
        return attendanceDao.getAttendancesByDateRange(startDate, endDate)
    }

    fun getHadirCountForDate(tanggal: String): Flow<Int> = attendanceDao.getHadirCountForDate(tanggal)
    fun getTerlambatCountForDate(tanggal: String): Flow<Int> = attendanceDao.getTerlambatCountForDate(tanggal)
    fun getIzinCountForDate(tanggal: String): Flow<Int> = attendanceDao.getIzinCountForDate(tanggal)
    fun getSakitCountForDate(tanggal: String): Flow<Int> = attendanceDao.getSakitCountForDate(tanggal)
    fun getAlpaCountForDate(tanggal: String): Flow<Int> = attendanceDao.getAlpaCountForDate(tanggal)
    fun getTotalStudentCount(): Flow<Int> = studentDao.getTotalStudentCount()

    suspend fun getStudentById(id: Long): StudentWithClass? {
        return studentDao.getStudentWithClassById(id)
    }

    suspend fun insertClass(classEntity: ClassEntity): Long = classDao.insertClass(classEntity)
    suspend fun updateClass(classEntity: ClassEntity) = classDao.updateClass(classEntity)
    suspend fun deleteClass(classEntity: ClassEntity) = classDao.deleteClass(classEntity)

    suspend fun insertStudent(student: StudentEntity): Long = studentDao.insertStudent(student)
    suspend fun updateStudent(student: StudentEntity) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: StudentEntity) = studentDao.deleteStudent(student)

    suspend fun toggleStudentStatus(studentId: Long) {
        val student = studentDao.getStudentById(studentId) ?: return
        studentDao.updateStudent(student.copy(isActive = !student.isActive))
    }

    suspend fun regenerateQrToken(studentId: Long): String {
        val student = studentDao.getStudentById(studentId) ?: return ""
        val newToken = UUID.randomUUID().toString().replace("-", "")
        studentDao.updateStudent(student.copy(qrToken = newToken))
        return newToken
    }

    suspend fun insertOrUpdateAttendance(attendance: AttendanceEntity): Long {
        return attendanceDao.insertAttendance(attendance)
    }

    suspend fun deleteAttendance(attendance: AttendanceEntity) {
        attendanceDao.deleteAttendance(attendance)
    }

    suspend fun updateSettings(settingsEntity: AttendanceSettingsEntity) {
        settingsDao.updateSettings(settingsEntity)
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun reseedData() {
        DatabaseInitializer.populateInitialData(database)
    }

    /**
     * Preserves original business logic from modules/scan/fungsi_scan.php:
     * - Searches by token / NIS / ID
     * - Validates active status
     * - Checks for duplicate attendance today
     * - Determines Hadir vs Terlambat using late_after setting
     * - Saves attendance record
     */
    suspend fun processScan(
        code: String,
        latitude: Double? = null,
        longitude: Double? = null,
        accuracy: Float? = null,
        distanceFromSchool: Float? = null,
        locationVerified: Boolean = false,
        geofenceStatus: String = "DI_SEKOLAH"
    ): ScanResult {
        val trimmedCode = code.trim()
        if (trimmedCode.isEmpty()) {
            return ScanResult.Error("Kode QR atau NIS tidak boleh kosong.")
        }

        // 1. Search Student
        val studentWithClass = studentDao.findByTokenOrNis(trimmedCode)
            ?: run {
                val asLong = trimmedCode.toLongOrNull()
                if (asLong != null) studentDao.getStudentWithClassById(asLong) else null
            }

        if (studentWithClass == null) {
            return ScanResult.NotFound("QR Code tidak valid atau siswa tidak terdaftar.")
        }

        val student = studentWithClass.student

        // 2. Active status
        if (!student.isActive) {
            return ScanResult.StudentInactive(
                student = studentWithClass,
                message = "Siswa berstatus TIDAK AKTIF. Hubungi operator/admin."
            )
        }

        // 3. Duplicate check for today
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val existingAttendance = attendanceDao.getAttendanceForStudentOnDate(student.id, today)

        if (existingAttendance != null) {
            val formattedTime = existingAttendance.jamMasuk.take(5) + " WIB"
            return ScanResult.AlreadyAttended(
                student = studentWithClass,
                status = existingAttendance.status,
                jam = formattedTime,
                tanggal = today,
                message = "Absensi hari ini sudah tercatat sebelumnya pada jam $formattedTime."
            )
        }

        val settings = settingsDao.getSettingsSync()

        // 4. Geolocation Verification check if required
        val schoolRadius = settings?.schoolRadiusMeters ?: LocationUtils.DEFAULT_SCHOOL_RADIUS_METERS
        var computedDistance = distanceFromSchool
        if (computedDistance == null && latitude != null && longitude != null && settings != null) {
            computedDistance = LocationUtils.calculateDistanceMeters(
                latitude, longitude,
                settings.schoolLatitude, settings.schoolLongitude
            )
        }

        val requireLocation = settings?.requireLocationForAttendance ?: true
        if (requireLocation && computedDistance != null && computedDistance > schoolRadius) {
            return ScanResult.LocationRejected(
                student = studentWithClass,
                message = "Di luar area sekolah: Berjarak ${computedDistance.toInt()} meter (Maksimal ${schoolRadius.toInt()} meter). Anda harus berada di area sekolah untuk melakukan absensi.",
                distanceMeters = computedDistance,
                allowedRadiusMeters = schoolRadius,
                accuracyMeters = accuracy
            )
        }

        // 5. Determine Hadir / Terlambat
        val lateAfter = settings?.lateAfter ?: "07:00:00"
        val status = if (nowTime > lateAfter) "Terlambat" else "Hadir"

        // 6. Save attendance with geolocation audit record
        val newAttendance = AttendanceEntity(
            studentId = student.id,
            tanggal = today,
            jamMasuk = nowTime,
            status = status,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            distanceFromSchool = computedDistance,
            locationVerified = locationVerified || (computedDistance != null && computedDistance <= schoolRadius),
            geofenceStatus = geofenceStatus
        )
        attendanceDao.insertAttendance(newAttendance)

        val jamDisplay = nowTime.take(5) + " WIB"
        val message = if (status == "Terlambat") {
            "Absensi Tercatat: Terlambat pada $jamDisplay (Batas: ${lateAfter.take(5)})."
        } else {
            "Absensi Berhasil: Hadir Tepat Waktu pada $jamDisplay."
        }

        return ScanResult.Success(
            student = studentWithClass,
            status = status,
            jam = jamDisplay,
            tanggal = today,
            message = message,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            distanceFromSchool = computedDistance,
            locationVerified = newAttendance.locationVerified
        )
    }

    fun generateCsvReport(attendances: List<AttendanceWithDetails>, schoolName: String): String {
        val sb = StringBuilder()
        sb.appendLine("LAPORAN REKAPITULASI PRESENSI SISWA")
        sb.appendLine("SEKOLAH: $schoolName")
        sb.appendLine("TANGGAL GENERATE: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
        sb.appendLine()
        sb.appendLine("No,Tanggal,Jam Masuk,NIS,Nama Siswa,Kelas,Jurusan,Status,Keterangan")

        attendances.forEachIndexed { index, item ->
            val no = index + 1
            val tgl = item.attendance.tanggal
            val jam = item.attendance.jamMasuk.take(5)
            val nis = item.studentWithClass?.student?.nis ?: "-"
            val nama = (item.studentWithClass?.student?.nama ?: "-").replace(",", " ")
            val kelas = item.studentWithClass?.classEntity?.namaKelas ?: "-"
            val jurusan = (item.studentWithClass?.classEntity?.jurusan ?: "-").replace(",", " ")
            val status = item.attendance.status
            val ket = (item.attendance.keterangan ?: "-").replace(",", " ").replace("\n", " ")
            sb.appendLine("$no,$tgl,$jam,$nis,\"$nama\",$kelas,\"$jurusan\",$status,\"$ket\"")
        }

        return sb.toString()
    }
}
