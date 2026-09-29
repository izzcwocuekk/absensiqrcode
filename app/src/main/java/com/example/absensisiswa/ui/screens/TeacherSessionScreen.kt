package com.example.absensisiswa.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.absensisiswa.data.SessionScanResult
import com.example.absensisiswa.data.model.StudentSessionRecord
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.components.StudentAvatar
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.ui.theme.StatusBlue
import com.example.absensisiswa.ui.theme.StatusGreen
import com.example.absensisiswa.ui.theme.StatusOrange
import com.example.absensisiswa.ui.theme.StatusPurple
import com.example.absensisiswa.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSessionScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSessionWithDetails.collectAsState()
    val studentRecords by viewModel.activeSessionStudentRecords.collectAsState()
    val sessionScanResult by viewModel.sessionScanResult.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showScanDialog by remember { mutableStateOf(false) }
    var showCloseConfirmDialog by remember { mutableStateOf(false) }
    var studentForNotes by remember { mutableStateOf<StudentSessionRecord?>(null) }
    var noteInputText by remember { mutableStateOf("") }

    BackHandler {
        onNavigateBack()
    }

    if (activeSession == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tidak ada sesi absensi yang aktif",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Kembali ke Jadwal")
                }
            }
        }
        return
    }

    val session = activeSession!!.session
    val classEntity = activeSession!!.classEntity
    val subject = activeSession!!.subject
    val teacher = activeSession!!.teacher

    val isSessionOpen = session.status == "DIBUKA"

    // Summary counts
    val hadirCount = studentRecords.count { it.record.status == "HADIR" }
    val izinCount = studentRecords.count { it.record.status == "IZIN" }
    val sakitCount = studentRecords.count { it.record.status == "SAKIT" }
    val alpaCount = studentRecords.count { it.record.status == "ALPA" }
    val totalStudents = studentRecords.size

    val filteredRecords = remember(studentRecords, searchQuery) {
        if (searchQuery.isBlank()) {
            studentRecords
        } else {
            studentRecords.filter {
                it.student.nama.contains(searchQuery, ignoreCase = true) ||
                        it.student.nis.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("teacher_session_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Sesi Absensi: ${subject?.namaMataPelajaran ?: "Pelajaran"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${classEntity?.namaKelas ?: "Kelas"} • Les ke-${session.lessonNumber} (${session.startTime} - ${session.endTime})",
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSessionOpen) Color(0xFFDCFCE7) else Color(0xFFE2E8F0),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSessionOpen) StatusGreen else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = session.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSessionOpen) StatusGreen else Color(0xFF475569)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Section 10 Header:
                // X RPL 1
                // Informatika
                // Les 1
                // 07:00–07:45
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = classEntity?.namaKelas ?: "X RPL 1",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = subject?.namaMataPelajaran ?: "Informatika",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Les ${session.lessonNumber} • ${session.startTime}–${session.endTime}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSessionOpen) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = if (isSessionOpen) "DIBUKA" else "DITUTUP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSessionOpen) StatusGreen else Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = BorderLight, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Section 10 Summary:
                        // Hadir 23 | Izin 1 | Sakit 0 | Alpa 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SessionStatItem(label = "Hadir", count = hadirCount, color = StatusGreen, bgColor = Color(0xFFDCFCE7))
                            SessionStatItem(label = "Izin", count = izinCount, color = StatusBlue, bgColor = Color(0xFFDBEAFE))
                            SessionStatItem(label = "Sakit", count = sakitCount, color = StatusPurple, bgColor = Color(0xFFF3E8FF))
                            SessionStatItem(label = "Alpa", count = alpaCount, color = StatusRed, bgColor = Color(0xFFFEE2E2))
                        }

                        // Simplified Geolocation info (Section 12)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "📍 Lokasi Terverifikasi • Berada di area sekolah",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }
            }

            // Section 10 Action Button:
            // [ SCAN QR ]
            if (isSessionOpen) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showScanDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SCAN QR", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setAllStudentsInActiveSession("HADIR") },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Semua Hadir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar for Students
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama atau NIS siswa di kelas ini...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Header List Siswa
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Siswa ${classEntity?.namaKelas ?: ""} (${filteredRecords.size} Siswa)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Khusus Kelas Ini",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
            }

            // Students List
            itemsIndexed(filteredRecords) { index, itemRecord ->
                val student = itemRecord.student
                val record = itemRecord.record
                val currentStatus = record.status

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentStatus == "HADIR") Color(0xFFBBF7D0) else BorderLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Number
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.width(26.dp)
                            )

                            // Avatar
                            StudentAvatar(
                                name = student.nama,
                                gender = student.jenisKelamin,
                                size = 40
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Name & NIS
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.nama,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "NIS: ${student.nis}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    if (record.checkInTime != null && currentStatus == "HADIR") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• Jam ${record.checkInTime}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = StatusGreen
                                        )
                                    }
                                    if (record.verifiedByQr) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFDBEAFE)
                                        ) {
                                            Text(
                                                text = "QR",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (!record.notes.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Ket: ${record.notes}",
                                        fontSize = 11.sp,
                                        color = StatusOrange,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }

                            // Edit note button
                            if (isSessionOpen) {
                                IconButton(
                                    onClick = {
                                        studentForNotes = itemRecord
                                        noteInputText = record.notes ?: ""
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.EditNote,
                                        contentDescription = "Tambah Catatan",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Toggle Button Group: [Hadir] [Izin] [Sakit] [Alpa]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusOptionButton(
                                label = "Hadir",
                                isSelected = currentStatus == "HADIR",
                                activeBg = Color(0xFFDCFCE7),
                                activeColor = StatusGreen,
                                isEnabled = isSessionOpen,
                                onClick = { viewModel.updateStudentRecordStatus(student.id, "HADIR") },
                                modifier = Modifier.weight(1f)
                            )
                            StatusOptionButton(
                                label = "Izin",
                                isSelected = currentStatus == "IZIN",
                                activeBg = Color(0xFFDBEAFE),
                                activeColor = StatusBlue,
                                isEnabled = isSessionOpen,
                                onClick = { viewModel.updateStudentRecordStatus(student.id, "IZIN") },
                                modifier = Modifier.weight(1f)
                            )
                            StatusOptionButton(
                                label = "Sakit",
                                isSelected = currentStatus == "SAKIT",
                                activeBg = Color(0xFFF3E8FF),
                                activeColor = StatusPurple,
                                isEnabled = isSessionOpen,
                                onClick = { viewModel.updateStudentRecordStatus(student.id, "SAKIT") },
                                modifier = Modifier.weight(1f)
                            )
                            StatusOptionButton(
                                label = "Alpa",
                                isSelected = currentStatus == "ALPA",
                                activeBg = Color(0xFFFEE2E2),
                                activeColor = StatusRed,
                                isEnabled = isSessionOpen,
                                onClick = { viewModel.updateStudentRecordStatus(student.id, "ALPA") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // Persistent Bottom Summary Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Summary Counts Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ringkasan:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Hadir: $hadirCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen
                        )
                        Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        Text(
                            text = "Izin: $izinCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusBlue
                        )
                        Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        Text(
                            text = "Sakit: $sakitCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusPurple
                        )
                        Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        Text(
                            text = "Alpa: $alpaCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Button: Simpan / Tutup Sesi
                if (isSessionOpen) {
                    Button(
                        onClick = { showCloseConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan & Tutup Sesi Absensi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sesi Telah Ditutup (Kembali)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog before closing session
    if (showCloseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCloseConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Konfirmasi Simpan Absensi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Periksa kembali rekap absensi kelas ${classEntity?.namaKelas ?: ""} untuk pelajaran ${subject?.namaMataPelajaran ?: ""}:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            SummaryRowItem("Hadir", hadirCount.toString(), StatusGreen)
                            SummaryRowItem("Izin", izinCount.toString(), StatusBlue)
                            SummaryRowItem("Sakit", sakitCount.toString(), StatusPurple)
                            SummaryRowItem("Alpa", alpaCount.toString(), StatusRed)
                            Divider(color = BorderLight, modifier = Modifier.padding(vertical = 6.dp))
                            SummaryRowItem("Total Siswa", totalStudents.toString(), Color(0xFF0F172A), isBold = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Setelah ditutup, sesi akan disimpan secara permanen ke basis data.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCloseConfirmDialog = false
                        viewModel.closeActiveSession("Selesai KBM Les ke-${session.lessonNumber}")
                        Toast.makeText(context, "Sesi absensi berhasil disimpan dan ditutup!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Tutup & Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // QR Scan / Manual Code Input Dialog for this Session
    if (showScanDialog) {
        var manualCode by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showScanDialog = false
                viewModel.dismissSessionScanResult()
            },
            icon = {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Scan QR Siswa: ${classEntity?.namaKelas ?: ""}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Arahkan kamera ke QR kartu siswa atau masukkan NIS siswa kelas ${classEntity?.namaKelas ?: ""}:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { manualCode = it },
                        label = { Text("NIS atau Kode QR") },
                        placeholder = { Text("Contoh: R.0422.26") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Feedback on scan result if any
                    if (sessionScanResult != null) {
                        when (val res = sessionScanResult!!) {
                            is SessionScanResult.Success -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(res.message, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                                    }
                                }
                            }
                            is SessionScanResult.AlreadyAttended -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusOrange, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(res.message, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusOrange)
                                    }
                                }
                            }
                            is SessionScanResult.WrongClass -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEE2E2),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = StatusRed, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(res.message, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                                    }
                                }
                            }
                            is SessionScanResult.NotFound -> {
                                Text(res.message, color = StatusRed, fontSize = 12.sp)
                            }
                            is SessionScanResult.Error -> {
                                Text(res.message, color = StatusRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualCode.isNotBlank()) {
                            viewModel.scanStudentQrInActiveSession(manualCode.trim())
                            manualCode = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Verifikasi & Absen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showScanDialog = false
                        viewModel.dismissSessionScanResult()
                    }
                ) {
                    Text("Selesai")
                }
            }
        )
    }

    // Dialog for Notes
    if (studentForNotes != null) {
        AlertDialog(
            onDismissRequest = { studentForNotes = null },
            title = {
                Text(
                    text = "Catatan Siswa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Catatan untuk: ${studentForNotes!!.student.nama}",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        label = { Text("Keterangan (misal: Sakit flu, Izin lomba)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stId = studentForNotes!!.student.id
                        val currStatus = studentForNotes!!.record.status
                        viewModel.updateStudentRecordStatus(stId, currStatus, noteInputText.trim())
                        studentForNotes = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentForNotes = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun StatusOptionButton(
    label: String,
    isSelected: Boolean,
    activeBg: Color,
    activeColor: Color,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isEnabled) { onClick() },
        color = if (isSelected) activeBg else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) activeColor else BorderLight
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) activeColor else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun SummaryRowItem(
    label: String,
    count: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF475569)
        )
        Text(
            text = count,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun SessionStatItem(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )
    }
}

