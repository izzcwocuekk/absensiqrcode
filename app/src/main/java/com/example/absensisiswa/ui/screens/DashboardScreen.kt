package com.example.absensisiswa.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.data.model.AttendanceWithDetails
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.components.StatusBadge
import com.example.absensisiswa.ui.components.StudentAvatar
import com.example.absensisiswa.ui.components.StudentCardDialog
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.ui.theme.StatusBlue
import com.example.absensisiswa.ui.theme.StatusGreen
import com.example.absensisiswa.ui.theme.StatusOrange
import com.example.absensisiswa.ui.theme.StatusPurple
import com.example.absensisiswa.ui.theme.StatusRed
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
    val isVerifyingLoc by viewModel.isVerifyingSessionLocation.collectAsState()

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

    // If current user is student, identify matching student
    val currentStudent = remember(currentUser, allStudents) {
        if (isStudent) {
            allStudents.firstOrNull { it.student.id == currentUser.studentId }
                ?: allStudents.firstOrNull { it.student.nama.contains(currentUser.nama, ignoreCase = true) }
        } else null
    }

    // Check if this student has attended today
    val studentTodayAttendance = remember(currentStudent, recentAttendances) {
        if (currentStudent != null) {
            recentAttendances.firstOrNull { it.attendance.studentId == currentStudent.student.id }
        } else null
    }

    val todayTeacherSchedules = remember(allSchedules, todayDayName, teacherId) {
        allSchedules.filter {
            it.schedule.day.equals(todayDayName, ignoreCase = true)
        }.sortedBy { it.schedule.lessonNumber }
    }

    val activeSessionCount = remember(allSessions, todayDateStr) {
        allSessions.count { it.session.date == todayDateStr && it.session.status == "DIBUKA" }
    }

    val totalStudents = if (stats.totalStudents > 0) stats.totalStudents else 25
    val percentNumber = if (totalStudents > 0) {
        val totalHadirSemua = stats.hadir + stats.terlambat
        ((totalHadirSemua.toFloat() / totalStudents.toFloat()) * 100).toInt()
    } else 0

    val progressFloat = (percentNumber / 100f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("dashboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Personalized Mobile Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val displayName = if (isStudent) {
                            currentUser.nama.split(" ").firstOrNull() ?: currentUser.nama
                        } else {
                            currentUser.nama
                        }

                        Text(
                            text = "$greetingTime, $displayName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isStudent) {
                                "${currentStudent?.classEntity?.namaKelas ?: "X RPL 1"} • NIS: ${currentStudent?.student?.nis ?: "R.0422.26"}"
                            } else {
                                "Berikut ringkasan kehadiran siswa hari ini."
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable { onNavigate(if (isStudent) AppScreen.PRESENSI else AppScreen.DATA_SISWA) },
                            color = Color(0xFFF1F5F9),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable { onNavigate(AppScreen.PRESENSI) },
                            color = Color(0xFFF1F5F9),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Pemberitahuan",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(18.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDC2626))
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SISWA SPECIFIC DASHBOARD VIEW
            // ==========================================
            if (isStudent) {
                // Today Attendance Status Card for Student
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
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
                                    text = "Status Kehadiran Hari Ini",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = todayDateFormatted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (studentTodayAttendance != null) {
                                // Already recorded
                                val isLate = studentTodayAttendance.attendance.status.equals("Terlambat", true)
                                val statusBg = if (isLate) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                                val statusColor = if (isLate) StatusOrange else StatusGreen

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(statusBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isLate) "Tercatat Terlambat" else "Sudah Hadir Tepat Waktu",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = statusColor
                                        )
                                        Text(
                                            text = "Pukul ${studentTodayAttendance.attendance.jamMasuk ?: "-"} WIB",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            } else {
                                // Not attended yet
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Belum Melakukan Presensi",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Batas masuk: ${settings?.schoolStartTime?.take(5) ?: "07:15"} WIB",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons for Student
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onNavigate(AppScreen.SCAN_QR) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buka Scanner", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (currentStudent != null) {
                                    OutlinedButton(
                                        onClick = { viewModel.showStudentCard(currentStudent) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Kartu QR Saya", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Student's Class Schedule for Today
                item {
                    val studentClassId = currentStudent?.classEntity?.id ?: 1L
                    val studentTodaySchedules = allSchedules.filter {
                        it.schedule.classId == studentClassId && it.schedule.day.equals(todayDayName, ignoreCase = true)
                    }.sortedBy { it.schedule.lessonNumber }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jadwal Pelajaran Kelas Hari Ini",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${studentTodaySchedules.size} Pelajaran",
                                    fontSize = 12.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = BorderLight)
                            Spacer(modifier = Modifier.height(10.dp))

                            if (studentTodaySchedules.isEmpty()) {
                                Text(
                                    text = "Tidak ada jadwal pelajaran hari ini",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            } else {
                                studentTodaySchedules.forEach { s ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFEFF6FF)
                                            ) {
                                                Text(
                                                    text = "Les ${s.schedule.lessonNumber}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlue,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = s.subject?.namaMataPelajaran ?: "Pelajaran",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = "${s.teacher?.nama?.split(",")?.firstOrNull() ?: "Guru"} • ${s.schedule.startTime} - ${s.schedule.endTime}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isGuru) {
                // ==========================================
                // GURU DASHBOARD VIEW (Mobile-First)
                // ==========================================
                // Teacher Header Card with stats
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
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
                                Column {
                                    Text(
                                        text = "Dashboard Guru Pengampu",
                                        color = Color(0xFFDBEAFE),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = currentUser.nama,
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = todayIndonesianDate.split(",").firstOrNull() ?: "Hari Ini",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "JADWAL HARI INI", color = Color(0xFFDBEAFE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${todayTeacherSchedules.size} Les", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(text = "SESI AKTIF", color = Color(0xFFDBEAFE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "$activeSessionCount Sesi", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(text = "TOTAL KELAS", color = Color(0xFFDBEAFE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${allClasses.size} Kelas", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section Header: Jadwal Mengajar Hari Ini
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jadwal Mengajar Hari Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Lihat Semua >",
                            fontSize = 12.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigate(AppScreen.JADWAL_PELAJARAN) }
                        )
                    }
                }

                // List of Teaching Schedule Cards for Today
                if (todayTeacherSchedules.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tidak ada jadwal mengajar pada hari $todayDayName",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(todayTeacherSchedules) { schedWithDetails ->
                        val sched = schedWithDetails.schedule
                        val subj = schedWithDetails.subject
                        val cls = schedWithDetails.classEntity
                        val tch = schedWithDetails.teacher

                        val existingSession = allSessions.firstOrNull {
                            it.session.scheduleId == sched.id && it.session.date == todayDateStr
                        }

                        val isMySchedule = sched.teacherId == teacherId

                        ScheduleCardItem(
                            schedule = schedWithDetails,
                            existingSession = existingSession,
                            isMySchedule = isMySchedule,
                            isVerifyingLoc = isVerifyingLoc,
                            onOpenSession = {
                                viewModel.openSession(schedWithDetails, context)
                            },
                            onContinueSession = { sId ->
                                viewModel.selectSession(sId)
                            }
                        )
                    }
                }

                // Quick Navigation Shortcuts for Teacher
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigate(AppScreen.JADWAL_PELAJARAN) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Jadwal Seminggu", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onNavigate(AppScreen.PRESENSI) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Riwayat Absensi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // ==========================================
                // ADMIN DASHBOARD VIEW (2x2 STATS & MASTER DATA)
                // ==========================================
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardStatCard(
                                modifier = Modifier.weight(1f),
                                title = "TOTAL SISWA",
                                titleColor = Color(0xFF64748B),
                                count = totalStudents.toString(),
                                countColor = Color(0xFF0F172A),
                                subtitle = "SMK Tritech",
                                icon = Icons.Default.People,
                                iconTint = PrimaryBlue,
                                iconBg = Color(0xFFEFF6FF),
                                onClick = { onNavigate(AppScreen.DATA_SISWA) }
                            )

                            DashboardStatCard(
                                modifier = Modifier.weight(1f),
                                title = "HADIR",
                                titleColor = StatusGreen,
                                count = stats.hadir.toString(),
                                countColor = StatusGreen,
                                subtitle = "Tepat waktu",
                                icon = Icons.Default.CheckCircle,
                                iconTint = StatusGreen,
                                iconBg = Color(0xFFDCFCE7),
                                onClick = { onNavigate(AppScreen.PRESENSI) }
                            )
                        }

                        val tidakHadirCount = stats.izin + stats.sakit + stats.alpa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardStatCard(
                                modifier = Modifier.weight(1f),
                                title = "TERLAMBAT",
                                titleColor = StatusOrange,
                                count = stats.terlambat.toString(),
                                countColor = StatusOrange,
                                subtitle = "Lewat ${settings?.schoolStartTime?.take(5) ?: "07:15"}",
                                icon = Icons.Default.Schedule,
                                iconTint = StatusOrange,
                                iconBg = Color(0xFFFEF3C7),
                                onClick = { onNavigate(AppScreen.PRESENSI) }
                            )

                            DashboardStatCard(
                                modifier = Modifier.weight(1f),
                                title = "TIDAK HADIR",
                                titleColor = StatusRed,
                                count = tidakHadirCount.toString(),
                                countColor = StatusRed,
                                subtitle = "Izin, Sakit, Alfa",
                                icon = Icons.Default.Warning,
                                iconTint = StatusRed,
                                iconBg = Color(0xFFFEE2E2),
                                onClick = { onNavigate(AppScreen.PRESENSI) }
                            )
                        }
                    }
                }

                // Admin Master Data Management Shortcuts
                item {
                    Text(
                        text = "Kelola Master Data Sekolah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigate(AppScreen.DATA_GURU) },
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Data Guru", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigate(AppScreen.DATA_MAPEL) },
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3E8FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Book, contentDescription = null, tint = StatusPurple, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Mata Pelajaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigate(AppScreen.JADWAL_PELAJARAN) },
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Jadwal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigate(AppScreen.DATA_KELAS) },
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Class, contentDescription = null, tint = StatusOrange, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Data Kelas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }
                    }
                }

                // Ringkasan Kehadiran Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
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
                                    text = "Ringkasan Kehadiran Hari Ini",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                                ) {
                                    Text(
                                        text = todayDateFormatted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$percentNumber%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "dari $totalStudents siswa sudah absen hari ini",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = StatusGreen,
                                trackColor = Color(0xFFE2E8F0),
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MiniStatusChip(label = "Hadir", count = stats.hadir, color = StatusGreen, bg = Color(0xFFDCFCE7))
                                MiniStatusChip(label = "Terlambat", count = stats.terlambat, color = StatusOrange, bg = Color(0xFFFEF3C7))
                                MiniStatusChip(label = "Izin", count = stats.izin, color = StatusBlue, bg = Color(0xFFDBEAFE))
                                MiniStatusChip(label = "Sakit", count = stats.sakit, color = StatusPurple, bg = Color(0xFFF3E8FF))
                                MiniStatusChip(label = "Alfa", count = stats.alpa, color = StatusRed, bg = Color(0xFFFEE2E2))
                            }
                        }
                    }
                }

                // Quick Scanner Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(AppScreen.SCAN_QR) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Buka Scanner QR Siswa",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Scan kartu atau foto QR kehadiran",
                                        fontSize = 12.sp,
                                        color = Color(0xFFDBEAFE)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = "Mulai",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recent Attendance Feed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isStudent) "Riwayat Presensi Saya Terkini" else "Aktivitas Presensi Terkini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Lihat Semua >",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(AppScreen.PRESENSI) }
                    )
                }
            }

            val attendancesToDisplay = if (isStudent && currentStudent != null) {
                recentAttendances.filter { it.attendance.studentId == currentStudent.student.id }
            } else {
                recentAttendances.take(5)
            }

            if (attendancesToDisplay.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada presensi tercatat hari ini",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(attendancesToDisplay) { item ->
                    val studentName = item.studentWithClass?.student?.nama ?: "Siswa"
                    val className = item.studentWithClass?.classEntity?.namaKelas ?: "-"
                    val gender = item.studentWithClass?.student?.jenisKelamin ?: "L"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StudentAvatar(
                                name = studentName,
                                gender = gender,
                                size = 40
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                                Text(
                                    text = "$className • Jam ${item.attendance.jamMasuk ?: "-"} WIB",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            StatusBadge(status = item.attendance.status)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Student Card Dialog
        cardStudentToShow?.let { studentWithClass ->
            StudentCardDialog(
                studentWithClass = studentWithClass,
                schoolName = settings?.schoolName ?: "SMK TRITECH INFORMATIKA MEDAN",
                onDismiss = { viewModel.showStudentCard(null) },
                onRegenerateQr = if (currentUser.role == "admin") {
                    { viewModel.regenerateQrToken(studentWithClass.student.id) }
                } else null
            )
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    titleColor: Color,
    count: String,
    countColor: Color,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    letterSpacing = 0.5.sp
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = count,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = countColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun MiniStatusChip(
    label: String,
    count: Int,
    color: Color,
    bg: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = count.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
