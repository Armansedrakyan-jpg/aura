package com.example.auraclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.data.LeaderboardEntry
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.components.*
import com.example.auraclone.ui.theme.AuraColors

@Composable
fun LeaderboardScreen(
    viewModel: AuraViewModel,
    onBack: () -> Unit,
    onUserClick: (Long) -> Unit = {},
    onSelectTab: ((AuraTab) -> Unit)? = null
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) { viewModel.loadLeaderboard() }

    val topThree = leaderboard.take(3)
    val rest = if (leaderboard.size > 3) leaderboard.drop(3) else emptyList()

    AuraScaffold(
        bottomBar = onSelectTab?.let { selectTab ->
            { AuraBottomBar(currentTab = AuraTab.LEADERBOARD, onSelectTab = selectTab) }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = if (onSelectTab != null) 96.dp else 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("МИРОВОЙ РЕЙТИНГ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp), color = AuraColors.ElectricCyan)
                    Text("Лидерборд 🏆", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeonChip(accentColor = AuraColors.PlasmaViolet, onClick = { viewModel.loadLeaderboard() }) {
                        Text("↻ Обновить", style = MaterialTheme.typography.labelMedium, color = AuraColors.ElectricCyan)
                    }
                    if (onSelectTab == null) {
                        NeonSecondaryButton(text = "Назад", onClick = onBack, height = 36.dp, cornerRadius = 12.dp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (leaderboard.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GlassCard(cornerRadius = 24.dp, contentPadding = PaddingValues(24.dp)) {
                        Text("Рейтинг пока пуст", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Стань первым, кто пройдёт анализ ауры!", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    if (topThree.isNotEmpty()) {
                        item {
                            LeaderboardPodium(topThree = topThree, onUserClick = { uid ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onUserClick(uid)
                            })
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    itemsIndexed(rest) { offsetIndex, item ->
                        LeaderboardRowCard(rank = offsetIndex + 4, item = item, onClick = {
                            if (item.userId > 0L) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onUserClick(item.userId) }
                        })
                    }
                }
            }
        }
    }
}
@Composable
private fun LeaderboardPodium(topThree: List<LeaderboardEntry>, onUserClick: (Long) -> Unit) {
    val first = topThree.getOrNull(0)
    val second = topThree.getOrNull(1)
    val third = topThree.getOrNull(2)

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (second != null) PodiumSlot("🥈 #2", second, 74.dp, AuraColors.ElectricCyan, Modifier.weight(1f)) { if (second.userId > 0L) onUserClick(second.userId) }
        else Spacer(Modifier.weight(1f))
        if (first != null) PodiumSlot("👑 #1", first, 96.dp, AuraColors.SolarGold, Modifier.weight(1.1f)) { if (first.userId > 0L) onUserClick(first.userId) }
        if (third != null) PodiumSlot("🥉 #3", third, 58.dp, AuraColors.HyperMagenta, Modifier.weight(1f)) { if (third.userId > 0L) onUserClick(third.userId) }
        else Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun PodiumSlot(rankLabel: String, entry: LeaderboardEntry, pedestalHeight: Dp, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clip(RoundedCornerShape(18.dp)).clickable(enabled = entry.userId > 0L, onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        AuraAvatar(username = entry.username, avatarFrame = entry.avatarFrame, avatarEffect = entry.avatarEffect, size = if (rankLabel.contains("#1")) 62.dp else 50.dp)
        Spacer(Modifier.height(6.dp))
        Text(entry.username, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth().height(pedestalHeight), cornerRadius = 16.dp, glowColor = accent,
            borderBrush = Brush.verticalGradient(listOf(accent.copy(0.85f), AuraColors.GlassBorderSoft)), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(rankLabel, style = MaterialTheme.typography.labelSmall, color = accent, textAlign = TextAlign.Center)
                Spacer(Modifier.height(2.dp))
                Text("${entry.score}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
                Text(entry.level, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AuraColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun LeaderboardRowCard(rank: Int, item: LeaderboardEntry, onClick: () -> Unit) {
    GlassCard(cornerRadius = 18.dp, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp), onClick = if (item.userId > 0L) onClick else null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text("#$rank", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = AuraColors.TextSecondary, modifier = Modifier.widthIn(min = 30.dp))
                Spacer(Modifier.width(8.dp))
                AuraAvatar(username = item.username, avatarFrame = item.avatarFrame, avatarEffect = item.avatarEffect, size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.username, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.level, style = MaterialTheme.typography.labelSmall, color = AuraColors.PlasmaViolet, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (item.verdict.isNotBlank()) Text(item.verdict, style = MaterialTheme.typography.bodySmall, color = AuraColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(AuraColors.ElectricCyan.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                Text("${item.score} pts", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = AuraColors.ElectricCyan)
            }
        }
    }
}
