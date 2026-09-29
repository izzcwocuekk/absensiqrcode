package com.example.absensisiswa.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.R
import com.example.absensisiswa.data.model.AttendanceWithDetails
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.components.GeofenceStatusBadge
import com.example.absensisiswa.ui.components.StatusBadge
import com.example.absensisiswa.ui.components.StudentAvatar
import com.example.absensisiswa.ui.components.StudentCardDialog
import com.example.absensisiswa.ui.theme.BrandBorder
import com.example.absensisiswa.ui.theme.BrandGreen
import com.example.absensisiswa.ui.theme.BrandGreenContainer
import com.example.absensisiswa.ui.theme.BrandGreenDark
import com.example.absensisiswa.ui.theme.BrandMagenta
import com.example.absensisiswa.ui.theme.BrandMagentaContainer
import com.example.absensisiswa.ui.theme.BrandMuted
import com.example.absensisiswa.ui.theme.BrandSurface
import com.example.absensisiswa.ui.theme.BrandText
import com.example.absensisiswa.ui.theme.BrandTextSecondary
import com.example.absensisiswa.ui.theme.BrandSuccess
import com.example.absensisiswa.ui.theme.BrandSuccessBg
import com.example.absensisiswa.ui.theme.BrandWarning
import com.example.absensisiswa.ui.theme.BrandWarningBg
import com.example.absensisiswa.ui.theme.BrandError
import com.example.absensisiswa.ui.theme.BrandErrorBg
import com.example.absensisiswa.ui.theme.BrandInfo
import com.example.absensisiswa.ui.theme.BrandInfoBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stats by viewModel.dashboardStats.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val recentAttendances by viewModel.recentAttendances.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val cardStudentToShow by viewModel.studentCardToShow.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val locationResult by viewModel.locationResult.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingTime = when (hour) {
        in 5..11 -> "Selamat pagi"
        in 12..14 -> "Selamat siang"
        in 15..18 -> "Selamat sore"
        else -> "Selamat malam"
    }

    val todayDateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(Date())
    val todayIndonesianDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    val todayDayName = SimpleDateFormat("EEEE", Locale("id", "ID")).format(Date())
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val isStudent = currentUser.role == "siswa"
    val isGuru = currentUser.role == "guru"
    val isAdmin = currentUser.role == "admin"
    val teacherId = currentUser.teacherId ?: 1L

    // Student identification
    val currentStudent = remember(currentUser, allStudents) {
        if (isStudent) {
            allStudents.firstOrNull { it.student.id == currentUser.studentId }
                ?: allStudents.firstOrNull { it.student.nama.contains(currentUser.nama, ignoreCase = true) }
        } else null
    }

    // Student's attendance today
    val studentTodayAttendance = remember(currentStudent, recentAttendances) {
        if (currentStudent != null) {
            recentAttendances.firstOrNull { it.attendance.studentId == currentStudent.student.id }
        } else null
    }

    // Teacher schedules for today
    val todayTeacherSchedules = remember(allSchedules, todayDayName, teacherId) {
        allSchedules.filter {
            it.schedule.day.equals(todayDayName, ignoreCase = true)
        }.sortedBy { it.schedule.lessonNumber }
    }

    // Class schedules for current student
    val studentClassId = currentStudent?.classEntity?.id ?: 1L
    val studentTodaySchedules = remember(allSchedules, todayDayName, studentClassId) {
        allSchedules.filter {
            it.schedule.classId == studentClassId && it.schedule.day.equals(todayDayName, ignoreCase = true)
        }.sortedBy { it.schedule.lessonNumber }
    }

    // Next lesson calculation for student
    val nextLesson = remember(studentTodaySchedules) {
        studentTodaySchedules.firstOrNull { it.schedule.lessonNumber > 1 }
            ?: studentTodaySchedules.firstOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF8))
            .testTag("dashboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 8. DASHBOARD SISWA (SECTION 8)
            // ==========================================
            if (isStudent) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Header Siswa:
                    // Logo / brand kecil
                    // "Selamat pagi, [Nama]"
                    // "X RPL 1"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            color = BrandGreenContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.tritech_logo),
                                    contentDescription = "Logo TriTech",
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val studentShortName = currentStudent?.student?.nama?.split(" ")?.firstOrNull()
                                ?: currentUser.nama.split(" ").firstOrNull() ?: "Siswa"
                            Text(
                                text = "$greetingTime, $studentShortName",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandText
                            )
                            Text(
                                text = currentStudent?.classEntity?.namaKelas ?: "X RPL 1",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandGreenDark
                            )
                        }

                        // Quick QR card button
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (currentStudent != null) {
                                        viewModel.showStudentCard(currentStudent)
                                    } else {
                                        onNavigate(AppScreen.SCAN_QR)
                                    }
                                },
                            color = BrandSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.QrCode,
                                    contentDescription = "Kartu QR",
                                    tint = BrandGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // STATUS ABSENSI HARI INI
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "STATUS ABSENSI HARI INI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = todayDateFormatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (studentTodayAttendance != null) {
                                val isLate = studentTodayAttendance.attendance.status.equals("Terlambat", true)
                                val statusBg = if (isLate) BrandWarningBg else BrandSuccessBg
                                val statusColor = if (isLate) BrandWarning else BrandSuccess
                                val statusTitle = if (isLate) "TERLAMBAT" else "HADIR"
                                val checkInTime = studentTodayAttendance.attendance.jamMasuk?.take(5) ?: "07:15"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(statusBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = statusTitle,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = statusColor,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "$checkInTime WIB",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandTextSecondary
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(BrandGreenContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = BrandGreen,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = "BELUM ABSEN",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandText,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "Batas: ${settings?.schoolStartTime?.take(5) ?: "07:00"} WIB",
                                            fontSize = 12.sp,
                                            color = BrandMuted
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Simplified action button
                            Button(
                                onClick = { onNavigate(AppScreen.SCAN_QR) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (studentTodayAttendance != null) "Scan Ulang / Presensi" else "Scan QR Absensi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Mata Pelajaran Berikutnya: Kelas, Les, Jam
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "MATA PELAJARAN BERIKUTNYA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandMuted,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            if (nextLesson != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        color = BrandMagentaContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Book,
                                                contentDescription = null,
                                                tint = BrandMagenta,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = nextLesson.subject?.namaMataPelajaran ?: "Pelajaran",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandText
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${nextLesson.classEntity?.namaKelas ?: "Kelas"} • Les ${nextLesson.schedule.lessonNumber} • ${nextLesson.schedule.startTime}–${nextLesson.schedule.endTime}",
                                            fontSize = 12.sp,
                                            color = BrandTextSecondary
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Tidak ada jadwal pelajaran lanjutan hari ini.",
                                    fontSize = 13.sp,
                                    color = BrandMuted
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 9. DASHBOARD GURU (SECTION 9)
            // ==========================================
            else if (isGuru) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Header: "Selamat datang, Pak/Bu [Nama]" + Tanggal
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Selamat datang, ${currentUser.nama}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = todayIndonesianDate,
                            fontSize = 13.sp,
                            color = BrandMagenta,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // JADWAL MENGAJAR HARI INI
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JADWAL MENGAJAR HARI INI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandMuted,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandGreenContainer
                        ) {
                            Text(
                                text = "${todayTeacherSchedules.size} Sesi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                items(todayTeacherSchedules) { sched ->
                    val isLes1 = sched.schedule.lessonNumber == 1
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    // 07:00
                                    Text(
                                        text = sched.schedule.startTime,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandGreenDark
                                    )
                                    // X RPL 1
                                    Text(
                                        text = sched.classEntity?.namaKelas ?: "Kelas",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandText
                                    )
                                    // Informatika • Les 1
                                    Text(
                                        text = "${sched.subject?.namaMataPelajaran ?: "Pelajaran"} • Les ${sched.schedule.lessonNumber}",
                                        fontSize = 13.sp,
                                        color = BrandTextSecondary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isLes1) BrandGreenContainer else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = if (isLes1) "Sesi Aktif" else "Terjadwal",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLes1) BrandGreenDark else BrandMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // [ BUKA ABSENSI ]
                            Button(
                                onClick = {
                                    viewModel.openSession(sched, context)
                                    onNavigate(AppScreen.SESI_ABSENSI)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                            ) {
                                Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BUKA ABSENSI",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 13. ADMIN DASHBOARD (SECTION 13)
            // ==========================================
            else {
                item {
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Dashboard Administrator",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandText
                            )
                            Text(
                                text = todayIndonesianDate,
                                fontSize = 12.sp,
                                color = BrandMuted
                            )
                        }

                        // Geolocation status pill (Section 12)
                        val locStatus = locationResult?.geofenceStatus?.name ?: "DI_SEKOLAH"
                        GeofenceStatusBadge(status = locStatus)
                    }
                }

                // 4 Mobile Metrics Cards: Siswa, Guru, Kelas, Absensi Hari Ini
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminMetricCard(
                                title = "Siswa",
                                count = "${allStudents.size}",
                                subtitle = "Terdaftar",
                                icon = Icons.Default.People,
                                color = BrandGreen,
                                bgColor = BrandGreenContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(AppScreen.DATA_SISWA) }
                            )
                            AdminMetricCard(
                                title = "Guru",
                                count = "${allTeachers.size.coerceAtLeast(4)}",
                                subtitle = "Pengajar",
                                icon = Icons.Default.Person,
                                color = BrandMagenta,
                                bgColor = BrandMagentaContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(AppScreen.DATA_GURU) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminMetricCard(
                                title = "Kelas",
                                count = "${allClasses.size.coerceAtLeast(4)}",
                                subtitle = "RPL & TKJ",
                                icon = Icons.Default.Class,
                                color = BrandInfo,
                                bgColor = BrandInfoBg,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(AppScreen.DATA_KELAS) }
                            )
                            val totalHadir = stats.hadir + stats.terlambat
                            AdminMetricCard(
                                title = "Absensi Hari Ini",
                                count = "$totalHadir",
                                subtitle = "${stats.attendancePercentage.toInt()}% Hadir",
                                icon = Icons.Default.AssignmentTurnedIn,
                                color = BrandSuccess,
                                bgColor = BrandSuccessBg,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(AppScreen.PRESENSI) }
                            )
                        }
                    }
                }

                // STATUS ABSENSI: Hadir, Izin, Sakit, Alpa
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "STATUS ABSENSI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandMuted,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AttendanceStatusColumn("Hadir", "${stats.hadir}", BrandSuccess, BrandSuccessBg)
                                AttendanceStatusColumn("Izin", "${stats.izin}", BrandInfo, BrandInfoBg)
                                AttendanceStatusColumn("Sakit", "${stats.sakit}", BrandMagenta, BrandMagentaContainer)
                                AttendanceStatusColumn("Alpa", "${stats.alpa}", BrandError, BrandErrorBg)
                            }
                        }
                    }
                }

                // Quick Admin Navigation Buttons
                item {
                    Text(
                        text = "AKSES CEPAT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(AppScreen.SESI_ABSENSI) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sesi Absen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onNavigate(AppScreen.SCAN_QR) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onNavigate(AppScreen.LAPORAN) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandMagenta)
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Laporan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Student Card Dialog (if active)
        cardStudentToShow?.let { studentWithClass ->
            StudentCardDialog(
                studentWithClass = studentWithClass,
                schoolName = settings?.schoolName ?: "SMK TRITECH INFORMATIKA MEDAN",
                onDismiss = { viewModel.showStudentCard(null) },
                onRegenerateQr = if (isAdmin) {
                    { viewModel.regenerateQrToken(studentWithClass.student.id) }
                } else null
            )
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    count: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = BrandSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandTextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandText
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = BrandMuted
            )
        }
    }
}

@Composable
private fun AttendanceStatusColumn(
    label: String,
    count: String,
    color: Color,
    bgColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandTextSecondary
        )
    }
}
