package com.example.absensisiswa.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.absensisiswa.data.AttendanceRepository
import com.example.absensisiswa.data.ScanResult
import com.example.absensisiswa.data.SessionScanResult
import com.example.absensisiswa.data.entity.AttendanceEntity
import com.example.absensisiswa.data.entity.AttendanceSettingsEntity
import com.example.absensisiswa.data.entity.AttendanceSessionEntity
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
import com.example.absensisiswa.util.LocationVerificationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppScreen {
    DASHBOARD,
    SCAN_QR,
    DATA_SISWA,
    QR_SISWA,
    DATA_KELAS,
    PRESENSI,
    LAPORAN,
    PENGATURAN,
    PROFIL,
    SESI_ABSENSI,
    JADWAL_PELAJARAN,
    DATA_GURU,
    DATA_MAPEL
}

data class DashboardStats(
    val totalStudents: Int = 0,
    val hadir: Int = 0,
    val terlambat: Int = 0,
    val izin: Int = 0,
    val sakit: Int = 0,
    val alpa: Int = 0,
    val attendancePercentage: Float = 0f
)

class MainViewModel(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayDayName: String = SimpleDateFormat("EEEE", Locale("id", "ID")).format(Date())

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow(
        UserEntity(
            id = 1,
            username = "admin",
            password = "admin123",
            nama = "Administrator",
            role = "admin"
        )
    )
    val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

    val settings: StateFlow<AttendanceSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allClasses: StateFlow<List<ClassEntity>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<StudentWithClass>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<TeacherEntity>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<ScheduleWithDetails>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<AttendanceSessionWithDetails>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAttendances: StateFlow<List<AttendanceWithDetails>> = repository.getRecentAttendances(15)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active attendance session management
    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSessionWithDetails: StateFlow<AttendanceSessionWithDetails?> = _activeSessionId
        .flatMapLatest { id ->
            if (id != null) {
                repository.allSessions.map { list -> list.firstOrNull { it.session.id == id } }
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSessionStudentRecords: StateFlow<List<StudentSessionRecord>> = _activeSessionId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getStudentRecordsForSession(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sessionScanResult = MutableStateFlow<SessionScanResult?>(null)
    val sessionScanResult: StateFlow<SessionScanResult?> = _sessionScanResult.asStateFlow()

    private val _isVerifyingSessionLocation = MutableStateFlow(false)
    val isVerifyingSessionLocation: StateFlow<Boolean> = _isVerifyingSessionLocation.asStateFlow()

    private val _sessionLocationResult = MutableStateFlow<LocationVerificationResult?>(null)
    val sessionLocationResult: StateFlow<LocationVerificationResult?> = _sessionLocationResult.asStateFlow()

    private val _selectedScheduleDay = MutableStateFlow(todayDayName)
    val selectedScheduleDay: StateFlow<String> = _selectedScheduleDay.asStateFlow()

    // Dashboard Statistics Flow
    val dashboardStats: StateFlow<DashboardStats> = combine(
        repository.getTotalStudentCount(),
        repository.getHadirCountForDate(todayString),
        repository.getTerlambatCountForDate(todayString),
        repository.getIzinCountForDate(todayString),
        repository.getSakitCountForDate(todayString),
        repository.getAlpaCountForDate(todayString)
    ) { flows: Array<Int> ->
        val total = flows[0]
        val hadir = flows[1]
        val terlambat = flows[2]
        val izin = flows[3]
        val sakit = flows[4]
        val alpa = flows[5]
        val totalRecorded = hadir + terlambat
        val pct = if (total > 0) ((totalRecorded.toFloat() / total.toFloat()) * 100f).coerceIn(0f, 100f) else 0f
        DashboardStats(
            totalStudents = total,
            hadir = hadir,
            terlambat = terlambat,
            izin = izin,
            sakit = sakit,
            alpa = alpa,
            attendancePercentage = pct
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Scanner state
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    private val _isScanningActive = MutableStateFlow(true)
    val isScanningActive: StateFlow<Boolean> = _isScanningActive.asStateFlow()

    // Geolocation Verification state
    private val _locationResult = MutableStateFlow<LocationVerificationResult?>(null)
    val locationResult: StateFlow<LocationVerificationResult?> = _locationResult.asStateFlow()

    private val _isCheckingLocation = MutableStateFlow(false)
    val isCheckingLocation: StateFlow<Boolean> = _isCheckingLocation.asStateFlow()

    // Filters & Selection
    private val _selectedClassFilter = MutableStateFlow<Long>(0)
    val selectedClassFilter: StateFlow<Long> = _selectedClassFilter.asStateFlow()

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _selectedPresensiDate = MutableStateFlow(todayString)
    val selectedPresensiDate: StateFlow<String> = _selectedPresensiDate.asStateFlow()

    private val _presensiClassFilter = MutableStateFlow<Long>(0)
    val presensiClassFilter: StateFlow<Long> = _presensiClassFilter.asStateFlow()

    // Dialog / Sheet states
    private val _studentCardToShow = MutableStateFlow<StudentWithClass?>(null)
    val studentCardToShow: StateFlow<StudentWithClass?> = _studentCardToShow.asStateFlow()

    private val _studentToEdit = MutableStateFlow<StudentEntity?>(null)
    val studentToEdit: StateFlow<StudentEntity?> = _studentToEdit.asStateFlow()
    val isStudentDialogOpen = MutableStateFlow(false)

    private val _classToEdit = MutableStateFlow<ClassEntity?>(null)
    val classToEdit: StateFlow<ClassEntity?> = _classToEdit.asStateFlow()
    val isClassDialogOpen = MutableStateFlow(false)

    private val _attendanceToEdit = MutableStateFlow<AttendanceWithDetails?>(null)
    val attendanceToEdit: StateFlow<AttendanceWithDetails?> = _attendanceToEdit.asStateFlow()

    val isManualInputDialogOpen = MutableStateFlow(false)

    // Snackbar / Feedback message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun setSelectedClassFilter(classId: Long) {
        _selectedClassFilter.value = classId
    }

    fun setPresensiDate(date: String) {
        _selectedPresensiDate.value = date
    }

    fun setPresensiClassFilter(classId: Long) {
        _presensiClassFilter.value = classId
    }

    fun showStudentCard(student: StudentWithClass?) {
        _studentCardToShow.value = student
    }

    fun openAddStudentDialog() {
        _studentToEdit.value = null
        isStudentDialogOpen.value = true
    }

    fun openEditStudentDialog(student: StudentEntity) {
        _studentToEdit.value = student
        isStudentDialogOpen.value = true
    }

    fun closeStudentDialog() {
        isStudentDialogOpen.value = false
        _studentToEdit.value = null
    }

    fun openAddClassDialog() {
        _classToEdit.value = null
        isClassDialogOpen.value = true
    }

    fun openEditClassDialog(classEntity: ClassEntity) {
        _classToEdit.value = classEntity
        isClassDialogOpen.value = true
    }

    fun closeClassDialog() {
        isClassDialogOpen.value = false
        _classToEdit.value = null
    }

    fun openEditAttendanceDialog(attendance: AttendanceWithDetails) {
        _attendanceToEdit.value = attendance
    }

    fun closeEditAttendanceDialog() {
        _attendanceToEdit.value = null
    }

    fun dismissScanResult() {
        _scanResult.value = null
        _isScanningActive.value = true
    }

    fun clearScanResult() {
        _scanResult.value = null
    }

    fun resumeScanning() {
        _isScanningActive.value = true
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun verifyLocation(context: Context, forceRefresh: Boolean = false) {
        if (_isCheckingLocation.value && !forceRefresh) return
        _isCheckingLocation.value = true

        viewModelScope.launch {
            val currSettings = settings.value ?: AttendanceSettingsEntity()
            val result = LocationUtils.verifyAttendanceLocation(
                context = context,
                schoolLat = currSettings.schoolLatitude,
                schoolLon = currSettings.schoolLongitude,
                schoolRadiusMeters = currSettings.schoolRadiusMeters,
                maxAccuracyMeters = currSettings.maxGpsAccuracyMeters,
                simulatedLocationMode = currSettings.simulationModeEnabled,
                simulateInsideSchool = true
            )
            _locationResult.value = result
            _isCheckingLocation.value = false
        }
    }

    fun clearLocationResult() {
        _locationResult.value = null
    }

    fun processBarcode(code: String) {
        if (!_isScanningActive.value) return
        _isScanningActive.value = false

        viewModelScope.launch {
            val loc = _locationResult.value
            val result = repository.processScan(
                code = code,
                latitude = loc?.latitude,
                longitude = loc?.longitude,
                accuracy = loc?.accuracyMeters,
                distanceFromSchool = loc?.distanceFromSchoolMeters,
                locationVerified = loc?.isSuccess == true,
                geofenceStatus = loc?.geofenceStatus?.name ?: "DI_SEKOLAH"
            )
            _scanResult.value = result
        }
    }

    fun recordCurrentStudentAttendance(context: Context) {
        val user = _currentUser.value
        val stId = user.studentId
        if (stId != null) {
            val studentWithClass = allStudents.value.firstOrNull { it.student.id == stId }
            val code = studentWithClass?.student?.qrToken ?: studentWithClass?.student?.nis ?: stId.toString()
            
            viewModelScope.launch {
                val currSettings = settings.value ?: AttendanceSettingsEntity()
                // Verify fresh location
                val loc = _locationResult.value ?: LocationUtils.verifyAttendanceLocation(
                    context = context,
                    schoolLat = currSettings.schoolLatitude,
                    schoolLon = currSettings.schoolLongitude,
                    schoolRadiusMeters = currSettings.schoolRadiusMeters,
                    maxAccuracyMeters = currSettings.maxGpsAccuracyMeters,
                    simulatedLocationMode = currSettings.simulationModeEnabled,
                    simulateInsideSchool = true
                )
                _locationResult.value = loc

                val result = repository.processScan(
                    code = code,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracy = loc.accuracyMeters,
                    distanceFromSchool = loc.distanceFromSchoolMeters,
                    locationVerified = loc.isSuccess,
                    geofenceStatus = loc.geofenceStatus.name
                )
                _scanResult.value = result
            }
        } else {
            _snackbarMessage.value = "Hanya akun siswa yang dapat melakukan absensi mandiri."
        }
    }

    fun saveStudent(
        id: Long,
        nis: String,
        nisn: String?,
        nama: String,
        classId: Long,
        jenisKelamin: String,
        existingQrToken: String?
    ) {
        viewModelScope.launch {
            val token = existingQrToken?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString().replace("-", "")
            val student = StudentEntity(
                id = id,
                nis = nis.trim(),
                nisn = nisn?.trim()?.takeIf { it.isNotBlank() },
                nama = nama.trim(),
                classId = classId,
                jenisKelamin = jenisKelamin,
                qrToken = token,
                isActive = true
            )
            if (id == 0L) {
                repository.insertStudent(student)
                _snackbarMessage.value = "Siswa ${student.nama} berhasil ditambahkan"
            } else {
                repository.updateStudent(student)
                _snackbarMessage.value = "Data siswa ${student.nama} berhasil diperbarui"
            }
            closeStudentDialog()
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _snackbarMessage.value = "Siswa ${student.nama} berhasil dihapus"
        }
    }

    fun toggleStudentActive(studentId: Long) {
        viewModelScope.launch {
            repository.toggleStudentStatus(studentId)
            _snackbarMessage.value = "Status keaktifan siswa berhasil diubah"
        }
    }

    fun regenerateQrToken(studentId: Long) {
        viewModelScope.launch {
            repository.regenerateQrToken(studentId)
            _snackbarMessage.value = "Token QR berhasil digenerate ulang"
        }
    }

    fun saveClass(
        id: Long,
        namaKelas: String,
        jurusan: String,
        tingkat: String
    ) {
        viewModelScope.launch {
            val classEntity = ClassEntity(
                id = id,
                namaKelas = namaKelas.trim(),
                jurusan = jurusan.trim(),
                tingkat = tingkat.trim()
            )
            if (id == 0L) {
                repository.insertClass(classEntity)
                _snackbarMessage.value = "Kelas ${classEntity.namaKelas} berhasil ditambahkan"
            } else {
                repository.updateClass(classEntity)
                _snackbarMessage.value = "Kelas ${classEntity.namaKelas} berhasil diperbarui"
            }
            closeClassDialog()
        }
    }

    fun deleteClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            try {
                repository.deleteClass(classEntity)
                _snackbarMessage.value = "Kelas ${classEntity.namaKelas} berhasil dihapus"
            } catch (e: Exception) {
                _snackbarMessage.value = "Gagal menghapus: kelas masih memiliki siswa"
            }
        }
    }

    fun saveAttendanceStatus(
        attendance: AttendanceEntity,
        newStatus: String,
        keterangan: String?
    ) {
        viewModelScope.launch {
            val updated = attendance.copy(
                status = newStatus,
                keterangan = keterangan?.trim()?.takeIf { it.isNotBlank() }
            )
            repository.insertOrUpdateAttendance(updated)
            closeEditAttendanceDialog()
            _snackbarMessage.value = "Presensi berhasil diperbarui menjadi $newStatus"
        }
    }

    fun deleteAttendance(attendance: AttendanceEntity) {
        viewModelScope.launch {
            repository.deleteAttendance(attendance)
            closeEditAttendanceDialog()
            _snackbarMessage.value = "Catatan presensi berhasil dihapus"
        }
    }

    fun saveAttendance(
        studentId: Long,
        tanggal: String,
        status: String,
        jamMasuk: String?,
        keterangan: String?
    ) {
        viewModelScope.launch {
            val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val entity = AttendanceEntity(
                studentId = studentId,
                tanggal = tanggal,
                status = status,
                jamMasuk = jamMasuk ?: nowTime,
                keterangan = keterangan?.trim()?.takeIf { it.isNotBlank() }
            )
            repository.insertOrUpdateAttendance(entity)
            _snackbarMessage.value = "Presensi siswa berhasil dicatat"
        }
    }

    fun updateSettings(
        schoolName: String,
        lateAfter: String,
        schoolAddress: String,
        schoolStartTime: String = "07:15:00",
        schoolLatitude: Double = LocationUtils.DEFAULT_SCHOOL_LAT,
        schoolLongitude: Double = LocationUtils.DEFAULT_SCHOOL_LON,
        schoolRadiusMeters: Float = LocationUtils.DEFAULT_SCHOOL_RADIUS_METERS,
        maxGpsAccuracyMeters: Float = LocationUtils.DEFAULT_MAX_ACCURACY_METERS,
        requireLocationForAttendance: Boolean = true,
        simulationModeEnabled: Boolean = false
    ) {
        viewModelScope.launch {
            val current = settings.value ?: AttendanceSettingsEntity()
            val updated = current.copy(
                schoolName = schoolName.trim(),
                lateAfter = lateAfter.trim(),
                schoolAddress = schoolAddress.trim(),
                schoolStartTime = schoolStartTime.trim(),
                schoolLatitude = schoolLatitude,
                schoolLongitude = schoolLongitude,
                schoolRadiusMeters = schoolRadiusMeters,
                maxGpsAccuracyMeters = maxGpsAccuracyMeters,
                requireLocationForAttendance = requireLocationForAttendance,
                simulationModeEnabled = simulationModeEnabled
            )
            repository.updateSettings(updated)
            _snackbarMessage.value = "Pengaturan presensi & geolocation berhasil disimpan"
        }
    }

    fun setSelectedScheduleDay(day: String) {
        _selectedScheduleDay.value = day
    }

    fun openSession(schedule: ScheduleWithDetails, context: Context) {
        viewModelScope.launch {
            _isVerifyingSessionLocation.value = true
            val currSettings = settings.value ?: AttendanceSettingsEntity()
            val loc = LocationUtils.verifyAttendanceLocation(
                context = context,
                schoolLat = currSettings.schoolLatitude,
                schoolLon = currSettings.schoolLongitude,
                schoolRadiusMeters = currSettings.schoolRadiusMeters,
                maxAccuracyMeters = currSettings.maxGpsAccuracyMeters,
                simulatedLocationMode = currSettings.simulationModeEnabled,
                simulateInsideSchool = true
            )
            _sessionLocationResult.value = loc
            _isVerifyingSessionLocation.value = false

            if (currSettings.requireLocationForAttendance && !loc.isSuccess) {
                val distMeters = loc.distanceFromSchoolMeters?.toInt() ?: 0
                _snackbarMessage.value = "Gagal membuka sesi: Lokasi guru di luar area sekolah (${distMeters}m). Maksimal radius: ${currSettings.schoolRadiusMeters.toInt()}m."
                return@launch
            }

            val session = repository.openAttendanceSession(
                scheduleId = schedule.schedule.id,
                teacherId = schedule.schedule.teacherId,
                classId = schedule.schedule.classId,
                subjectId = schedule.schedule.subjectId,
                date = todayString,
                lessonNumber = schedule.schedule.lessonNumber,
                startTime = schedule.schedule.startTime,
                endTime = schedule.schedule.endTime,
                latitude = loc.latitude,
                longitude = loc.longitude,
                distanceFromSchool = loc.distanceFromSchoolMeters,
                locationVerified = loc.isSuccess,
                notes = "Sesi ${schedule.subject?.namaMataPelajaran ?: "Pelajaran"} - ${schedule.classEntity?.namaKelas ?: ""}"
            )
            _activeSessionId.value = session.id
            _currentScreen.value = AppScreen.SESI_ABSENSI
            _snackbarMessage.value = "Sesi ${schedule.subject?.namaMataPelajaran ?: ""} berhasil dibuka! (Lokasi terverifikasi ✓)"
        }
    }

    fun selectSession(sessionId: Long) {
        _activeSessionId.value = sessionId
        _currentScreen.value = AppScreen.SESI_ABSENSI
    }

    fun closeActiveSession(notes: String? = null) {
        val sId = _activeSessionId.value ?: return
        viewModelScope.launch {
            repository.closeAttendanceSession(sId, notes)
            _snackbarMessage.value = "Sesi absensi berhasil disimpan dan ditutup."
        }
    }

    fun updateStudentRecordStatus(studentId: Long, status: String, notes: String? = null) {
        val sId = _activeSessionId.value ?: return
        viewModelScope.launch {
            repository.updateStudentRecordStatus(sId, studentId, status, notes)
        }
    }

    fun setAllStudentsInActiveSession(status: String) {
        val sId = _activeSessionId.value ?: return
        viewModelScope.launch {
            repository.setAllStudentsStatusInSession(sId, status)
            _snackbarMessage.value = "Semua siswa diatur ke status $status"
        }
    }

    fun scanStudentQrInActiveSession(code: String) {
        val sId = _activeSessionId.value ?: return
        viewModelScope.launch {
            val loc = _sessionLocationResult.value
            val res = repository.recordStudentQrInSession(
                sessionId = sId,
                codeOrNis = code,
                latitude = loc?.latitude,
                longitude = loc?.longitude,
                distanceFromSchool = loc?.distanceFromSchoolMeters
            )
            _sessionScanResult.value = res
        }
    }

    fun dismissSessionScanResult() {
        _sessionScanResult.value = null
    }

    fun saveTeacher(
        id: Long,
        nama: String,
        nip: String?,
        noHp: String?,
        mataPelajaranUtama: String?,
        email: String?
    ) {
        viewModelScope.launch {
            val teacher = TeacherEntity(
                id = id,
                nama = nama.trim(),
                nip = nip?.trim()?.takeIf { it.isNotBlank() },
                noHp = noHp?.trim()?.takeIf { it.isNotBlank() },
                mataPelajaranUtama = mataPelajaranUtama?.trim()?.takeIf { it.isNotBlank() },
                email = email?.trim()?.takeIf { it.isNotBlank() }
            )
            if (id == 0L) {
                repository.insertTeacher(teacher)
                _snackbarMessage.value = "Guru ${teacher.nama} berhasil ditambahkan"
            } else {
                repository.updateTeacher(teacher)
                _snackbarMessage.value = "Data guru ${teacher.nama} berhasil diperbarui"
            }
        }
    }

    fun deleteTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            try {
                repository.deleteTeacher(teacher)
                _snackbarMessage.value = "Guru ${teacher.nama} berhasil dihapus"
            } catch (e: Exception) {
                _snackbarMessage.value = "Gagal menghapus: guru terkait jadwal aktif"
            }
        }
    }

    fun saveSubject(
        id: Long,
        kode: String,
        nama: String,
        deskripsi: String?
    ) {
        viewModelScope.launch {
            val subject = SubjectEntity(
                id = id,
                kode = kode.trim().uppercase(),
                namaMataPelajaran = nama.trim(),
                deskripsi = deskripsi?.trim()?.takeIf { it.isNotBlank() }
            )
            if (id == 0L) {
                repository.insertSubject(subject)
                _snackbarMessage.value = "Mata pelajaran ${subject.namaMataPelajaran} berhasil ditambahkan"
            } else {
                repository.updateSubject(subject)
                _snackbarMessage.value = "Mata pelajaran ${subject.namaMataPelajaran} diperbarui"
            }
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            try {
                repository.deleteSubject(subject)
                _snackbarMessage.value = "Mata pelajaran ${subject.namaMataPelajaran} berhasil dihapus"
            } catch (e: Exception) {
                _snackbarMessage.value = "Gagal menghapus: mapel terkait jadwal aktif"
            }
        }
    }

    fun saveSchedule(
        id: Long,
        teacherId: Long,
        classId: Long,
        subjectId: Long,
        day: String,
        startTime: String,
        endTime: String,
        lessonNumber: Int,
        room: String
    ) {
        viewModelScope.launch {
            val schedule = ScheduleEntity(
                id = id,
                teacherId = teacherId,
                classId = classId,
                subjectId = subjectId,
                day = day,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                lessonNumber = lessonNumber,
                room = room.trim()
            )
            if (id == 0L) {
                repository.insertSchedule(schedule)
                _snackbarMessage.value = "Jadwal les ke-$lessonNumber berhasil ditambahkan"
            } else {
                repository.updateSchedule(schedule)
                _snackbarMessage.value = "Jadwal les ke-$lessonNumber diperbarui"
            }
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            try {
                repository.deleteSchedule(schedule)
                _snackbarMessage.value = "Jadwal berhasil dihapus"
            } catch (e: Exception) {
                _snackbarMessage.value = "Gagal menghapus jadwal"
            }
        }
    }

    fun switchUserRole(role: String) {
        val user = when (role) {
            "admin" -> UserEntity(id = 1, username = "admin", password = "admin123", nama = "Administrator", role = "admin")
            "guru" -> UserEntity(id = 2, username = "guru", password = "guru123", nama = "Aditya Pratama, S.Kom (Pak Adit)", role = "guru", teacherId = 1)
            else -> UserEntity(id = 3, username = "siswa", password = "siswa123", nama = "FAIZ DHABIT HARFANDA MANURUNG", role = "siswa", studentId = 9)
        }
        _currentUser.value = user
        _snackbarMessage.value = "Masuk sebagai: ${user.nama} (${user.role.uppercase()})"
    }

    fun reseedData() {
        viewModelScope.launch {
            repository.reseedData()
            _snackbarMessage.value = "Data demo SMK Tritech berhasil di-reset ulang!"
        }
    }

    fun getExportData(attendances: List<AttendanceWithDetails>): String {
        val school = settings.value?.schoolName ?: "SMK TRITECH INFORMATIKA MEDAN"
        return repository.generateCsvReport(attendances, school)
    }

    class Factory(private val repository: AttendanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
