package com.example.absensisiswa.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.data.model.ScheduleWithDetails
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.ui.theme.StatusGreen
import com.example.absensisiswa.ui.theme.StatusOrange
import com.example.absensisiswa.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScheduleScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val selectedDay by viewModel.selectedScheduleDay.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val isVerifyingLoc by viewModel.isVerifyingSessionLocation.collectAsState()

    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val isAdmin = currentUser.role == "admin"
    val isGuru = currentUser.role == "guru"
    val teacherId = currentUser.teacherId ?: 1L

    var showAddDialog by remember { mutableStateOf(false) }

    val daysOfWeek = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

    // Filter schedules by selected day
    val filteredSchedules = remember(allSchedules, selectedDay, currentUser) {
        allSchedules.filter {
            it.schedule.day.equals(selectedDay, ignoreCase = true)
        }.sortedBy { it.schedule.lessonNumber }
    }

    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("teacher_schedule_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (isGuru) "Jadwal Mengajar Saya" else "Jadwal Pelajaran Sekolah",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "SMK Tritech Informatika Medan",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color(0xFF0F172A)
                    )
                }
            },
            actions = {
                if (isAdmin) {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah Jadwal",
                            tint = PrimaryBlue
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Day Selector Tabs
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysOfWeek) { day ->
                    val isSelected = day.equals(selectedDay, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.setSelectedScheduleDay(day) },
                        color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Schedules List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jadwal Hari $selectedDay (${filteredSchedules.size} Les)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    if (isGuru) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = "Guru: Pak Adit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (filteredSchedules.isEmpty()) {
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
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tidak ada jadwal pelajaran pada hari $selectedDay",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(filteredSchedules) { schedWithDetails ->
                    val sched = schedWithDetails.schedule
                    val subj = schedWithDetails.subject
                    val cls = schedWithDetails.classEntity
                    val tch = schedWithDetails.teacher

                    // Check if there is an existing session today for this schedule
                    val existingSession = allSessions.firstOrNull {
                        it.session.scheduleId == sched.id && it.session.date == todayDateStr
                    }

                    val isMySchedule = if (isGuru) sched.teacherId == teacherId else true

                    ScheduleCardItem(
                        schedule = schedWithDetails,
                        existingSession = existingSession,
                        isMySchedule = isMySchedule,
                        isVerifyingLoc = isVerifyingLoc,
                        onOpenSession = {
                            viewModel.openSession(schedWithDetails, context)
                        },
                        onContinueSession = { sessionId ->
                            viewModel.selectSession(sessionId)
                        },
                        onDeleteSchedule = if (isAdmin) {
                            { viewModel.deleteSchedule(sched) }
                        } else null
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Add Schedule Dialog (Admin)
    if (showAddDialog) {
        var selectedTeacherId by remember { mutableLongStateOf(allTeachers.firstOrNull()?.id ?: 1L) }
        var selectedClassId by remember { mutableLongStateOf(allClasses.firstOrNull()?.id ?: 1L) }
        var selectedSubjectId by remember { mutableLongStateOf(allSubjects.firstOrNull()?.id ?: 1L) }
        var lessonNum by remember { mutableIntStateOf(1) }
        var startTime by remember { mutableStateOf("07:00") }
        var endTime by remember { mutableStateOf("07:45") }
        var roomName by remember { mutableStateOf("Lab RPL 1") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Tambah Jadwal Pelajaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Hari: $selectedDay", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Teacher dropdown selection
                    Column {
                        Text("Guru Pengampu:", fontSize = 12.sp, color = Color(0xFF64748B))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(allTeachers) { t ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (t.id == selectedTeacherId) PrimaryBlue else Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable { selectedTeacherId = t.id }
                                ) {
                                    Text(
                                        text = t.nama.split(",").firstOrNull() ?: t.nama,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (t.id == selectedTeacherId) Color.White else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Class selection
                    Column {
                        Text("Kelas:", fontSize = 12.sp, color = Color(0xFF64748B))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(allClasses) { c ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (c.id == selectedClassId) PrimaryBlue else Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable { selectedClassId = c.id }
                                ) {
                                    Text(
                                        text = c.namaKelas,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (c.id == selectedClassId) Color.White else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Subject selection
                    Column {
                        Text("Mata Pelajaran:", fontSize = 12.sp, color = Color(0xFF64748B))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(allSubjects) { s ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (s.id == selectedSubjectId) PrimaryBlue else Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable { selectedSubjectId = s.id }
                                ) {
                                    Text(
                                        text = s.namaMataPelajaran,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (s.id == selectedSubjectId) Color.White else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Jam Mulai") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("Jam Selesai") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Ruangan / Lab") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSchedule(
                            id = 0,
                            teacherId = selectedTeacherId,
                            classId = selectedClassId,
                            subjectId = selectedSubjectId,
                            day = selectedDay,
                            startTime = startTime,
                            endTime = endTime,
                            lessonNumber = lessonNum,
                            room = roomName
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ScheduleCardItem(
    schedule: ScheduleWithDetails,
    existingSession: com.example.absensisiswa.data.model.AttendanceSessionWithDetails?,
    isMySchedule: Boolean,
    isVerifyingLoc: Boolean,
    onOpenSession: () -> Unit,
    onContinueSession: (Long) -> Unit,
    onDeleteSchedule: (() -> Unit)? = null
) {
    val sched = schedule.schedule
    val subj = schedule.subject
    val cls = schedule.classEntity
    val tch = schedule.teacher

    val isSessionOpen = existingSession != null && existingSession.session.status == "DIBUKA"
    val isSessionClosed = existingSession != null && existingSession.session.status == "DITUTUP"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSessionOpen) StatusGreen else BorderLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Time & Lesson Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSessionOpen) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "L${sched.lessonNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSessionOpen) StatusGreen else PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${sched.startTime} – ${sched.endTime}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Text(
                            text = "Les ke-${sched.lessonNumber} • ${sched.room}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Session Status Tag
                if (isSessionOpen) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "SESI AKTIF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (isSessionClosed) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "SELESAI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (onDeleteSchedule != null) {
                    IconButton(
                        onClick = onDeleteSchedule,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus Jadwal",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Class & Subject Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = cls?.namaKelas ?: "Kelas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = subj?.namaMataPelajaran ?: "Mata Pelajaran",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Guru: ${tch?.nama ?: "Pak Adit"}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: BUKA ABSENSI / LANJUTKAN ABSENSI / LIHAT ABSENSI
            if (isSessionOpen) {
                Button(
                    onClick = { onContinueSession(existingSession.session.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lanjutkan Absensi (Buka)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else if (isSessionClosed) {
                OutlinedButton(
                    onClick = { onContinueSession(existingSession.session.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF94A3B8)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569))
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lihat Rekap Absensi Sesi Ini",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            } else if (isMySchedule) {
                Button(
                    onClick = onOpenSession,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isVerifyingLoc) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Memverifikasi GPS...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BUKA ABSENSI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Diampu oleh: ${tch?.nama ?: "Guru lain"}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}
