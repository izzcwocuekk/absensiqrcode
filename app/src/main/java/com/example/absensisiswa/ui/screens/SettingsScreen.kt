package com.example.absensisiswa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.util.LocationUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var schoolName by remember(settings) { mutableStateOf(settings?.schoolName ?: "SMK TRITECH INFORMATIKA MEDAN") }
    var schoolAddress by remember(settings) { mutableStateOf(settings?.schoolAddress ?: "Jl. Bhayangkara No. 434, Medan") }
    var jamMasuk by remember(settings) { mutableStateOf(settings?.schoolStartTime?.take(5) ?: "07:15") }
    var toleransiTerlambat by remember(settings) { mutableStateOf(settings?.lateAfter?.take(5) ?: "07:16") }

    // Geolocation & Geofence Settings
    var schoolLat by remember(settings) { mutableStateOf((settings?.schoolLatitude ?: LocationUtils.DEFAULT_SCHOOL_LAT).toString()) }
    var schoolLon by remember(settings) { mutableStateOf((settings?.schoolLongitude ?: LocationUtils.DEFAULT_SCHOOL_LON).toString()) }
    var schoolRadius by remember(settings) { mutableStateOf((settings?.schoolRadiusMeters ?: LocationUtils.DEFAULT_SCHOOL_RADIUS_METERS).toInt().toString()) }
    var maxGpsAcc by remember(settings) { mutableStateOf((settings?.maxGpsAccuracyMeters ?: LocationUtils.DEFAULT_MAX_ACCURACY_METERS).toInt().toString()) }
    var requireLocation by remember(settings) { mutableStateOf(settings?.requireLocationForAttendance ?: true) }
    var simulationMode by remember(settings) { mutableStateOf(settings?.simulationModeEnabled ?: false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("settings_screen")
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

                // Title & Subtitle (Frame 00:52 in video)
                Text(
                    text = "Pengaturan Sistem",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Konfigurasi identitas sekolah dan parameter waktu presensi.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Card: Identitas Sekolah & Presensi
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
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Identitas Sekolah & Presensi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        // Nama Sekolah Field
                        Column {
                            Text(text = "Nama Sekolah", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = schoolName,
                                onValueChange = { schoolName = it },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Alamat Sekolah Field
                        Column {
                            Text(text = "Alamat Sekolah", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = schoolAddress,
                                onValueChange = { schoolAddress = it },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Jam Masuk Sekolah Field
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Jam Masuk Sekolah", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = jamMasuk,
                                onValueChange = { jamMasuk = it },
                                singleLine = true,
                                placeholder = { Text("07:15") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Jam resmi kegiatan belajar mengajar dimulai.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Batas Toleransi Terlambat Field
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Batas Toleransi Terlambat", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = toleransiTerlambat,
                                onValueChange = { toleransiTerlambat = it },
                                singleLine = true,
                                placeholder = { Text("07:16") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Scan lewat jam ini otomatis berstatus Terlambat.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card: Konfigurasi Geolocation & Radius Presensi
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
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Konfigurasi Geolocation & Radius",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Validasi kehadiran siswa berbasis titik GPS sekolah",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Koordinat Sekolah: Latitude & Longitude
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Latitude Sekolah", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = schoolLat,
                                    onValueChange = { schoolLat = it },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = BorderLight,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Longitude Sekolah", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = schoolLon,
                                    onValueChange = { schoolLon = it },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = BorderLight,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Radius Sekolah (Meter)
                        Column {
                            Text(text = "Radius Area Sekolah (Meter)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = schoolRadius,
                                onValueChange = { schoolRadius = it },
                                singleLine = true,
                                placeholder = { Text("100") },
                                trailingIcon = { Text("meter", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(end = 12.dp)) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Siswa berjarak di atas radius ini otomatis ditolak saat absensi.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Batas Akurasi GPS (Meter)
                        Column {
                            Text(text = "Batas Akurasi GPS Maksimal (Meter)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = maxGpsAcc,
                                onValueChange = { maxGpsAcc = it },
                                singleLine = true,
                                placeholder = { Text("80") },
                                trailingIcon = { Text("meter", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(end = 12.dp)) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Menghindari sinyal GPS palsu atau akurasi sinyal yang terlalu rendah.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Toggle: Wajib Verifikasi Lokasi
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Wajib Verifikasi Geolocation",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Tolak absensi jika siswa berada di luar radius sekolah.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Switch(
                                checked = requireLocation,
                                onCheckedChange = { requireLocation = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBlue)
                            )
                        }

                        // Toggle: Mode Simulasi Lokasi (Dev / Demo)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mode Simulasi Lokasi (Demo)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Simulasikan lokasi berada di sekolah saat uji coba di emulator.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Switch(
                                checked = simulationMode,
                                onCheckedChange = { simulationMode = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBlue)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Button: [ Simpan Semua Pengaturan ]
                        Button(
                            onClick = {
                                viewModel.updateSettings(
                                    schoolName = schoolName.trim(),
                                    schoolStartTime = if (jamMasuk.length == 5) "$jamMasuk:00" else jamMasuk,
                                    lateAfter = if (toleransiTerlambat.length == 5) "$toleransiTerlambat:00" else toleransiTerlambat,
                                    schoolAddress = schoolAddress.trim(),
                                    schoolLatitude = schoolLat.toDoubleOrNull() ?: LocationUtils.DEFAULT_SCHOOL_LAT,
                                    schoolLongitude = schoolLon.toDoubleOrNull() ?: LocationUtils.DEFAULT_SCHOOL_LON,
                                    schoolRadiusMeters = schoolRadius.toFloatOrNull() ?: LocationUtils.DEFAULT_SCHOOL_RADIUS_METERS,
                                    maxGpsAccuracyMeters = maxGpsAcc.toFloatOrNull() ?: LocationUtils.DEFAULT_MAX_ACCURACY_METERS,
                                    requireLocationForAttendance = requireLocation,
                                    simulationModeEnabled = simulationMode
                                )
                                Toast.makeText(context, "Pengaturan sistem & lokasi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan Semua Pengaturan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Below: School Crest & Dynamic DB Info Callout
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
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = schoolName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = schoolAddress,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Green Callout Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Parameter jam diatur dinamis di database sehingga tidak ada nilai waktu yang di-hardcode di kode aplikasi.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF15803D),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Footer Notice
                Spacer(modifier = Modifier.height(14.dp))
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
