package com.example.absensisiswa.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.R
import com.example.absensisiswa.ui.MainViewModel
import com.example.absensisiswa.ui.theme.BrandBorder
import com.example.absensisiswa.ui.theme.BrandError
import com.example.absensisiswa.ui.theme.BrandErrorBg
import com.example.absensisiswa.ui.theme.BrandGreen
import com.example.absensisiswa.ui.theme.BrandGreenDark
import com.example.absensisiswa.ui.theme.BrandMagenta
import com.example.absensisiswa.ui.theme.BrandMuted
import com.example.absensisiswa.ui.theme.BrandSurface
import com.example.absensisiswa.ui.theme.BrandText
import com.example.absensisiswa.ui.theme.BrandTextSecondary

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    fun doLogin() {
        focusManager.clearFocus()
        viewModel.login(username, password)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF8))
            .imePadding()
            .testTag("login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo TriTech Card
            Surface(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = BrandSurface,
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tritech_logo),
                        contentDescription = "Logo TriTech",
                        modifier = Modifier.size(76.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Brand Header
            Text(
                text = "TRITECH",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandGreenDark,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "School Attendance System",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandMagenta
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome texts
            Text(
                text = "Selamat Datang",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BrandText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Masuk ke sistem absensi",
                fontSize = 14.sp,
                color = BrandMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Feedback Alert (if any)
            if (loginError != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("login_error_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = BrandErrorBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandError.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = BrandError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = loginError ?: "",
                            color = BrandError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Input Fields Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = BrandSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Username Field
                    Text(
                        text = "Username",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        placeholder = { Text("Masukkan username Anda", fontSize = 13.sp, color = BrandMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            unfocusedBorderColor = BrandBorder,
                            focusedContainerColor = Color(0xFFF9FCFA),
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        placeholder = { Text("Masukkan kata sandi", fontSize = 13.sp, color = BrandMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Sembunyikan password" else "Tampilkan password",
                                    tint = BrandMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            unfocusedBorderColor = BrandBorder,
                            focusedContainerColor = Color(0xFFF9FCFA),
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { doLogin() }
                        )
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Submit Button [Masuk]
                    Button(
                        onClick = { doLogin() },
                        enabled = !isLoggingIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Masuk",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Demo Accounts Section
            Text(
                text = "Pilih Akun Demo:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAccountChip(
                    roleName = "Admin",
                    subtext = "admin",
                    modifier = Modifier.weight(1f),
                    onSelect = {
                        username = "admin"
                        password = "admin123"
                        viewModel.login("admin", "admin123")
                    }
                )
                QuickAccountChip(
                    roleName = "Guru",
                    subtext = "guru",
                    modifier = Modifier.weight(1f),
                    onSelect = {
                        username = "guru"
                        password = "guru123"
                        viewModel.login("guru", "guru123")
                    }
                )
                QuickAccountChip(
                    roleName = "Siswa",
                    subtext = "siswa",
                    modifier = Modifier.weight(1f),
                    onSelect = {
                        username = "siswa"
                        password = "siswa123"
                        viewModel.login("siswa", "siswa123")
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SMK TRITECH INFORMATIKA MEDAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = BrandMuted
            )
        }
    }
}

@Composable
private fun QuickAccountChip(
    roleName: String,
    subtext: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = BrandSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = roleName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandGreenDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = BrandMuted
            )
        }
    }
}
