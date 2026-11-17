package com.example.auraclone.ui.screens

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.data.Battle
import com.example.auraclone.data.Player
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.components.*
import com.example.auraclone.ui.theme.AuraColors

@Composable
fun BattleScreen(viewModel: AuraViewModel, battleId: String, onBack: () -> Unit) {
    val battleState by viewModel.currentBattle.collectAsState()
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(battleId) {
        viewModel.listenToBattle(battleId)
    }

    AuraScaffold(
        primaryOrbColor = AuraColors.HyperMagenta,
        secondaryOrbColor = AuraColors.ElectricCyan
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "АРЕНА ДУЭЛЕЙ",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = AuraColors.HyperMagenta
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Битва Аур ⚔️",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = AuraColors.TextPrimary
            )
            Spacer(Modifier.height(10.dp))

            NeonChip(
                accentColor = if (copied) AuraColors.EmeraldPulse else AuraColors.ElectricCyan,
                onClick = {
                    clipboard.setText(AnnotatedString(battleId))
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    copied = true
                }
            ) {
                Text(
                    text = if (copied) "ID скопирован ✓" else "ID: $battleId  📋",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (copied) AuraColors.EmeraldPulse else AuraColors.ElectricCyan
                )
            }

            Spacer(Modifier.height(28.dp))

            val battle = battleState
            if (battle == null || battle.status == "WAITING") {
                WaitingArenaView()
                Spacer(Modifier.height(28.dp))
                NeonSecondaryButton(
                    text = "Покинуть комнату",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                FinishedArenaView(battle = battle, onBack = onBack)
            }
        }
    }
}

@Composable
private fun WaitingArenaView() {
    val inf = rememberInfiniteTransition(label = "radar")
    val ring1 by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "r1")
    val ring2 by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(1800, 900, easing = LinearEasing)), label = "r2")

    GlassCard(cornerRadius = 26.dp, glowColor = AuraColors.PlasmaViolet, contentPadding = PaddingValues(26.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(168.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.minDimension / 2f
                    drawCircle(AuraColors.ElectricCyan.copy(alpha = (1f - ring1) * 0.65f), maxR * ring1, c, style = Stroke(2.5.dp.toPx()))
                    drawCircle(AuraColors.PlasmaViolet.copy(alpha = (1f - ring2) * 0.65f), maxR * ring2, c, style = Stroke(2.5.dp.toPx()))
                }
                AuraEnergyOrb(size = 110.dp, subtitle = "SEARCH", isScanning = true)
            }
            Spacer(Modifier.height(16.dp))
            Text("Ожидание соперника...", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = AuraColors.TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text("Отправь ID дуэли другу — битва начнётся автоматически, как только он подключится.", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FinishedArenaView(battle: Battle, onBack: () -> Unit) {
    val p1 = battle.player1
    val p2 = battle.player2
    val s1 = (p1?.score ?: 0).coerceAtLeast(0)
    val s2 = (p2?.score ?: 0).coerceAtLeast(0)
    val total = (s1 + s2).coerceAtLeast(1)
    val ratio by animateFloatAsState((s1.toFloat() / total.toFloat()).coerceIn(0.12f, 0.88f), tween(1100), label = "tug")
    val p1Won = battle.winnerId != null && battle.winnerId == p1?.userId
    val p2Won = battle.winnerId != null && battle.winnerId == p2?.userId
    val winnerName = when { p1Won -> p1?.username ?: "Игрок 1"; p2Won -> p2?.username ?: "Игрок 2"; else -> "Ничья" }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            BattlePlayerCard(p1, p1Won, AuraColors.ElectricCyan, Modifier.weight(1f))
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(AuraColors.ElectricCyan, AuraColors.HyperMagenta)))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Text("VS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White) }
            BattlePlayerCard(p2, p2Won, AuraColors.HyperMagenta, Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        GlassCard(cornerRadius = 20.dp, contentPadding = PaddingValues(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$s1 pts", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = AuraColors.ElectricCyan)
                Text("ПЕРЕТЯГИВАНИЕ ЭНЕРГИИ", style = MaterialTheme.typography.labelSmall, color = AuraColors.TextMuted)
                Text("$s2 pts", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = AuraColors.HyperMagenta)
            }
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50)).background(AuraColors.HyperMagenta.copy(alpha = 0.85f))) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(ratio).background(Brush.horizontalGradient(listOf(AuraColors.ElectricCyan, AuraColors.PlasmaViolet))))
            }
        }
        Spacer(Modifier.height(20.dp))
        GlassCard(
            cornerRadius = 22.dp, glowColor = if (p1Won || p2Won) AuraColors.SolarGold else AuraColors.ElectricCyan,
            borderBrush = if (p1Won || p2Won) AuraColors.GoldAuraGradient else AuraColors.GlassStrokeGradient, contentPadding = PaddingValues(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ИТОГ ДУЭЛИ", style = MaterialTheme.typography.labelSmall, color = AuraColors.SolarGold)
                Spacer(Modifier.height(4.dp))
                Text(if (winnerName == "Ничья") "Ничья ⚡" else "Победитель: $winnerName 🏆", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(24.dp))
        NeonPrimaryButton(text = "В главное меню", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun BattlePlayerCard(
    player: Player?,
    isWinner: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 22.dp,
        glowColor = if (isWinner) AuraColors.SolarGold else accent,
        borderBrush = if (isWinner) AuraColors.GoldAuraGradient else Brush.linearGradient(listOf(accent.copy(0.75f), AuraColors.GlassBorderSoft)),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isWinner) {
                Text("👑 WINNER", style = MaterialTheme.typography.labelSmall, color = AuraColors.SolarGold)
                Spacer(Modifier.height(6.dp))
            }
            AuraAvatar(username = player?.username ?: "Игрок", avatarFrame = if (isWinner) "GOLD" else "NEON", size = 56.dp)
            Spacer(Modifier.height(10.dp))
            Text(player?.username ?: "Игрок", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text("${player?.score ?: 0} pts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = accent)
            if (!player?.title.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(player?.title ?: "", style = MaterialTheme.typography.labelSmall, color = AuraColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}


