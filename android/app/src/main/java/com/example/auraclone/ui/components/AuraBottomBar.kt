package com.example.auraclone.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.ui.theme.AuraColors

enum class AuraTab(val label: String) {
    HOME("Главная"),
    LEADERBOARD("Рейтинг"),
    SHOP("Магазин"),
    PROFILE("Профиль")
}

@Composable
fun AuraBottomBar(
    currentTab: AuraTab,
    onSelectTab: (AuraTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = shape,
                ambientColor = AuraColors.PlasmaViolet.copy(alpha = 0.4f),
                spotColor = AuraColors.ElectricCyan.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xE6180E33), Color(0xF20D061E))),
                shape = shape
            )
            .border(width = 1.dp, brush = AuraColors.GlassStrokeGradient, shape = shape)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuraTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val tint by animateColorAsState(
                targetValue = if (selected) AuraColors.ElectricCyan else AuraColors.TextMuted,
                animationSpec = spring(stiffness = 400f),
                label = "tab_tint"
            )
            val pillColor by animateColorAsState(
                targetValue = if (selected) AuraColors.PlasmaViolet.copy(alpha = 0.24f) else Color.Transparent,
                animationSpec = spring(stiffness = 400f),
                label = "tab_pill"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillColor, RoundedCornerShape(20.dp))
                    .then(
                        if (selected) {
                            Modifier.border(0.8.dp, AuraColors.ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        } else Modifier
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        if (!selected) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectTab(tab)
                        }
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        drawTabIcon(tab = tab, color = tint, filled = selected)
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (selected) AuraColors.TextPrimary else AuraColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTabIcon(tab: AuraTab, color: Color, filled: Boolean) {
    val strokeWidth = if (filled) 2.1.dp.toPx() else 1.7.dp.toPx()
    val w = size.width
    val h = size.height

    when (tab) {
        AuraTab.HOME -> {
            val path = Path().apply {
                moveTo(w * 0.5f, h * 0.1f)
                lineTo(w * 0.9f, h * 0.45f)
                lineTo(w * 0.78f, h * 0.45f)
                lineTo(w * 0.78f, h * 0.88f)
                lineTo(w * 0.22f, h * 0.88f)
                lineTo(w * 0.22f, h * 0.45f)
                lineTo(w * 0.1f, h * 0.45f)
                close()
            }
            if (filled) drawPath(path, color = color.copy(alpha = 0.22f))
            drawPath(path, color = color, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        AuraTab.LEADERBOARD -> {
            val barWidth = w * 0.22f
            drawRoundRect(color, Offset(w * 0.1f, h * 0.48f), Size(barWidth, h * 0.4f), CornerRadius(3.dp.toPx()), style = Stroke(strokeWidth))
            if (filled) drawRoundRect(color.copy(alpha = 0.28f), Offset(w * 0.39f, h * 0.16f), Size(barWidth, h * 0.72f), CornerRadius(3.dp.toPx()))
            drawRoundRect(color, Offset(w * 0.39f, h * 0.16f), Size(barWidth, h * 0.72f), CornerRadius(3.dp.toPx()), style = Stroke(strokeWidth))
            drawRoundRect(color, Offset(w * 0.68f, h * 0.60f), Size(barWidth, h * 0.28f), CornerRadius(3.dp.toPx()), style = Stroke(strokeWidth))
        }
        AuraTab.SHOP -> {
            val path = Path().apply {
                moveTo(w * 0.25f, h * 0.18f)
                lineTo(w * 0.75f, h * 0.18f)
                lineTo(w * 0.92f, h * 0.42f)
                lineTo(w * 0.5f, h * 0.88f)
                lineTo(w * 0.08f, h * 0.42f)
                close()
            }
            if (filled) drawPath(path, color = color.copy(alpha = 0.24f))
            drawPath(path, color = color, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawLine(color, Offset(w * 0.08f, h * 0.42f), Offset(w * 0.92f, h * 0.42f), strokeWidth * 0.8f)
        }
        AuraTab.PROFILE -> {
            if (filled) drawCircle(color.copy(alpha = 0.24f), w * 0.19f, Offset(w * 0.5f, h * 0.30f))
            drawCircle(color, w * 0.19f, Offset(w * 0.5f, h * 0.30f), style = Stroke(strokeWidth))
            val arcPath = Path().apply {
                moveTo(w * 0.16f, h * 0.88f)
                quadraticTo(w * 0.18f, h * 0.60f, w * 0.50f, h * 0.60f)
                quadraticTo(w * 0.82f, h * 0.60f, w * 0.84f, h * 0.88f)
            }
            drawPath(arcPath, color = color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        }
    }
}
