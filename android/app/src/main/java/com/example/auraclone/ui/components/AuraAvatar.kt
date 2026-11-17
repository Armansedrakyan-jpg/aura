package com.example.auraclone.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.ui.theme.AuraColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuraAvatar(
    username: String,
    avatarFrame: String?,
    avatarEffect: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val frameKey = avatarFrame?.uppercase() ?: "DEFAULT"
    val effectKey = avatarEffect?.uppercase()

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val frameColors = when (frameKey) {
        "NEON" -> listOf(AuraColors.ElectricCyan, AuraColors.PlasmaViolet, AuraColors.ElectricCyan)
        "GOLD" -> listOf(AuraColors.SolarGold, Color(0xFFFF9F1C), Color(0xFFFFF1A8), AuraColors.SolarGold)
        "CYBER" -> listOf(AuraColors.HyperMagenta, AuraColors.ElectricCyan, AuraColors.PlasmaViolet, AuraColors.HyperMagenta)
        "SIGMA" -> listOf(Color(0xFFF3F4F6), Color(0xFF9CA3AF), Color.White)
        "LEGENDARY" -> listOf(AuraColors.SolarGold, AuraColors.HyperMagenta, AuraColors.ElectricCyan, AuraColors.PlasmaViolet, AuraColors.SolarGold)
        else -> listOf(Color(0xFF64479B), Color(0xFF331D5E))
    }

    val borderWidth = when (frameKey) {
        "LEGENDARY" -> 3.5.dp
        "NEON", "GOLD", "CYBER", "SIGMA" -> 2.5.dp
        else -> 1.5.dp
    }

    val glowColor = frameColors.first()
    val initial = username.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (!effectKey.isNullOrBlank()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = this.size.minDimension / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val particleCount = if (effectKey.contains("SPARK")) 8 else 6
                val particleColor = when {
                    effectKey.contains("GOLD") -> Color(0xFFFFD700)
                    effectKey.contains("FIRE") -> Color(0xFFFF5722)
                    else -> Color(0xFF00FEFC)
                }
                for (i in 0 until particleCount) {
                    val angle = (2.0 * PI * i / particleCount) + pulse * 2.0 * PI
                    val dist = radius * 0.96f
                    val px = center.x + (dist * cos(angle)).toFloat()
                    val py = center.y + (dist * sin(angle)).toFloat()
                    drawCircle(
                        color = particleColor.copy(alpha = 0.25f),
                        radius = (size.toPx() * 0.085f).coerceAtLeast(4f),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = particleColor.copy(alpha = 0.92f),
                        radius = (size.toPx() * 0.045f).coerceAtLeast(2.5f),
                        center = Offset(px, py)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (effectKey.isNullOrBlank()) 0.dp else 4.dp)
                .shadow(
                    elevation = if (frameKey == "DEFAULT") 0.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2E145C), Color(0xFF120726))
                    )
                )
                .border(
                    width = borderWidth,
                    brush = Brush.sweepGradient(frameColors),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.38f).sp,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
