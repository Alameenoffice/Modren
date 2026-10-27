package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Brand Palette (SaaS Financial Grade)
val NavyPrimary = Color(0xFF0F172A)
val NavySecondary = Color(0xFF1E293B)
val NavySurfaceDark = Color(0xFF1E293B)
val NavyBackgroundDark = Color(0xFF0B1120)

val SlateBackgroundLight = Color(0xFFF8FAFC)
val SurfaceWhite = Color(0xFFFFFFFF)
val SlateBorderLight = Color(0xFFE2E8F0)
val SlateBorderDark = Color(0xFF334155)

val RoyalBlue = Color(0xFF2563EB)
val RoyalBlueLight = Color(0xFFDBEAFE)
val RoyalBlueDark = Color(0xFF1E3A8A)

val EmeraldGreen = Color(0xFF10B981)
val EmeraldGreenSoft = Color(0xFFD1FAE5)
val EmeraldGreenDarkSoft = Color(0xFF064E3B)

val CrimsonRed = Color(0xFFEF4444)
val CrimsonRedSoft = Color(0xFFFEE2E2)
val CrimsonRedDarkSoft = Color(0xFF7F1D1D)

val AmberWarning = Color(0xFFF59E0B)
val AmberWarningSoft = Color(0xFFFEF3C7)
val AmberWarningDarkSoft = Color(0xFF78350F)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextMutedLight = Color(0xFF64748B)

val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFFCBD5E1)
val TextMutedDark = Color(0xFF94A3B8)

@Immutable
data class LedgerSemanticColors(
    val isDark: Boolean,
    val background: Color,
    val cardSurface: Color,
    val elevatedSurface: Color,
    val sidebarSurface: Color,
    val subtleBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryInteractive: Color,
    val primaryInteractiveSoft: Color,
    val paymentEmerald: Color,
    val paymentEmeraldSoft: Color,
    val debtCrimson: Color,
    val debtCrimsonSoft: Color,
    val warningAmber: Color,
    val warningAmberSoft: Color
)

val LightLedgerColors = LedgerSemanticColors(
    isDark = false,
    background = SlateBackgroundLight,
    cardSurface = SurfaceWhite,
    elevatedSurface = Color(0xFFF1F5F9),
    sidebarSurface = NavyPrimary,
    subtleBorder = SlateBorderLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted = TextMutedLight,
    primaryInteractive = RoyalBlue,
    primaryInteractiveSoft = RoyalBlueLight,
    paymentEmerald = EmeraldGreen,
    paymentEmeraldSoft = EmeraldGreenSoft,
    debtCrimson = CrimsonRed,
    debtCrimsonSoft = CrimsonRedSoft,
    warningAmber = AmberWarning,
    warningAmberSoft = AmberWarningSoft
)

val DarkLedgerColors = LedgerSemanticColors(
    isDark = true,
    background = NavyBackgroundDark,
    cardSurface = NavySurfaceDark,
    elevatedSurface = Color(0xFF283548),
    sidebarSurface = Color(0xFF090D16),
    subtleBorder = SlateBorderDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark,
    primaryInteractive = Color(0xFF3B82F6),
    primaryInteractiveSoft = RoyalBlueDark,
    paymentEmerald = Color(0xFF34D399),
    paymentEmeraldSoft = EmeraldGreenDarkSoft,
    debtCrimson = Color(0xFFF87171),
    debtCrimsonSoft = CrimsonRedDarkSoft,
    warningAmber = Color(0xFFFBBF24),
    warningAmberSoft = AmberWarningDarkSoft
)

val LocalLedgerColors = staticCompositionLocalOf { LightLedgerColors }
