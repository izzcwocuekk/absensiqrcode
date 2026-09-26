package com.example.absensisiswa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.ui.theme.PrimaryBlue
import com.example.absensisiswa.ui.theme.StatusBlue
import com.example.absensisiswa.ui.theme.StatusBlueBg
import com.example.absensisiswa.ui.theme.StatusGreen
import com.example.absensisiswa.ui.theme.StatusGreenBg
import com.example.absensisiswa.ui.theme.StatusOrange
import com.example.absensisiswa.ui.theme.StatusOrangeBg
import com.example.absensisiswa.ui.theme.StatusPurple
import com.example.absensisiswa.ui.theme.StatusPurpleBg
import com.example.absensisiswa.ui.theme.StatusRed
import com.example.absensisiswa.ui.theme.StatusRedBg

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "hadir" -> StatusGreenBg to StatusGreen
        "terlambat" -> StatusOrangeBg to StatusOrange
        "izin" -> StatusBlueBg to StatusBlue
        "sakit" -> StatusPurpleBg to StatusPurple
        "alfa", "alpa" -> StatusRedBg to StatusRed
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun StudentAvatar(
    name: String,
    gender: String = "L",
    size: Int = 44,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    val bgColor = if (gender == "P") Color(0xFFF472B6) else PrimaryBlue

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.42).sp
        )
    }
}
