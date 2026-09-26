package com.example.absensisiswa.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.example.absensisiswa.ui.MainViewModel
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
fun LaporanScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recentAttendances by viewModel.recentAttendances.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var startDate by remember { mutableStateOf(firstDayMonth) }
    var endDate by remember { mutableStateOf(today) }
    var selectedClassId by remember { mutableStateOf(0L) }
    var isClassMenuOpen by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val startDatePicker = DatePickerDialog(
        context,
        { _, y, m, d -> startDate = "%04d-%02d-%02d".format(y, m + 1, d) },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    val endCal = Calendar.getInstance()
    val endDatePicker = DatePickerDialog(
        context,
        { _, y, m, d -> endDate = "%04d-%02d-%02d".format(y, m + 1, d) },
        endCal.get(Calendar.YEAR),
        endCal.get(Calendar.MONTH),
        endCal.get(Calendar.DAY_OF_MONTH)
    )

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val p = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            if (p != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(p) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    val filteredAttendances = remember(recentAttendances, selectedClassId) {
        recentAttendances.filter { item ->
            selectedClassId == 0L || item.studentWithClass?.student?.classId == selectedClassId
        }
    }

    val hadirCount = filteredAttendances.count { it.attendance.status.equals("Hadir", ignoreCase = true) }
    val terlambatCount = filteredAttendances.count { it.attendance.status.equals("Terlambat", ignoreCase = true) }
    val izinCount = filteredAttendances.count { it.attendance.status.equals("Izin", ignoreCase = true) }
    val sakitCount = filteredAttendances.count { it.attendance.status.equals("Sakit", ignoreCase = true) }
    val alfaCount = filteredAttendances.count { it.attendance.status.equals("Alfa", ignoreCase = true) || it.attendance.status.equals("Alpa", ignoreCase = true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("laporan_screen")
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

                // Title & Subtitle (Frame 00:44 in video)
                Text(
                    text = "Laporan Rekap Absensi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Rekap kehadiran siswa per rentang tanggal dan ekspor data.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: [ Cetak Laporan ] & [ Ekspor CSV ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Mencetak Laporan Rekapitulasi...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Laporan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val csvData = buildString {
                                appendLine("No,Nama,NIS,Kelas,Tanggal,Jam Masuk,Status,Keterangan")
                                filteredAttendances.forEachIndexed { i, it ->
                                    val st = it.studentWithClass?.student
                                    val cl = it.studentWithClass?.classEntity
                                    appendLine("${i + 1},\"${st?.nama}\",\"${st?.nis}\",\"${cl?.namaKelas}\",\"${it.attendance.tanggal}\",\"${it.attendance.jamMasuk ?: ""}\",\"${it.attendance.status}\",\"${it.attendance.keterangan ?: ""}\"")
                                }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "Rekap Absensi Siswa")
                                putExtra(Intent.EXTRA_TEXT, csvData)
                            }
                            context.startActivity(Intent.createChooser(intent, "Ekspor Data CSV"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date Range & Filter Card (Frame 00:44)
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
                        // Dari Tanggal & Sampai Tanggal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dari Tanggal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { startDatePicker.show() },
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
                                        Text(text = formatDisplayDate(startDate), fontSize = 12.sp, color = Color(0xFF0F172A))
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sampai Tanggal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { endDatePicker.show() },
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
                                        Text(text = formatDisplayDate(endDate), fontSize = 12.sp, color = Color(0xFF0F172A))
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Filter Kelas
                        Column {
                            Text("Filter Kelas", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { isClassMenuOpen = true },
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
                                        val clsName = if (selectedClassId == 0L) "Semua Kelas"
                                        else allClasses.firstOrNull { it.id == selectedClassId }?.namaKelas ?: "Semua Kelas"
                                        Text(text = clsName, fontSize = 12.sp, color = Color(0xFF0F172A))
                                        Text("▾", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }

                                DropdownMenu(expanded = isClassMenuOpen, onDismissRequest = { isClassMenuOpen = false }) {
                                    DropdownMenuItem(text = { Text("Semua Kelas") }, onClick = { selectedClassId = 0L; isClassMenuOpen = false })
                                    allClasses.forEach { cls ->
                                        DropdownMenuItem(text = { Text(cls.namaKelas) }, onClick = { selectedClassId = cls.id; isClassMenuOpen = false })
                                    }
                                }
                            }
                        }

                        // Big Blue Button: [ Tampilkan ]
                        Button(
                            onClick = { /* Live updated */ },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tampilkan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Summary Grid (5 chips)
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

                // Data Rekap Header Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Data Rekap (${filteredAttendances.size} Catatan)  Periode: $startDate s/d $endDate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Records List
            itemsIndexed(filteredAttendances, key = { _, item -> item.attendance.id }) { index, item ->
                val student = item.studentWithClass?.student
                val classEntity = item.studentWithClass?.classEntity

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ${student?.nama ?: "Siswa"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            StatusBadge(status = item.attendance.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "NIS: ${student?.nis ?: "-"}  •  ${classEntity?.namaKelas ?: "-"}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "${item.attendance.tanggal} • ${item.attendance.jamMasuk?.take(5) ?: "-"} WIB",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        if (!item.attendance.keterangan.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ket: ${item.attendance.keterangan}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
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

        // Scroll to Top FAB (Floating Blue Arrow Up)
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
    }
}
