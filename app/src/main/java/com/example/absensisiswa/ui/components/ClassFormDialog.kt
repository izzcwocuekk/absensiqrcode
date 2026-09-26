package com.example.absensisiswa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.absensisiswa.ui.theme.PrimaryBlue

@Composable
fun ClassFormDialog(
    classToEdit: ClassEntity?,
    onDismiss: () -> Unit,
    onSave: (id: Long, namaKelas: String, jurusan: String, tingkat: String) -> Unit
) {
    var namaKelas by remember { mutableStateOf(classToEdit?.namaKelas ?: "") }
    var jurusan by remember { mutableStateOf(classToEdit?.jurusan ?: "") }
    var tingkat by remember { mutableStateOf(classToEdit?.tingkat ?: "X") }
    var tingkatExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tingkatOptions = listOf("X", "XI", "XII")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("class_form_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (classToEdit == null) "Tambah Kelas" else "Ubah Kelas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Batal")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nama Kelas
                OutlinedTextField(
                    value = namaKelas,
                    onValueChange = { namaKelas = it },
                    label = { Text("Nama Kelas (contoh: X RPL 1) *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Jurusan
                OutlinedTextField(
                    value = jurusan,
                    onValueChange = { jurusan = it },
                    label = { Text("Jurusan (contoh: Rekayasa Perangkat Lunak) *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_jurusan_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tingkat
                Text(
                    text = "Tingkat *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = "Kelas $tingkat",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { tingkatExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tingkatExpanded = true }
                    )

                    DropdownMenu(
                        expanded = tingkatExpanded,
                        onDismissRequest = { tingkatExpanded = false }
                    ) {
                        tingkatOptions.forEach { t ->
                            DropdownMenuItem(
                                text = { Text("Kelas $t") },
                                onClick = {
                                    tingkat = t
                                    tingkatExpanded = false
                                }
                            )
                        }
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
                            if (namaKelas.isBlank()) {
                                errorMessage = "Nama kelas tidak boleh kosong"
                                return@Button
                            }
                            if (jurusan.isBlank()) {
                                errorMessage = "Jurusan tidak boleh kosong"
                                return@Button
                            }
                            onSave(classToEdit?.id ?: 0L, namaKelas, jurusan, tingkat)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_class_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
