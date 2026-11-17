package com.example.auraclone.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.auraclone.ui.theme.AuraColors

@Composable
fun AuraScaffold(
    modifier: Modifier = Modifier,
    primaryOrbColor: Color = AuraColors.PlasmaViolet,
    secondaryOrbColor: Color = AuraColors.ElectricCyan,
    tertiaryOrbColor: Color = AuraColors.HyperMagenta,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura_bg")
    val driftX by infiniteTransition.animateFloat(
        initialValue = -0.06f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX"
    )
    val driftY by infiniteTransition.animateFloat(
        initialValue = -0.05f,
        targetValue = 0.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraColors.BackgroundGradient)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val orb1Center = Offset(
                x = w * (0.18f + driftX),
                y = h * (0.14f + driftY)
            )
            val orb1Radius = w * 0.72f * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryOrbColor.copy(alpha = 0.28f),
                        primaryOrbColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = orb1Center,
                    radius = orb1Radius
                ),
                radius = orb1Radius,
                center = orb1Center
            )

            val orb2Center = Offset(
                x = w * (0.86f - driftX * 0.9f),
                y = h * (0.34f - driftY)
            )
            val orb2Radius = w * 0.65f * (2f - pulse * 0.95f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryOrbColor.copy(alpha = 0.20f),
                        secondaryOrbColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = orb2Radius
                ),
                radius = orb2Radius,
                center = orb2Center
            )

            val orb3Center = Offset(
                x = w * (0.45f + driftY * 0.8f),
                y = h * (0.88f - driftX * 0.6f)
            )
            val orb3Radius = w * 0.78f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tertiaryOrbColor.copy(alpha = 0.16f),
                        primaryOrbColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = orb3Center,
                    radius = orb3Radius
                ),
                radius = orb3Radius,
                center = orb3Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            if (topBar != null) {
                topBar()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                content()
            }
        }

        if (bottomBar != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                bottomBar()
            }
        }
    }
}
