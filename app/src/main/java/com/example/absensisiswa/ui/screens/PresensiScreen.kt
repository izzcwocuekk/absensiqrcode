package com.example.absensisiswa.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.data.entity.AttendanceEntity
import com.example.absensisiswa.data.model.AttendanceWithDetails
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.components.AttendanceEditDialog
import com.example.absensisiswa.ui.components.StatusBadge
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.ui.theme.StatusBlue
import com.example.absensisiswa.ui.theme.StatusGreen
import com.example.absensisiswa.ui.theme.StatusOrange
import com.example.absensisiswa.ui.theme.StatusPurple
import com.example.absensisiswa.ui.theme.StatusRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PresensiScreen(
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedPresensiDate.collectAsState()
    val presensiClassFilter by viewModel.presensiClassFilter.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val attendanceToEdit by viewModel.attendanceToEdit.collectAsState()
    val attendancesForDate by viewModel.recentAttendances.collectAsState()

    var statusFilter by remember { mutableStateOf("Semua Status") }
    var searchQuery by remember { mutableStateOf("") }
    var isClassMenuOpen by remember { mutableStateOf(false) }
    var isStatusMenuOpen by remember { mutableStateOf(false) }
    var isManualAccordionOpen by remember { mutableStateOf(false) }

    // Manual add form states
    var manualStudentId by remember { mutableStateOf<Long?>(null) }
    var manualStatus by remember { mutableStateOf("Hadir") }
    var manualKeterangan by remember { mutableStateOf("") }
    var isStudentSelectOpen by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formatted = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            viewModel.setPresensiDate(formatted)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Formatted date string (e.g. 26/09/2026)
    val displayDateFormatted = remember(selectedDate) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (parsed != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed) else selectedDate
        } catch (e: Exception) {
            selectedDate
        }
    }

    val headerDateFormatted = remember(selectedDate) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (parsed != null) SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(parsed) else selectedDate
        } catch (e: Exception) {
            selectedDate
        }
    }

    // Filter attendances
    val filteredAttendances = remember(attendancesForDate, selectedDate, presensiClassFilter, statusFilter, searchQuery) {
        attendancesForDate.filter { item ->
            val matchClass = presensiClassFilter == 0L || item.studentWithClass?.student?.classId == presensiClassFilter
            val matchStatus = when (statusFilter) {
                "Semua Status" -> true
                else -> item.attendance.status.equals(statusFilter, ignoreCase = true)
            }
            val studentName = item.studentWithClass?.student?.nama ?: ""
            val studentNis = item.studentWithClass?.student?.nis ?: ""
            val matchSearch = searchQuery.isBlank() ||
                    studentName.contains(searchQuery, ignoreCase = true) ||
                    studentNis.contains(searchQuery, ignoreCase = true)
            matchClass && matchStatus && matchSearch
        }
    }

    // Counts for chips
    val hadirCount = remember(attendancesForDate) { attendancesForDate.count { it.attendance.status.equals("Hadir", ignoreCase = true) } }
    val terlambatCount = remember(attendancesForDate) { attendancesForDate.count { it.attendance.status.equals("Terlambat", ignoreCase = true) } }
    val izinCount = remember(attendancesForDate) { attendancesForDate.count { it.attendance.status.equals("Izin", ignoreCase = true) } }
    val sakitCount = remember(attendancesForDate) { attendancesForDate.count { it.attendance.status.equals("Sakit", ignoreCase = true) } }
    val alfaCount = remember(attendancesForDate) { attendancesForDate.count { it.attendance.status.equals("Alfa", ignoreCase = true) || it.attendance.status.equals("Alpa", ignoreCase = true) } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("presensi_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Title & Subtitle (Frame 00:05 in video)
                Text(
                    text = "Riwayat Absensi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Catatan riwayat kehadiran siswa dan pelacakan presensi harian.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Button: [ Buka Scanner QR ]
                OutlinedButton(
                    onClick = { onNavigate(AppScreen.SCAN_QR) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buka Scanner QR",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Card (Date, Class, Status, Search, and Filter Button)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tanggal row
                        Column {
                            Text(text = "Tanggal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { datePickerDialog.show() },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = displayDateFormatted, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Kelas and Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Kelas Dropdown
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Kelas", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(4.dp))
                                Box {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { isClassMenuOpen = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val selectedClassName = if (presensiClassFilter == 0L) "Semua Kelas"
                                            else allClasses.firstOrNull { it.id == presensiClassFilter }?.namaKelas ?: "Semua Kelas"
                                            Text(text = selectedClassName, fontSize = 12.sp, color = Color(0xFF0F172A), maxLines = 1)
                                            Text("▾", fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    DropdownMenu(expanded = isClassMenuOpen, onDismissRequest = { isClassMenuOpen = false }) {
                                        DropdownMenuItem(text = { Text("Semua Kelas") }, onClick = { viewModel.setPresensiClassFilter(0L); isClassMenuOpen = false })
                                        allClasses.forEach { cls ->
                                            DropdownMenuItem(text = { Text(cls.namaKelas) }, onClick = { viewModel.setPresensiClassFilter(cls.id); isClassMenuOpen = false })
                                        }
                                    }
                                }
                            }

                            // Status Dropdown
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Status", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(4.dp))
                                Box {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { isStatusMenuOpen = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = statusFilter, fontSize = 12.sp, color = Color(0xFF0F172A), maxLines = 1)
                                            Text("▾", fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    DropdownMenu(expanded = isStatusMenuOpen, onDismissRequest = { isStatusMenuOpen = false }) {
                                        listOf("Semua Status", "Hadir", "Terlambat", "Izin", "Sakit", "Alfa").forEach { st ->
                                            DropdownMenuItem(text = { Text(st) }, onClick = { statusFilter = st; isStatusMenuOpen = false })
                                        }
                                    }
                                }
                            }
                        }

                        // Cari Siswa
                        Column {
                            Text(text = "Cari Siswa", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("NIS, NISN, atau Nama...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color(0xFF64748B))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            )
                        }

                        // Big Blue Button: [ Filter ]
                        Button(
                            onClick = { /* Live filtered */ },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Filter", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mini Stats Row: 7 Hadir, 2 Terlambat, 1 Izin, 1 Sakit, 0 Alfa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AttendanceStatBox(count = hadirCount, label = "Hadir", color = StatusGreen, bg = Color(0xFFDCFCE7), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    AttendanceStatBox(count = terlambatCount, label = "Terlambat", color = StatusOrange, bg = Color(0xFFFEF3C7), modifier = Modifier.weight(1.1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    AttendanceStatBox(count = izinCount, label = "Izin", color = StatusBlue, bg = Color(0xFFDBEAFE), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    AttendanceStatBox(count = sakitCount, label = "Sakit", color = StatusPurple, bg = Color(0xFFF3E8FF), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    AttendanceStatBox(count = alfaCount, label = "Alfa", color = StatusRed, bg = Color(0xFFFEE2E2), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Collapsible Accordion: Koreksi / Catat Kehadiran Manual
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isManualAccordionOpen = !isManualAccordionOpen }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Koreksi / Catat Kehadiran Manual",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isManualAccordionOpen) "Tutup" else "Buka/Tutup",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Icon(
                                    if (isManualAccordionOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = isManualAccordionOpen) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Divider(color = Color(0xFFF1F5F9))

                                // Select Student
                                Box {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { isStudentSelectOpen = true },
                                        color = Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val selectedStudent = allStudents.firstOrNull { it.student.id == manualStudentId }
                                            Text(
                                                text = selectedStudent?.student?.nama ?: "Pilih Siswa...",
                                                fontSize = 12.sp,
                                                color = if (selectedStudent != null) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                            )
                                            Text("▾", fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isStudentSelectOpen,
                                        onDismissRequest = { isStudentSelectOpen = false }
                                    ) {
                                        allStudents.forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text("${s.student.nama} (${s.classEntity?.namaKelas ?: ""})") },
                                                onClick = {
                                                    manualStudentId = s.student.id
                                                    isStudentSelectOpen = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Status selection chips
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Hadir", "Terlambat", "Izin", "Sakit", "Alfa").forEach { st ->
                                        val isSelected = manualStatus == st
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { manualStatus = st },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = st,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) Color.White else Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // Keterangan input
                                OutlinedTextField(
                                    value = manualKeterangan,
                                    onValueChange = { manualKeterangan = it },
                                    placeholder = { Text("Keterangan (opsional)...", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                )

                                Button(
                                    onClick = {
                                        manualStudentId?.let { sId ->
                                            val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                            viewModel.saveAttendance(
                                                studentId = sId,
                                                tanggal = selectedDate,
                                                status = manualStatus,
                                                jamMasuk = if (manualStatus in listOf("Hadir", "Terlambat")) nowTime else null,
                                                keterangan = manualKeterangan.takeIf { it.isNotBlank() }
                                            )
                                            manualKeterangan = ""
                                            isManualAccordionOpen = false
                                        }
                                    },
                                    enabled = manualStudentId != null,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Simpan Kehadiran Manual", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section Header: Daftar Kehadiran (X Catatan) - Date Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Kehadiran (${filteredAttendances.size} Catatan)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Text(
                            text = headerDateFormatted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Attendance Records List (Detailed Table-Style Cards)
            itemsIndexed(filteredAttendances, key = { _, item -> item.attendance.id }) { index, item ->
                DetailedAttendanceTableCard(
                    no = index + 1,
                    attendanceItem = item,
                    isAdmin = currentUser.role == "admin",
                    onEdit = { viewModel.openEditAttendanceDialog(item) },
                    onDelete = { viewModel.deleteAttendance(item.attendance) }
                )
            }

            // Footer Notice
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "© 2026 SMK Tritech Informatika Medan • Sistem Absensi Siswa QR Code",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Scroll to Top FAB (Blue circular arrow up button)
        if (showScrollToTop) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                shape = CircleShape,
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }

        // Edit Attendance Dialog
        attendanceToEdit?.let { att ->
            AttendanceEditDialog(
                attendanceDetails = att,
                isAdmin = currentUser.role == "admin",
                onDismiss = { viewModel.closeEditAttendanceDialog() },
                onSave = { entity, newStatus, keterangan ->
                    viewModel.saveAttendanceStatus(entity, newStatus, keterangan)
                },
                onDelete = { entity ->
                    viewModel.deleteAttendance(entity)
                }
            )
        }
    }
}

@Composable
fun DetailedAttendanceTableCard(
    no: Int,
    attendanceItem: AttendanceWithDetails,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val attendance = attendanceItem.attendance
    val student = attendanceItem.studentWithClass?.student
    val classEntity = attendanceItem.studentWithClass?.classEntity

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: NO X on left, STUD0X on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NO  $no",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Text(
                        text = "STUD%02d".format(student?.id ?: 0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Row: NAMA SISWA
            TableRow(label = "NAMA SISWA") {
                Text(
                    text = student?.nama ?: "Siswa Tidak Dikenal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
            }

            // Row: NIS
            TableRow(label = "NIS") {
                Text(
                    text = "R.${student?.nis?.removePrefix("R.") ?: "-"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A)
                )
            }

            // Row: KELAS
            TableRow(label = "KELAS") {
                Text(
                    text = classEntity?.namaKelas ?: "X RPL 1",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
            }

            // Row: TANGGAL
            TableRow(label = "TANGGAL") {
                val dateFormatted = try {
                    val p = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(attendance.tanggal)
                    if (p != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(p) else attendance.tanggal
                } catch (e: Exception) {
                    attendance.tanggal
                }
                Text(
                    text = dateFormatted,
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }

            // Row: JAM MASUK
            TableRow(label = "JAM MASUK") {
                if (attendance.jamMasuk != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Text(
                            text = "${attendance.jamMasuk.take(5)} WIB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(text = "-", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }

            // Row: STATUS
            TableRow(label = "STATUS") {
                StatusBadge(status = attendance.status)
            }

            // Row: LOKASI (Item 9 User Specification: Admin location detail)
            TableRow(label = "LOKASI") {
                if (attendance.distanceFromSchool != null) {
                    val dist = attendance.distanceFromSchool.toInt()
                    val acc = attendance.accuracy?.toInt() ?: 0
                    val isInside = dist <= 100 || attendance.geofenceStatus == "DI_SEKOLAH"

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isInside) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isInside) Color(0xFFBBF7D0) else Color(0xFFFECACA)
                                )
                            ) {
                                Text(
                                    text = if (isInside) "✓ $dist m dari sekolah" else "⚠ $dist m (Di luar)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isInside) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (acc > 0) {
                            Text(
                                text = "Akurasi GPS: ±${acc}m",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                } else {
                    Text(text = "Area Sekolah (Valid)", fontSize = 12.sp, color = Color(0xFF16A34A))
                }
            }

            // Row: KETERANGAN
            TableRow(label = "KETERANGAN") {
                Text(
                    text = attendance.keterangan ?: "-",
                    fontSize = 12.sp,
                    color = if (attendance.keterangan != null) Color(0xFF0F172A) else Color(0xFF94A3B8)
                )
            }

            // Row: AKSI
            if (isAdmin) {
                TableRow(label = "AKSI") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionSquareButton(
                            icon = Icons.Default.Edit,
                            tint = Color(0xFFD97706),
                            bg = Color(0xFFFEF3C7),
                            onClick = onEdit
                        )
                        ActionSquareButton(
                            icon = Icons.Default.Delete,
                            tint = StatusRed,
                            bg = Color(0xFFFEE2E2),
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceStatBox(
    count: Int,
    label: String,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                maxLines = 1
            )
        }
    }
}
