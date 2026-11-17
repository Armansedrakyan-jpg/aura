package com.example.auraclone.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AuraColors {
    // Глубокие космические фоны (2026 Dark Mode)
    val VoidBlack = Color(0xFF05030D)
    val AbyssViolet = Color(0xFF0C061F)
    val DeepNebula = Color(0xFF150A30)

    // Стеклянные поверхности
    val ObsidianGlass = Color(0xB3130A29)
    val ElevatedGlass = Color(0xCC1E113D)
    val SubtleGlass = Color(0x66170D30)
    val GlassHighlight = Color(0x2EFFFFFF)
    val GlassBorderSoft = Color(0x33FFFFFF)

    // Неоновые акценты ауры
    val ElectricCyan = Color(0xFF00F5FF)
    val PlasmaViolet = Color(0xFFB829FF)
    val HyperMagenta = Color(0xFFFF2E93)
    val SolarGold = Color(0xFFFFD166)
    val EmeraldPulse = Color(0xFF34D399)
    val CrimsonAlert = Color(0xFFFF4D6D)

    // Типографика
    val TextPrimary = Color(0xFFF8F7FF)
    val TextSecondary = Color(0xFFB5AECB)
    val TextMuted = Color(0xFF7B7394)

    // Градиентные кисти
    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(AbyssViolet, VoidBlack)
    )

    val AuroraGradient = Brush.linearGradient(
        colors = listOf(ElectricCyan, PlasmaViolet, HyperMagenta)
    )

    val CyanVioletGradient = Brush.linearGradient(
        colors = listOf(ElectricCyan, PlasmaViolet)
    )

    val GoldAuraGradient = Brush.linearGradient(
        colors = listOf(SolarGold, Color(0xFFFF9F1C), Color(0xFFFFF1A8))
    )

    val GlassStrokeGradient = Brush.linearGradient(
        colors = listOf(
            ElectricCyan.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.14f),
            PlasmaViolet.copy(alpha = 0.55f)
        )
    )

    val DangerStrokeGradient = Brush.linearGradient(
        colors = listOf(
            CrimsonAlert.copy(alpha = 0.7f),
            HyperMagenta.copy(alpha = 0.45f)
        )
    )

    fun scoreAccentColor(score: Int): Color = when {
        score >= 90 -> SolarGold
        score >= 75 -> ElectricCyan
        score >= 55 -> PlasmaViolet
        else -> HyperMagenta
    }

    fun scoreGradient(score: Int): List<Color> = when {
        score >= 90 -> listOf(SolarGold, HyperMagenta, ElectricCyan)
        score >= 75 -> listOf(ElectricCyan, PlasmaViolet, EmeraldPulse)
        score >= 55 -> listOf(PlasmaViolet, ElectricCyan, HyperMagenta)
        else -> listOf(HyperMagenta, PlasmaViolet, Color(0xFFFF6B81))
    }
}
