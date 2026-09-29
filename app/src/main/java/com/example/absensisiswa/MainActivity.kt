package com.example.absensisiswa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.ui.AppScreen
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.screens.ClassListScreen
import com.example.absensisiswa.ui.screens.DashboardScreen
import com.example.absensisiswa.ui.screens.LaporanScreen
import com.example.absensisiswa.ui.screens.LoginScreen
import com.example.absensisiswa.ui.screens.PresensiScreen
import com.example.absensisiswa.ui.screens.ProfileScreen
import com.example.absensisiswa.ui.screens.QrSiswaScreen
import com.example.absensisiswa.ui.screens.ScanScreen
import com.example.absensisiswa.ui.screens.SettingsScreen
import com.example.absensisiswa.ui.screens.SplashScreen
import com.example.absensisiswa.ui.screens.StudentListScreen
import com.example.absensisiswa.ui.screens.SubjectManagementScreen
import com.example.absensisiswa.ui.screens.TeacherManagementScreen
import com.example.absensisiswa.ui.screens.TeacherScheduleScreen
import com.example.absensisiswa.ui.screens.TeacherSessionScreen
import com.example.absensisiswa.ui.theme.AbsensiSiswaQRTheme
import com.example.absensisiswa.ui.theme.BrandBorder
import com.example.absensisiswa.ui.theme.BrandGreen
import com.example.absensisiswa.ui.theme.BrandGreenContainer
import com.example.absensisiswa.ui.theme.BrandGreenDark
import com.example.absensisiswa.ui.theme.BrandMagenta
import com.example.absensisiswa.ui.theme.BrandMagentaContainer
import com.example.absensisiswa.ui.theme.BrandMuted
import com.example.absensisiswa.ui.theme.BrandSurface
import com.example.absensisiswa.ui.theme.BrandText
import com.example.absensisiswa.ui.theme.BrandTextSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as AbsensiApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AbsensiSiswaQRTheme {
                val isSplashScreenVisible by viewModel.isSplashScreenVisible.collectAsState()
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()

                if (isSplashScreenVisible) {
                    SplashScreen(
                        onTimeout = { viewModel.dismissSplashScreen() }
                    )
                } else if (!isLoggedIn) {
                    LoginScreen(viewModel = viewModel)
                } else {
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}

data class BottomBarItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val screen: AppScreen
)

