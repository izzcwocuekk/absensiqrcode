package com.example.absensisiswa.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TriTech Brand Design System Tokens
 * Sampled directly from the official TriTech logo:
 * - Emerald Green: Symbolizing growth, technology, integrity
 * - Deep Magenta / Orchid Violet: Symbolizing innovation, telematics, high quality
 * - Clean White & Soft Slate: Clean, readable educational surface
 */

// Primary Brand Color: TriTech Emerald Green
val BrandGreen = Color(0xFF007A3D)
val BrandGreenDark = Color(0xFF005F2F)
val BrandGreenLight = Color(0xFF10B981)
val BrandGreenContainer = Color(0xFFE8F5E9)
val BrandGreenBorder = Color(0xFFA7F3D0)

// Secondary Brand Color: TriTech Deep Magenta / Orchid Violet
val BrandMagenta = Color(0xFF8F2D6B)
val BrandMagentaDark = Color(0xFF732055)
val BrandMagentaLight = Color(0xFFB94C8D)
val BrandMagentaContainer = Color(0xFFFDF2F8)
val BrandMagentaBorder = Color(0xFFFBCFE8)

// Accent Color
val BrandAccent = Color(0xFF00A859)
val BrandAccentLight = Color(0xFF34D399)

// Surfaces & Backgrounds
val BrandBackground = Color(0xFFF6F8F6)
val BrandSurface = Color(0xFFFFFFFF)
val BrandSurfaceVariant = Color(0xFFF1F5F9)
val BrandBorder = Color(0xFFE2E8F0)
val BrandBorderFocus = BrandGreen

// Text Hierarchy
val BrandText = Color(0xFF0F172A)
val BrandTextSecondary = Color(0xFF475569)
val BrandMuted = Color(0xFF64748B)

// Functional Status Tokens
val BrandSuccess = Color(0xFF16A34A)
val BrandSuccessBg = Color(0xFFDCFCE7)

val BrandWarning = Color(0xFFD97706)
val BrandWarningBg = Color(0xFFFEF3C7)

val BrandError = Color(0xFFDC2626)
val BrandErrorBg = Color(0xFFFEE2E2)

val BrandInfo = Color(0xFF2563EB)
val BrandInfoBg = Color(0xFFDBEAFE)

// Backwards compatibility aliases for existing codebase
val PrimaryBlue = BrandGreen
val PrimaryBlueDark = BrandGreenDark
val PrimaryBlueLight = BrandGreenLight

val SecondaryTeal = BrandMagenta
val SecondaryTealLight = BrandMagentaLight

val StatusGreen = BrandSuccess
val StatusGreenBg = BrandSuccessBg
val StatusOrange = BrandWarning
val StatusOrangeBg = BrandWarningBg
val StatusBlue = BrandInfo
val StatusBlueBg = BrandInfoBg
val StatusPurple = BrandMagenta
val StatusPurpleBg = BrandMagentaContainer
val StatusRed = BrandError
val StatusRedBg = BrandErrorBg

val BackgroundLight = BrandBackground
val SurfaceLight = BrandSurface
val SurfaceVariantLight = BrandSurfaceVariant
val TextPrimary = BrandText
val TextSecondary = BrandTextSecondary
val TextMuted = BrandMuted
val BorderLight = BrandBorder
