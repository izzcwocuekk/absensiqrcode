package com.example.absensisiswa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.absensisiswa.data.entity.ClassEntity
import com.example.absensisiswa.data.entity.StudentEntity
import com.example.absensisiswa.ui.theme.PrimaryBlue

@Composable
fun StudentFormDialog(
    studentToEdit: StudentEntity?,
    classList: List<ClassEntity>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        nis: String,
        nisn: String?,
        nama: String,
        classId: Long,
        jenisKelamin: String,
        existingQrToken: String?
    ) -> Unit
) {
    var nis by remember { mutableStateOf(studentToEdit?.nis ?: "") }
    var nisn by remember { mutableStateOf(studentToEdit?.nisn ?: "") }
    var nama by remember { mutableStateOf(studentToEdit?.nama ?: "") }
    var selectedClassId by remember {
        mutableLongStateOf(studentToEdit?.classId ?: (classList.firstOrNull()?.id ?: 1L))
    }
    var jenisKelamin by remember { mutableStateOf(studentToEdit?.jenisKelamin ?: "L") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    val selectedClassName = classList.find { it.id == selectedClassId }?.namaKelas ?: "Pilih Kelas"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("student_form_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (studentToEdit == null) "Tambah Data Siswa" else "Ubah Data Siswa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Batal")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // NIS
                OutlinedTextField(
                    value = nis,
                    onValueChange = { nis = it },
                    label = { Text("NIS (Nomor Induk Siswa) *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_nis_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // NISN
                OutlinedTextField(
                    value = nisn,
                    onValueChange = { nisn = it },
                    label = { Text("NISN (Opsional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_nisn_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Nama Siswa
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap Siswa *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Kelas Dropdown
                Text(
                    text = "Kelas *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = selectedClassName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { classDropdownExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { classDropdownExpanded = true }
                            .testTag("student_class_dropdown")
                    )

                    DropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        classList.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.namaKelas} - ${c.jurusan}") },
                                onClick = {
                                    selectedClassId = c.id
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Jenis Kelamin
                Text(
                    text = "Jenis Kelamin *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { jenisKelamin = "L" }
                            .padding(end = 16.dp)
                    ) {
                        RadioButton(
                            selected = jenisKelamin == "L",
                            onClick = { jenisKelamin = "L" }
                        )
                        Text("Laki-laki (L)", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { jenisKelamin = "P" }
                    ) {
                        RadioButton(
                            selected = jenisKelamin == "P",
                            onClick = { jenisKelamin = "P" }
                        )
                        Text("Perempuan (P)", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (nis.isBlank()) {
                                errorMessage = "NIS tidak boleh kosong"
                                return@Button
                            }
                            if (nama.isBlank()) {
                                errorMessage = "Nama siswa tidak boleh kosong"
                                return@Button
                            }
                            onSave(
                                studentToEdit?.id ?: 0L,
                                nis,
                                nisn,
                                nama,
                                selectedClassId,
                                jenisKelamin,
                                studentToEdit?.qrToken
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_student_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