data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val screen: AppScreen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Handle system back navigation to Dashboard or close drawer
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    // Role-specific bottom navigation strictly following Section 14
    val bottomNavItems = when (currentUser.role) {
        "siswa" -> listOf(
            BottomBarItem("home", "Home", Icons.Default.Dashboard, AppScreen.DASHBOARD),
            BottomBarItem("jadwal", "Jadwal", Icons.Default.CalendarMonth, AppScreen.JADWAL_PELAJARAN),
            BottomBarItem("riwayat", "Riwayat", Icons.Default.Assignment, AppScreen.PRESENSI),
            BottomBarItem("profil", "Profil", Icons.Default.Person, AppScreen.PROFIL)
        )
        "guru" -> listOf(
            BottomBarItem("home", "Home", Icons.Default.Dashboard, AppScreen.DASHBOARD),
            BottomBarItem("jadwal", "Jadwal", Icons.Default.CalendarMonth, AppScreen.JADWAL_PELAJARAN),
            BottomBarItem("absensi", "Absensi", Icons.Default.AssignmentTurnedIn, AppScreen.SESI_ABSENSI),
            BottomBarItem("riwayat", "Riwayat", Icons.Default.Assignment, AppScreen.PRESENSI),
            BottomBarItem("profil", "Profil", Icons.Default.Person, AppScreen.PROFIL)
        )
        else -> listOf(
            BottomBarItem("home", "Home", Icons.Default.Dashboard, AppScreen.DASHBOARD),
            BottomBarItem("absensi", "Absensi", Icons.Default.AssignmentTurnedIn, AppScreen.SESI_ABSENSI),
            BottomBarItem("data", "Data", Icons.Default.People, AppScreen.DATA_SISWA),
            BottomBarItem("laporan", "Laporan", Icons.Default.Assessment, AppScreen.LAPORAN),
            BottomBarItem("profil", "Profil", Icons.Default.Person, AppScreen.PROFIL)
        )
    }

    // Role-filtered drawer menu items
    val drawerMenuItems = when (currentUser.role) {
        "siswa" -> listOf(
            DrawerMenuItem("Beranda Siswa", Icons.Default.Dashboard, AppScreen.DASHBOARD),
            DrawerMenuItem("Jadwal Pelajaran", Icons.Default.CalendarMonth, AppScreen.JADWAL_PELAJARAN),
            DrawerMenuItem("Scan Presensi QR", Icons.Default.QrCodeScanner, AppScreen.SCAN_QR),
            DrawerMenuItem("Riwayat Presensi", Icons.Default.Assignment, AppScreen.PRESENSI),
            DrawerMenuItem("Kartu Pelajar QR", Icons.Default.QrCode, AppScreen.QR_SISWA),
            DrawerMenuItem("Profil Saya", Icons.Default.Person, AppScreen.PROFIL)
        )
        "guru" -> listOf(
            DrawerMenuItem("Dashboard Mengajar", Icons.Default.Dashboard, AppScreen.DASHBOARD),
            DrawerMenuItem("Jadwal Mengajar", Icons.Default.CalendarMonth, AppScreen.JADWAL_PELAJARAN),
            DrawerMenuItem("Sesi Absensi Kelas", Icons.Default.AssignmentTurnedIn, AppScreen.SESI_ABSENSI),
            DrawerMenuItem("Scan QR Siswa", Icons.Default.QrCodeScanner, AppScreen.SCAN_QR),
            DrawerMenuItem("Riwayat Absensi", Icons.Default.Assignment, AppScreen.PRESENSI),
            DrawerMenuItem("Profil Akun Guru", Icons.Default.Person, AppScreen.PROFIL)
        )
        else -> listOf(
            DrawerMenuItem("Dashboard Utama", Icons.Default.Dashboard, AppScreen.DASHBOARD),
            DrawerMenuItem("Sesi Absensi Guru", Icons.Default.AssignmentTurnedIn, AppScreen.SESI_ABSENSI),
            DrawerMenuItem("Scan QR Presensi", Icons.Default.QrCodeScanner, AppScreen.SCAN_QR),
            DrawerMenuItem("Data Siswa", Icons.Default.People, AppScreen.DATA_SISWA),
            DrawerMenuItem("Data Guru", Icons.Default.Person, AppScreen.DATA_GURU),
            DrawerMenuItem("Data Kelas", Icons.Default.Class, AppScreen.DATA_KELAS),
            DrawerMenuItem("Mata Pelajaran", Icons.Default.Book, AppScreen.DATA_MAPEL),
            DrawerMenuItem("Jadwal Pelajaran", Icons.Default.CalendarMonth, AppScreen.JADWAL_PELAJARAN),
            DrawerMenuItem("Kartu QR Siswa", Icons.Default.QrCode, AppScreen.QR_SISWA),
            DrawerMenuItem("Riwayat Presensi", Icons.Default.Assignment, AppScreen.PRESENSI),
            DrawerMenuItem("Laporan & Rekap", Icons.Default.Assessment, AppScreen.LAPORAN),
            DrawerMenuItem("Pengaturan", Icons.Default.Settings, AppScreen.PENGATURAN),
            DrawerMenuItem("Profil Akun", Icons.Default.Person, AppScreen.PROFIL)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BrandSurface,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // Header Drawer with TriTech Branding
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                color = BrandGreenContainer,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.tritech_logo),
                                        contentDescription = "Logo TriTech",
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TRITECH",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = BrandGreenDark,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "School Attendance System",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandMagenta
                                )
                            }
                        }

                        IconButton(
                            onClick = { scope.launch { drawerState.close() } }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = BrandMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(color = BrandBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Menu items list
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        drawerMenuItems.forEach { item ->
                            val isSelected = currentScreen == item.screen
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.navigateTo(item.screen)
                                        scope.launch { drawerState.close() }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BrandGreen else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) Color.White else BrandTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else BrandText
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = BrandBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Footer Drawer: User Profile & Quick Logout
                    val userInitial = currentUser.nama.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.navigateTo(AppScreen.PROFIL)
                                    scope.launch { drawerState.close() }
                                }
                        ) {
                            Text(
                                text = currentUser.nama,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BrandText,
                                maxLines = 1
                            )
                            Text(
                                text = currentUser.role.replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                color = BrandMuted
                            )
                        }

                        IconButton(
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.logout()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Keluar",
                                tint = BrandMagenta,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("topbar_menu_button")
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu Navigasi",
                                tint = BrandText
                            )
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = BrandGreenContainer
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.tritech_logo),
                                        contentDescription = "Logo TriTech",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TRITECH",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        ),
                                        color = BrandGreenDark,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (currentUser.role) {
                                            "siswa" -> BrandGreenContainer
                                            "guru" -> BrandMagentaContainer
                                            else -> Color(0xFFEFF6FF)
                                        }
                                    ) {
                                        Text(
                                            text = currentUser.role.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (currentUser.role) {
                                                "siswa" -> BrandGreenDark
                                                "guru" -> BrandMagenta
                                                else -> Color(0xFF1D4ED8)
                                            },
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "School Attendance System",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp
                                    ),
                                    color = BrandMuted
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick QR Scan Button
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.navigateTo(AppScreen.SCAN_QR) }
                                .testTag("topbar_quick_scan"),
                            color = BrandGreen
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan QR",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Avatar
                        val userInitial = currentUser.nama.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (currentUser.role == "guru") BrandMagenta else BrandGreen)
                                .clickable { viewModel.navigateTo(AppScreen.PROFIL) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BrandSurface,
                        titleContentColor = BrandText
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = BrandSurface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                viewModel.navigateTo(item.screen)
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandGreen,
                                selectedTextColor = BrandGreen,
                                indicatorColor = BrandGreenContainer,
                                unselectedIconColor = BrandMuted,
                                unselectedTextColor = BrandMuted
                            ),
                            modifier = Modifier.testTag("nav_${item.id}")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    AppScreen.SCAN_QR -> ScanScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    AppScreen.DATA_SISWA -> StudentListScreen(
                        viewModel = viewModel
                    )
                    AppScreen.QR_SISWA -> QrSiswaScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    AppScreen.DATA_KELAS -> ClassListScreen(
                        viewModel = viewModel
                    )
                    AppScreen.PRESENSI -> PresensiScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    AppScreen.LAPORAN -> LaporanScreen(
                        viewModel = viewModel
                    )
                    AppScreen.PENGATURAN -> SettingsScreen(
                        viewModel = viewModel
                    )
                    AppScreen.PROFIL -> ProfileScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    AppScreen.SESI_ABSENSI -> TeacherSessionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                    AppScreen.JADWAL_PELAJARAN -> TeacherScheduleScreen(
                        viewModel = viewModel,
                        onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                    AppScreen.DATA_GURU -> TeacherManagementScreen(
                        viewModel = viewModel,
                        onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                    AppScreen.DATA_MAPEL -> SubjectManagementScreen(
                        viewModel = viewModel,
                        onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                }
            }
        }
    }
}
