package com.example.absensisiswa.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.components.StudentAvatar
import com.example.absensisiswa.ui.components.StudentCardDialog
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.ui.theme.StatusGreen
import com.example.absensisiswa.ui.theme.StatusRed

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val cardStudentToShow by viewModel.studentCardToShow.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // If current user is student, find student details
    val currentStudent = remember(currentUser, allStudents) {
        if (currentUser.role == "siswa") {
            allStudents.firstOrNull { it.student.id == currentUser.studentId }
                ?: allStudents.firstOrNull { it.student.nama.contains(currentUser.nama, ignoreCase = true) }
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("profile_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Profil Pengguna",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Informasi akun dan preferensi sistem presensi.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Header Card
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
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        StudentAvatar(
                            name = currentUser.nama,
                            gender = if (currentUser.nama.contains("ALIKA", true) || currentUser.nama.contains("DEVIA", true)) "P" else "L",
                            size = 64
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentUser.nama,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Role Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (currentUser.role) {
                                "admin" -> Color(0xFFEFF6FF)
                                "guru" -> Color(0xFFDCFCE7)
                                else -> Color(0xFFFEF3C7)
                            }
                        ) {
                            Text(
                                text = when (currentUser.role) {
                                    "admin" -> "👑 ADMINISTRATOR"
                                    "guru" -> "👨‍🏫 GURU PIKET"
                                    else -> "🎓 SISWA • ${currentStudent?.classEntity?.namaKelas ?: "X RPL 1"}"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (currentUser.role) {
                                    "admin" -> PrimaryBlue
                                    "guru" -> StatusGreen
                                    else -> Color(0xFFD97706)
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (currentStudent != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "NIS: ${currentStudent.student.nis}  •  NISN: ${currentStudent.student.nisn ?: "-"}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { viewModel.showStudentCard(currentStudent) },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tampilkan Kartu Pelajar QR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Quick Role Switcher (Crucial for testing all user perspectives easily!)
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
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ganti Peran / Akun (Mode Cepat)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RoleOptionChip(
                                label = "Admin",
                                isSelected = currentUser.role == "admin",
                                onClick = { viewModel.switchUserRole("admin") },
                                modifier = Modifier.weight(1f)
                            )
                            RoleOptionChip(
                                label = "Guru",
                                isSelected = currentUser.role == "guru",
                                onClick = { viewModel.switchUserRole("guru") },
                                modifier = Modifier.weight(1f)
                            )
                            RoleOptionChip(
                                label = "Siswa",
                                isSelected = currentUser.role == "siswa",
                                onClick = { viewModel.switchUserRole("siswa") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // System Menus (Accessible by Admin/Guru)
            if (currentUser.role in listOf("admin", "guru")) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ProfileMenuItem(
                                title = "Pengaturan Sekolah & Presensi",
                                subtitle = "Jam masuk, toleransi keterlambatan",
                                icon = Icons.Default.Settings,
                                onClick = { onNavigate(AppScreen.PENGATURAN) }
                            )
                            Divider(color = Color(0xFFF1F5F9))
                            ProfileMenuItem(
                                title = "Laporan & Rekapitulasi",
                                subtitle = "Ekspor CSV & cetak data absensi",
                                icon = Icons.Default.Assessment,
                                onClick = { onNavigate(AppScreen.LAPORAN) }
                            )
                            Divider(color = Color(0xFFF1F5F9))
                            ProfileMenuItem(
                                title = "Manajemen QR Siswa",
                                subtitle = "Generate token & unduh file barcode",
                                icon = Icons.Default.QrCode,
                                onClick = { onNavigate(AppScreen.QR_SISWA) }
                            )
                            Divider(color = Color(0xFFF1F5F9))
                            ProfileMenuItem(
                                title = "Reset Data Demo SMK Tritech",
                                subtitle = "Kembalikan 25 siswa dan kelas awal",
                                icon = Icons.Default.Refresh,
                                onClick = { viewModel.reseedData() }
                            )
                        }
                    }
                }
            }

            // School Info
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
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = settings?.schoolName ?: "SMK TRITECH INFORMATIKA MEDAN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = settings?.schoolAddress ?: "Jl. Bhayangkara No. 434, Medan",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Versi Aplikasi", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("1.0.0 (Native Android)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Keamanan Scanner", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("CameraX + ML Kit Offline", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusGreen)
                        }
                    }
                }
            }

            // Logout Action
            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = StatusRed)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar dari Akun", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))
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

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold) },
                text = { Text("Apakah Anda yakin ingin keluar dari akun ${currentUser.nama}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                            Toast.makeText(context, "Berhasil keluar dari akun", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                    ) {
                        Text("Ya, Keluar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun RoleOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF334155)
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
    }
}
