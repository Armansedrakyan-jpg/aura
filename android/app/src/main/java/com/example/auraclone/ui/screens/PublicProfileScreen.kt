package com.example.auraclone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.data.PublicUserProfileResponse
import com.example.auraclone.ui.ActionState
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.components.*
import com.example.auraclone.ui.theme.AuraColors
import java.util.Locale

@Composable
fun PublicProfileScreen(
    viewModel: AuraViewModel,
    userId: Long,
    onBack: () -> Unit
) {
    val profile by viewModel.publicProfile.collectAsState()
    val loadState by viewModel.publicProfileState.collectAsState()

    LaunchedEffect(userId) { viewModel.loadPublicProfile(userId) }

    AuraScaffold {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("КАРТОЧКА ИГРОКА", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp), color = AuraColors.ElectricCyan)
                    Text("Профиль игрока", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
                }
                NeonSecondaryButton("Назад", onBack, height = 38.dp, cornerRadius = 12.dp)
            }
            Spacer(Modifier.height(18.dp))
            when (loadState) {
                is ActionState.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AuraColors.ElectricCyan) }
                is ActionState.Error -> Text((loadState as ActionState.Error).message, color = AuraColors.CrimsonAlert, style = MaterialTheme.typography.bodyMedium)
                else -> profile?.let { PublicProfileBody(it) }
            }
        }
    }
}

@Composable
private fun PublicProfileBody(user: PublicUserProfileResponse) {
    GlassCard(cornerRadius = 26.dp, glowColor = AuraColors.PlasmaViolet, contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AuraAvatar(user.username, user.avatarFrame, user.avatarEffect, size = 86.dp)
            Spacer(Modifier.height(12.dp))
            Text(user.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("В системе с ${user.createdAt.take(10)}", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
            user.bestLevel?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                NeonChip(accentColor = AuraColors.SolarGold) { Text(it, style = MaterialTheme.typography.labelSmall, color = AuraColors.SolarGold) }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatBox("Лучший балл", "${user.bestScore} pts", AuraColors.ElectricCyan, Modifier.weight(1f))
        StatBox("Средний балл", String.format(Locale.US, "%.1f", user.averageScore), AuraColors.PlasmaViolet, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatBox("Анализов", "${user.totalAnalyses}", AuraColors.SolarGold, Modifier.weight(1f))
        StatBox("Побед в дуэлях", "${user.winsCount} / ${user.totalBattles}", AuraColors.EmeraldPulse, Modifier.weight(1f))
    }
    Spacer(Modifier.height(20.dp))
    Text("ДОСТИЖЕНИЯ (${user.achievements.size})", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp), color = AuraColors.TextMuted, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(10.dp))
    if (user.achievements.isEmpty()) {
        GlassCard(cornerRadius = 18.dp, contentPadding = PaddingValues(16.dp)) {
            Text("Пока нет открытых достижений", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
        }
    } else {
        user.achievements.forEach { ach ->
            GlassCard(
                modifier = Modifier.padding(bottom = 10.dp), cornerRadius = 18.dp,
                borderBrush = Brush.linearGradient(listOf(AuraColors.SolarGold.copy(0.65f), AuraColors.GlassBorderSoft)),
                contentPadding = PaddingValues(14.dp)
            ) {
                Text("🏅 ${ach.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(ach.description, style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text(ach.unlockedAt.take(10), style = MaterialTheme.typography.labelSmall, color = AuraColors.ElectricCyan)
            }
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        borderBrush = Brush.linearGradient(listOf(accent.copy(0.6f), AuraColors.GlassBorderSoft)),
        contentPadding = PaddingValues(14.dp)
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = AuraColors.TextMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = accent)
    }
}
