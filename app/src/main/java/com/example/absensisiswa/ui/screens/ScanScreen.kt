package com.example.absensisiswa.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.components.ScanResultDialog
import com.example.absensisiswa.ui.components.StatusBadge
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.ui.theme.StatusGreen
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scanResult by viewModel.scanResult.collectAsState()
    val isScanningActive by viewModel.isScanningActive.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val recentAttendances by viewModel.recentAttendances.collectAsState()

    var manualCodeInput by remember { mutableStateOf("") }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var cameraRestartTrigger by remember { mutableStateOf(0) }
    var isCameraActive by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 }
    }

    val locationResult by viewModel.locationResult.collectAsState()
    val isCheckingLocation by viewModel.isCheckingLocation.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isStudent = currentUser.role == "siswa"

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            viewModel.verifyLocation(context, forceRefresh = true)
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val scanner = BarcodeScanning.getClient()
                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val rawVal = barcode.rawValue
                                if (!rawVal.isNullOrBlank()) {
                                    viewModel.processBarcode(rawVal)
                                    break
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_laser_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("scan_screen")
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

                // Absensi Siswa Card (Frame 00:00 & 00:11)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Absensi Siswa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Scan QR Code untuk mencatat kehadiran",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pill Chip: Batas Tepat Waktu
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Batas Tepat Waktu: ${settings?.schoolStartTime?.take(5) ?: "07:00"} WIB",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryBlue
                                )
                            }
                        }

                        // ============================================================
                        // GEOLOCATION VERIFICATION CARD (Item 5 User Specification)
                        // ============================================================
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    locationResult == null || isCheckingLocation -> Color(0xFFF8FAFC)
                                    locationResult?.isSuccess == true -> Color(0xFFF0FDF4)
                                    else -> Color(0xFFFFF1F2)
                                }
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = when {
                                    locationResult == null || isCheckingLocation -> BorderLight
                                    locationResult?.isSuccess == true -> Color(0xFFBBF7D0)
                                    else -> Color(0xFFFECDD3)
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when {
                                    !hasLocationPermission -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOff,
                                                contentDescription = null,
                                                tint = Color(0xFFE11D48),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Izin Lokasi Diperlukan",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF9F1239)
                                            )
                                        }
                                        Text(
                                            text = "Absensi membutuhkan akses lokasi untuk memastikan Anda berada di area sekolah.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 16.sp
                                        )
                                        Button(
                                            onClick = {
                                                locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(38.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Izinkan Akses Lokasi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    isCheckingLocation -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = PrimaryBlue
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "📍 Memeriksa lokasi GPS Anda...",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PrimaryBlue
                                            )
                                        }
                                    }

                                    locationResult != null && locationResult?.isSuccess == true -> {
                                        val dist = locationResult?.distanceFromSchoolMeters?.toInt() ?: 0
                                        val acc = locationResult?.accuracyMeters?.toInt() ?: 0

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "📍 Lokasi Terverifikasi",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF15803D)
                                                    )
                                                    Text(
                                                        text = "Anda berada di area sekolah",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF166534)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "DI SEKOLAH",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF16A34A),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        // Diagnostic details only for admin
                                        if (currentUser.role == "admin") {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Admin Telemetri: Jarak $dist m • Akurasi ±$acc m",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        // If student, provide direct Absen Sekarang button
                                        if (isStudent) {
                                            Button(
                                                onClick = {
                                                    viewModel.recordCurrentStudentAttendance(context)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(42.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Absen Sekarang", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewModel.verifyLocation(context, forceRefresh = true) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(34.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A))
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Perbarui Lokasi GPS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    locationResult != null && locationResult?.isSuccess == false -> {
                                        val dist = locationResult?.distanceFromSchoolMeters?.toInt() ?: 0
                                        val allowedRad = settings?.schoolRadiusMeters?.toInt() ?: 100

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOff,
                                                contentDescription = null,
                                                tint = Color(0xFFE11D48),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "📍 Di luar area sekolah",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF9F1239)
                                            )
                                        }

                                        Text(
                                            text = "Jarak dari sekolah: $dist meter (Maksimal: $allowedRad meter)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFBE123C)
                                        )

                                        Text(
                                            text = locationResult?.message ?: "Anda harus berada di area sekolah untuk melakukan absensi.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF881337),
                                            lineHeight = 15.sp
                                        )

                                        Button(
                                            onClick = { viewModel.verifyLocation(context, forceRefresh = true) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(38.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Coba Lagi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    else -> {
                                        // Not yet checked
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.GpsFixed,
                                                    contentDescription = null,
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Verifikasi Lokasi Sekolah",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = { viewModel.verifyLocation(context, forceRefresh = true) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(34.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                                            ) {
                                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Cek Lokasi", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scanner Camera Label & Status Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scanner Kamera",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isCameraActive) StatusGreen else Color(0xFF3B82F6))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCameraActive) "Kamera Aktif" else "Memulai Kamera...",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dark Camera Viewfinder Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasCameraPermission) {
                                androidx.compose.runtime.key(cameraRestartTrigger) {
                                    AndroidView(
                                        factory = { ctx ->
                                            val previewView = PreviewView(ctx)
                                            val cameraExecutor = Executors.newSingleThreadExecutor()
                                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                                            cameraProviderFuture.addListener({
                                                val cameraProvider = cameraProviderFuture.get()
                                                val preview = Preview.Builder().build().also {
                                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                                }

                                                val barcodeScanner = BarcodeScanning.getClient()
                                                val imageAnalysis = ImageAnalysis.Builder()
                                                    .setTargetResolution(Size(1280, 720))
                                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                    .build()

                                                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                                    val mediaImage = imageProxy.image
                                                    if (mediaImage != null && isScanningActive) {
                                                        val image = InputImage.fromMediaImage(
                                                            mediaImage,
                                                            imageProxy.imageInfo.rotationDegrees
                                                        )
                                                        barcodeScanner.process(image)
                                                            .addOnSuccessListener { barcodes ->
                                                                for (barcode in barcodes) {
                                                                    val rawValue = barcode.rawValue
                                                                    if (!rawValue.isNullOrBlank()) {
                                                                        viewModel.processBarcode(rawValue)
                                                                        break
                                                                    }
                                                                }
                                                            }
                                                            .addOnCompleteListener {
                                                                imageProxy.close()
                                                            }
                                                    } else {
                                                        imageProxy.close()
                                                    }
                                                }

                                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                                try {
                                                    cameraProvider.unbindAll()
                                                    val camera = cameraProvider.bindToLifecycle(
                                                        lifecycleOwner,
                                                        cameraSelector,
                                                        preview,
                                                        imageAnalysis
                                                    )
                                                    camera.cameraControl.enableTorch(isTorchEnabled)
                                                    isCameraActive = true
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    isCameraActive = false
                                                }
                                            }, ContextCompat.getMainExecutor(ctx))

                                            previewView
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Kamera belum diizinkan",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    OutlinedButton(
                                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Text("Beri Izin Kamera", fontSize = 12.sp)
                                    }
                                }
                            }

                            // Viewfinder Reticle Overlay with cyan corner brackets
                            Box(
                                modifier = Modifier
                                    .size(190.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF38BDF8).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                val bracketColor = Color(0xFF38BDF8)
                                val bracketSize = 22.dp
                                val bracketThickness = 3.dp

                                Box(modifier = Modifier.align(Alignment.TopStart).size(bracketSize).border(bracketThickness, bracketColor, RoundedCornerShape(topStart = 8.dp)))
                                Box(modifier = Modifier.align(Alignment.TopEnd).size(bracketSize).border(bracketThickness, bracketColor, RoundedCornerShape(topEnd = 8.dp)))
                                Box(modifier = Modifier.align(Alignment.BottomStart).size(bracketSize).border(bracketThickness, bracketColor, RoundedCornerShape(bottomStart = 8.dp)))
                                Box(modifier = Modifier.align(Alignment.BottomEnd).size(bracketSize).border(bracketThickness, bracketColor, RoundedCornerShape(bottomEnd = 8.dp)))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .padding(horizontal = 8.dp)
                                        .align(Alignment.TopCenter)
                                        .padding(top = (180.dp * scanLineProgress))
                                        .background(Color(0xFF38BDF8))
                                )
                            }

                            // Flashlight Toggle
                            if (hasCameraPermission) {
                                IconButton(
                                    onClick = { isTorchEnabled = !isTorchEnabled },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.4f))
                                ) {
                                    Icon(
                                        if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = "Flashlight",
                                        tint = if (isTorchEnabled) Color(0xFFFACC15) else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "◉ Posisikan QR Code di dalam area",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Menunggu QR Code...",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: [ Restart Kamera ] & [ Scan dari Foto ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    cameraRestartTrigger++
                                    viewModel.resumeScanning()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restart Kamera", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan dari Foto", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ID Siswa / QR Code / NIS Card (Frame 00:12 in video)
            item {
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
                        Text(
                            text = "ID Siswa / QR Code / NIS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualCodeInput,
                                onValueChange = { manualCodeInput = it },
                                placeholder = { Text("cth: STU001, QR-S1...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )

                            Button(
                                onClick = {
                                    if (manualCodeInput.isNotBlank()) {
                                        viewModel.processBarcode(manualCodeInput.trim())
                                        manualCodeInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Catat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Absensi Terkini Hari Ini Section (Frame 00:13 - 00:15)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Absensi Terkini Hari Ini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Text(
                        text = "Lihat Semua >",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(AppScreen.PRESENSI) }
                    )
                }
            }

            // List of today's scanned students
            if (recentAttendances.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada absensi yang tercatat hari ini",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(recentAttendances) { item ->
                    val student = item.studentWithClass?.student
                    val classEntity = item.studentWithClass?.classEntity

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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Badge STUDXX
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Text(
                                        text = "STUD%02d".format(student?.id ?: 0),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = student?.nama ?: "Siswa",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${classEntity?.namaKelas ?: "X RPL 1"} • ${item.attendance.jamMasuk?.take(5) ?: "-"} WIB",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            StatusBadge(status = item.attendance.status)
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

    // Scan Result Popup Dialog
    scanResult?.let { result ->
        ScanResultDialog(
            result = result,
            onDismiss = {
                viewModel.clearScanResult()
                viewModel.resumeScanning()
            }
        )
    }
}
