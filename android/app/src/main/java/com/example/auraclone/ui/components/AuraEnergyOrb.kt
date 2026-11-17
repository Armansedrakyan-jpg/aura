package com.example.auraclone.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.ui.theme.AuraColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuraEnergyOrb(
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    score: Int? = null,
    subtitle: String? = null,
    isScanning: Boolean = false,
    accentColors: List<Color> = score?.let { AuraColors.scoreGradient(it) }
        ?: listOf(AuraColors.ElectricCyan, AuraColors.PlasmaViolet, AuraColors.HyperMagenta)
) {
    var targetScore by remember { mutableIntStateOf(0) }
    LaunchedEffect(score) { if (score != null) targetScore = score }
    val animatedScore by animateIntAsState(
        targetValue = targetScore,
        animationSpec = tween(1350, easing = EaseOutCubic),
        label = "aura_score_counter"
    )
    val inf = rememberInfiniteTransition(label = "energy_orb")
    val rot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(if (isScanning) 2600 else 7500, easing = LinearEasing)),
        label = "rot"
    )
    val cRot by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(if (isScanning) 3800 else 11000, easing = LinearEasing)),
        label = "cRot"
    )
    val breathe by inf.animateFloat(
        0.90f, 1.06f,
        infiniteRepeatable(tween(if (isScanning) 900 else 2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathe"
    )

    val c1 = accentColors.firstOrNull() ?: AuraColors.ElectricCyan
    val c2 = accentColors.getOrElse(1) { AuraColors.PlasmaViolet }
    val c3 = accentColors.getOrElse(2) { AuraColors.HyperMagenta }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val d = this.size.minDimension
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            drawCircle(
                brush = Brush.radialGradient(listOf(c1.copy(0.32f), c2.copy(0.14f), Color.Transparent), c, (d / 2f) * breathe),
                radius = (d / 2f) * breathe, center = c
            )
            val r1 = d * 0.42f
            val s1 = Size(r1 * 2f, r1 * 2f)
            val tl1 = Offset(c.x - r1, c.y - r1)
            drawCircle(Color.White.copy(0.07f), r1, c, style = Stroke(1.5.dp.toPx()))
            drawArc(Brush.sweepGradient(listOf(c1, c2, c3, c1), c), rot, 220f, false, tl1, s1, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))

            val r2 = d * 0.34f
            val s2 = Size(r2 * 2f, r2 * 2f)
            val tl2 = Offset(c.x - r2, c.y - r2)
            drawArc(c3.copy(0.78f), cRot, 140f, false, tl2, s2, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
            drawArc(c1.copy(0.65f), cRot + 180f, 95f, false, tl2, s2, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))

            val rCore = d * 0.29f
            drawCircle(Brush.radialGradient(listOf(AuraColors.DeepNebula.copy(0.96f), AuraColors.VoidBlack.copy(0.98f)), c, rCore), rCore, c)
            drawCircle(Brush.linearGradient(listOf(Color.White.copy(0.25f), c1.copy(0.35f))), rCore, c, style = Stroke(1.2.dp.toPx()))

            for (i in 0 until 6) {
                val rad = (rot + i * 60f) * (PI / 180.0)
                val orbit = if (i % 2 == 0) r1 * 1.07f else r2 * 0.93f
                drawCircle(
                    color = (if (i % 2 == 0) c1 else c3).copy(0.9f),
                    radius = if (i % 2 == 0) 3.2.dp.toPx() else 2.2.dp.toPx(),
                    center = Offset(c.x + (orbit * cos(rad)).toFloat(), c.y + (orbit * sin(rad)).toFloat())
                )
            }
        }
        OrbCenterContent(score = score, animatedScore = animatedScore, subtitle = subtitle, accent = c1, size = size)
    }
}


@Composable
private fun OrbCenterContent(
    score: Int?,
    animatedScore: Int,
    subtitle: String?,
    accent: Color,
    size: Dp
) {
    if (score != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = (size.value * 0.24f).sp,
                    lineHeight = (size.value * 0.25f).sp
                ),
                fontWeight = FontWeight.Black,
                color = AuraColors.TextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
        }
    } else if (!subtitle.isNullOrBlank()) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = AuraColors.TextPrimary
        )
    }
}
