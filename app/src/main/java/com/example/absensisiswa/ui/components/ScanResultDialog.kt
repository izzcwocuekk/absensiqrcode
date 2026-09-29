package com.example.absensisiswa.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.absensisiswa.R
import com.example.absensisiswa.data.ScanResult
import com.example.absensisiswa.ui.theme.BrandBorder
import com.example.absensisiswa.ui.theme.BrandGreen
import com.example.absensisiswa.ui.theme.BrandGreenContainer
import com.example.absensisiswa.ui.theme.BrandGreenDark
import com.example.absensisiswa.ui.theme.BrandMagenta
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

@Composable
fun ScanResultDialog(
    result: ScanResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("scan_result_dialog"),
            color = BrandSurface,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (result) {
                    is ScanResult.Success -> {
                        val isLate = result.status.equals("Terlambat", ignoreCase = true)
                        val iconColor = if (isLate) BrandWarning else BrandSuccess
                        val bgColor = if (isLate) BrandWarningBg else BrandSuccessBg

                        // TriTech Branding mini header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.tritech_logo),
                                contentDescription = "Logo TriTech",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRITECH ATTENDANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreenDark,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "✓ ABSENSI BERHASIL",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = iconColor,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = result.message,
                            fontSize = 12.sp,
                            color = BrandTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Required Structured Details:
                        // Nama, Kelas, Mata Pelajaran, Les, Jam
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ResultDetailRow(label = "Nama", value = result.student.student.nama, isBold = true)
                                ResultDetailRow(label = "Kelas", value = result.student.classEntity?.namaKelas ?: "X RPL 1")
                                ResultDetailRow(label = "Mata Pelajaran", value = "Presensi Harian / Sekolah")
                                ResultDetailRow(label = "Les", value = "Les 1 (Pagi)")
                                ResultDetailRow(label = "Jam", value = result.jam, highlightColor = BrandGreen)
                            }
                        }

                        // Simplified Geolocation info (Section 12)
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandGreenContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "📍 Lokasi Terverifikasi",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandGreenDark
                                    )
                                    Text(
                                        text = "Anda berada di area sekolah",
                                        fontSize = 11.sp,
                                        color = BrandGreenDark.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    is ScanResult.LocationRejected -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BrandErrorBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = BrandError,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "DI LUAR AREA SEKOLAH",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandError
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Absensi tidak dapat dicatat karena posisi Anda terdeteksi di luar radius sekolah.",
                            fontSize = 12.sp,
                            color = BrandTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandErrorBg,
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandError.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Status: DI LUAR AREA",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandError
                                )
                                Text(
                                    text = "Silakan berada di dalam area sekolah untuk melakukan absensi.",
                                    fontSize = 11.sp,
                                    color = BrandError
                                )
                            }
                        }
                    }

                    is ScanResult.AlreadyAttended -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BrandWarningBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = BrandWarning,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SUDAH TERCATAT HARI INI",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandWarning
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = result.message,
                            fontSize = 12.sp,
                            color = BrandTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = result.student.student.nama,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BrandText
                                )
                                Text(
                                    text = "${result.student.classEntity?.namaKelas ?: "Kelas"} • NIS: ${result.student.student.nis}",
                                    fontSize = 11.sp,
                                    color = BrandMuted
                                )
                            }
                        }
                    }

                    is ScanResult.NotFound -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BrandErrorBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = BrandError,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "QR TIDAK TERDAFTAR",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandError
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = result.message,
                            fontSize = 12.sp,
                            color = BrandTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    is ScanResult.StudentInactive -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BrandErrorBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = BrandError,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SISWA TIDAK AKTIF",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandError
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = result.message,
                            fontSize = 12.sp,
                            color = BrandTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    is ScanResult.Error -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BrandErrorBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = BrandError,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "PERINGATAN ABSENSI",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandError
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = result.message,
                            fontSize = 12.sp,
                            color = BrandTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_scan_result_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (result is ScanResult.LocationRejected) BrandError else BrandGreen
                    )
                ) {
                    Text(
                        text = when (result) {
                            is ScanResult.LocationRejected -> "Coba Lagi"
                            is ScanResult.Success -> "Selesai"
                            else -> "Tutup"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    highlightColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = BrandMuted
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = highlightColor ?: BrandText
        )
    }
}
