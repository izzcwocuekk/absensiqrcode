package com.example.absensisiswa.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.data.model.StudentWithClass
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.components.StudentCardDialog
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.util.QrCodeUtils
import kotlinx.coroutines.launch

@Composable
fun QrSiswaScreen(
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by viewModel.allStudents.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val cardStudentToShow by viewModel.studentCardToShow.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf(0L) }
    var isClassMenuOpen by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val filteredStudents = remember(allStudents, searchQuery, selectedClassId) {
        allStudents.filter { item ->
            val matchClass = selectedClassId == 0L || item.student.classId == selectedClassId
            val matchSearch = searchQuery.isBlank() ||
                    item.student.nama.contains(searchQuery, ignoreCase = true) ||
                    item.student.nis.contains(searchQuery, ignoreCase = true) ||
                    (item.student.nisn?.contains(searchQuery, ignoreCase = true) == true)
            matchClass && matchSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("qr_siswa_screen")
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

                // Title & Subtitle (Frame 00:35 in video)
                Text(
                    text = "Manajemen QR Siswa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Kelola token QR Code identitas unik siswa untuk sistem presensi sekolah.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row: [ Generate Semua File PNG ] & [ Buka Scanner ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Mempersiapkan export QR Code...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Generate Semua File PNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { onNavigate(AppScreen.SCAN_QR) },
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Buka Scanner",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box: Cari NIS, NISN, atau Nama Siswa
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari NIS, NISN, atau Nama Siswa", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Row: Kelas Dropdown + Filter Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isClassMenuOpen = true },
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
                                val selectedClassName = if (selectedClassId == 0L) "Semua Kelas"
                                else allClasses.firstOrNull { it.id == selectedClassId }?.namaKelas ?: "Semua Kelas"
                                Text(
                                    text = selectedClassName,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text("▾", fontSize = 13.sp, color = Color(0xFF64748B))
                            }
                        }

                        DropdownMenu(
                            expanded = isClassMenuOpen,
                            onDismissRequest = { isClassMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Kelas") },
                                onClick = {
                                    selectedClassId = 0L
                                    isClassMenuOpen = false
                                }
                            )
                            allClasses.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.namaKelas) },
                                    onClick = {
                                        selectedClassId = cls.id
                                        isClassMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { /* Filter is live */ },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Filter", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // List of Large QR Cards (Frame 00:35 - 00:41)
            items(filteredStudents, key = { it.student.id }) { item ->
                LargeQrStudentCard(
                    studentWithClass = item,
                    onViewCard = { viewModel.showStudentCard(item) },
                    onRegenerate = { viewModel.regenerateQrToken(item.student.id) }
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

        // Student Card Dialog
        cardStudentToShow?.let { studentWithClass ->
            StudentCardDialog(
                studentWithClass = studentWithClass,
                schoolName = settings?.schoolName ?: "SMK TRITECH INFORMATIKA MEDAN",
                onDismiss = { viewModel.showStudentCard(null) },
                onRegenerateQr = { viewModel.regenerateQrToken(studentWithClass.student.id) }
            )
        }
    }
}

@Composable
fun LargeQrStudentCard(
    studentWithClass: StudentWithClass,
    onViewCard: () -> Unit,
    onRegenerate: () -> Unit
) {
    val context = LocalContext.current
    val student = studentWithClass.student
    val classEntity = studentWithClass.classEntity

    val qrBitmap = remember(student.qrToken, student.nis) {
        val token = if (student.qrToken.isNotBlank()) student.qrToken else student.nis
        QrCodeUtils.generateQrBitmap(token, 260)
    }

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Left badge STU001, Right badge QR-STU001
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Text(
                        text = "STU%03d".format(student.id),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Text(
                        text = "QR-STU%03d".format(student.id),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large QR Code in Center
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code ${student.nama}",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Student Name (Uppercase bold)
            Text(
                text = student.nama,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Class and NIS
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Text(
                    text = "${classEntity?.namaKelas ?: "-"}  •  NIS: ${student.nis}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: [ Unduh ] [ Kartu ] [ Regenerate ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "QR ${student.nama} diunduh", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF334155))
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Unduh", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onViewCard,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF334155))
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kartu", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onRegenerate,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBCFE8)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBE185D))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Regenerate", fontSize = 10.sp)
                }
            }
        }
    }
}
