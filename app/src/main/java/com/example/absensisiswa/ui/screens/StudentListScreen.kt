package com.example.absensisiswa.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.data.model.StudentWithClass
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.components.StudentCardDialog
import com.example.absensisiswa.ui.components.StudentFormDialog
import com.example.absensisiswa.ui.theme.BorderLight
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.ui.theme.StatusGreen
import com.example.absensisiswa.ui.theme.StatusRed
import kotlinx.coroutines.launch

@Composable
fun StudentListScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val selectedClassFilter by viewModel.selectedClassFilter.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val cardStudentToShow by viewModel.studentCardToShow.collectAsState()
    val isStudentDialogOpen by viewModel.isStudentDialogOpen.collectAsState()
    val studentToEdit by viewModel.studentToEdit.collectAsState()

    var statusFilter by remember { mutableStateOf("Semua Status") }
    var isStatusMenuOpen by remember { mutableStateOf(false) }
    var isClassMenuOpen by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val filteredStudents = remember(allStudents, searchQuery, selectedClassFilter, statusFilter) {
        allStudents.filter { item ->
            val matchClass = selectedClassFilter == 0L || item.student.classId == selectedClassFilter
            val matchSearch = searchQuery.isBlank() ||
                    item.student.nama.contains(searchQuery, ignoreCase = true) ||
                    item.student.nis.contains(searchQuery, ignoreCase = true) ||
                    (item.student.nisn?.contains(searchQuery, ignoreCase = true) == true)
            val matchStatus = when (statusFilter) {
                "Aktif" -> item.student.isActive
                "Non-Aktif" -> !item.student.isActive
                else -> true
            }
            matchClass && matchSearch && matchStatus
        }
    }

    val maleCount = remember(allStudents) { allStudents.count { it.student.jenisKelamin.equals("L", ignoreCase = true) } }
    val femaleCount = remember(allStudents) { allStudents.count { it.student.jenisKelamin.equals("P", ignoreCase = true) } }
    val activeCount = remember(allStudents) { allStudents.count { it.student.isActive } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("student_list_screen")
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

                // Header: Data Siswa (Frame 00:16 in video)
                Text(
                    text = "Data Siswa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Kelola data siswa, status aktif, dan QR Code presensi.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: [ Generate Semua QR ] & [ + Tambah Siswa ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            allStudents.firstOrNull()?.let { viewModel.showStudentCard(it) }
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
                            text = "Generate Semua QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { viewModel.openAddStudentDialog() },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Tambah Siswa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar: Cari Student ID, NIS, atau Nama
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setStudentSearchQuery(it) },
                    placeholder = { Text("Cari Student ID, NIS, atau Nama", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
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
                            IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
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

                // Filter Dropdowns Row: Kelas & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Class Filter Dropdown
                    Box(modifier = Modifier.weight(1f)) {
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
                                val selectedClassName = if (selectedClassFilter == 0L) "Semua Kelas"
                                else allClasses.firstOrNull { it.id == selectedClassFilter }?.namaKelas ?: "Semua Kelas"
                                Text(
                                    text = selectedClassName,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                                Text("▾", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }

                        DropdownMenu(
                            expanded = isClassMenuOpen,
                            onDismissRequest = { isClassMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Kelas") },
                                onClick = {
                                    viewModel.setSelectedClassFilter(0L)
                                    isClassMenuOpen = false
                                }
                            )
                            allClasses.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.namaKelas) },
                                    onClick = {
                                        viewModel.setSelectedClassFilter(cls.id)
                                        isClassMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Status Filter Dropdown
                    Box(modifier = Modifier.weight(1f)) {
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
                                Text(
                                    text = statusFilter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                                Text("▾", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }

                        DropdownMenu(
                            expanded = isStatusMenuOpen,
                            onDismissRequest = { isStatusMenuOpen = false }
                        ) {
                            listOf("Semua Status", "Aktif", "Non-Aktif").forEach { statusOption ->
                                DropdownMenuItem(
                                    text = { Text(statusOption) },
                                    onClick = {
                                        statusFilter = statusOption
                                        isStatusMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Filter Button
                    Button(
                        onClick = { /* Live filter applied */ },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Filter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Menampilkan 25 siswa (22 Laki-laki, 3 Perempuan, 25 Aktif)
                Text(
                    text = "Menampilkan ${filteredStudents.size} siswa ($maleCount Laki-laki, $femaleCount Perempuan, $activeCount Aktif)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )
            }

            // Detailed Table-Style Student Cards (Frame 00:16 - 00:24)
            itemsIndexed(filteredStudents, key = { _, item -> item.student.id }) { index, item ->
                DetailedStudentTableCard(
                    no = index + 1,
                    studentWithClass = item,
                    isAdmin = currentUser.role == "admin",
                    onViewCard = { viewModel.showStudentCard(item) },
                    onEdit = { viewModel.openEditStudentDialog(item.student) },
                    onDelete = { viewModel.deleteStudent(item.student) }
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

        // Student Card Dialog
        cardStudentToShow?.let { studentWithClass ->
            StudentCardDialog(
                studentWithClass = studentWithClass,
                schoolName = settings?.schoolName ?: "SMK TRITECH INFORMATIKA MEDAN",
                onDismiss = { viewModel.showStudentCard(null) },
                onRegenerateQr = { viewModel.regenerateQrToken(studentWithClass.student.id) }
            )
        }

        // Add/Edit Student Dialog
        if (isStudentDialogOpen) {
            StudentFormDialog(
                studentToEdit = studentToEdit,
                classList = allClasses,
                onDismiss = { viewModel.closeStudentDialog() },
                onSave = { id, nis, nisn, nama, classId, jk, token ->
                    viewModel.saveStudent(id, nis, nisn, nama, classId, jk, token)
                }
            )
        }
    }
}

@Composable
fun DetailedStudentTableCard(
    no: Int,
    studentWithClass: StudentWithClass,
    isAdmin: Boolean,
    onViewCard: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val student = studentWithClass.student
    val classEntity = studentWithClass.classEntity

    // Derived login username (e.g. adevito.rpl1)
    val loginUsername = remember(student.nama, classEntity?.namaKelas) {
        val firstPart = student.nama.split(" ").firstOrNull()?.lowercase() ?: "siswa"
        val classPart = classEntity?.namaKelas?.lowercase()?.replace(" ", "") ?: "rpl1"
        "$firstPart.$classPart"
    }

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
                        text = "STUD%02d".format(student.id),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = student.nama,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            // Row: NIS / NISN
            TableRow(label = "NIS / NISN") {
                Column {
                    Text(
                        text = "R.${student.nis.removePrefix("R.")}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "NISN: ${student.nisn ?: "-"}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
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

            // Row: AKUN LOGIN
            TableRow(label = "AKUN LOGIN") {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Text(
                        text = loginUsername,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Row: QR CODE
            TableRow(label = "QR CODE") {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.clickable(onClick = onViewCard)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "QR-STU%02d".format(student.id),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Row: L/P
            TableRow(label = "L/P") {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (student.jenisKelamin == "P") Color(0xFFFCE7F3) else Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = student.jenisKelamin,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (student.jenisKelamin == "P") Color(0xFFBE185D) else PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Row: STATUS
            TableRow(label = "STATUS") {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StatusGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (student.isActive) "Aktif" else "Non-Aktif",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen
                        )
                    }
                }
            }

            // Row: AKSI (4 square action buttons: Badge, QR, Edit, Delete)
            TableRow(label = "AKSI") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. ID Card / Badge Icon
                    ActionSquareButton(
                        icon = Icons.Default.Badge,
                        tint = PrimaryBlue,
                        bg = Color(0xFFEFF6FF),
                        onClick = onViewCard
                    )
                    // 2. QR Code Icon
                    ActionSquareButton(
                        icon = Icons.Default.QrCode,
                        tint = Color(0xFF475569),
                        bg = Color(0xFFF1F5F9),
                        onClick = onViewCard
                    )
                    // 3. Edit Pencil Icon
                    if (isAdmin) {
                        ActionSquareButton(
                            icon = Icons.Default.Edit,
                            tint = Color(0xFFD97706),
                            bg = Color(0xFFFEF3C7),
                            onClick = onEdit
                        )
                        // 4. Delete Trash Icon
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
fun TableRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B),
            modifier = Modifier.width(96.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
fun ActionSquareButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
