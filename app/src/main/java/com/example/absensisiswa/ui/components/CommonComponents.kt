package com.example.absensisiswa.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensisiswa.R
import com.example.absensisiswa.ui.theme.BrandBorder
import com.example.absensisiswa.ui.theme.BrandGreen
import com.example.absensisiswa.ui.theme.BrandGreenContainer
import com.example.absensisiswa.ui.theme.BrandGreenDark
import com.example.absensisiswa.ui.theme.BrandMagenta
import com.example.absensisiswa.ui.theme.BrandMagentaContainer
import com.example.absensisiswa.ui.theme.BrandMuted
import com.example.absensisiswa.ui.theme.BrandSuccess
import com.example.absensisiswa.ui.theme.BrandSuccessBg
import com.example.absensisiswa.ui.theme.BrandWarning
import com.example.absensisiswa.ui.theme.BrandWarningBg
import com.example.absensisiswa.ui.theme.BrandError
import com.example.absensisiswa.ui.theme.BrandErrorBg
import com.example.absensisiswa.ui.theme.BrandInfo
import com.example.absensisiswa.ui.theme.BrandInfoBg

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "hadir" -> BrandSuccessBg to BrandSuccess
        "terlambat" -> BrandWarningBg to BrandWarning
        "izin" -> BrandInfoBg to BrandInfo
        "sakit" -> BrandMagentaContainer to BrandMagenta
        "alfa", "alpa" -> BrandErrorBg to BrandError
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
    val bgColor = if (gender == "P") BrandMagenta else BrandGreen

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

@Composable
fun TriTechBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Int = 36
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(logoSize.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = BrandGreenContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.tritech_logo),
                    contentDescription = "Logo TriTech",
                    modifier = Modifier.size((logoSize * 0.82).dp)
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
}

@Composable
fun GeofenceStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "DI_SEKOLAH" -> Triple(BrandSuccessBg, BrandSuccess, "DI SEKOLAH")
        "DI_LUAR_AREA" -> Triple(BrandErrorBg, BrandError, "DI LUAR AREA")
        else -> Triple(BrandWarningBg, BrandWarning, "PERLU VERIFIKASI")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
