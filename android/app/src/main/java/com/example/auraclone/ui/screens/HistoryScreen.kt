package com.example.auraclone.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.data.AuraAnalysis
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.components.AuraScaffold
import com.example.auraclone.ui.components.GlassCard
import com.example.auraclone.ui.components.NeonChip
import com.example.auraclone.ui.components.NeonSecondaryButton
import com.example.auraclone.ui.theme.AuraColors

@Composable
fun HistoryScreen(viewModel: AuraViewModel, onBack: () -> Unit) {
    val history by viewModel.history.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    AuraScaffold {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("АРХИВ СКАНОВ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp), color = AuraColors.ElectricCyan)
                    Text("История анализов 📜", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
                }
                NeonSecondaryButton(text = "Назад", onClick = onBack, height = 40.dp, cornerRadius = 14.dp)
            }
            Spacer(Modifier.height(16.dp))
            if (history.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GlassCard(cornerRadius = 24.dp, contentPadding = PaddingValues(24.dp)) {
                        Text("Анализов пока нет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text("Сделай первый AI-скан, и он появится здесь с полной расшифровкой.", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(history, key = { it.id }) { item -> HistoryAnalysisCard(item = item) }
                }
            }
        }
    }
}

@Composable
private fun HistoryAnalysisCard(item: AuraAnalysis) {
    var expanded by remember { mutableStateOf(false) }
    val accent = AuraColors.scoreAccentColor(item.score)

    GlassCard(
        cornerRadius = 20.dp,
        borderBrush = Brush.linearGradient(listOf(accent.copy(0.65f), AuraColors.GlassBorderSoft)),
        contentPadding = PaddingValues(16.dp),
        onClick = { expanded = !expanded }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${item.score}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = accent)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(item.level, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = AuraColors.TextPrimary)
                    Text(item.createdAt.take(16).replace("T", " "), style = MaterialTheme.typography.labelSmall, color = AuraColors.TextMuted)
                }
            }
            NeonChip(accentColor = accent) {
                Text(if (expanded) "Скрыть" else "Детали", style = MaterialTheme.typography.labelSmall, color = accent)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(item.verdict, style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary, maxLines = if (expanded) 10 else 2, overflow = TextOverflow.Ellipsis)
        AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text("Вайб: ${item.vibe}", style = MaterialTheme.typography.labelMedium, color = AuraColors.ElectricCyan)
                Spacer(Modifier.height(3.dp))
                Text("Энергия: ${item.energy}", style = MaterialTheme.typography.labelMedium, color = AuraColors.HyperMagenta)
                Spacer(Modifier.height(3.dp))
                Text("Уверенность: ${item.confidence}", style = MaterialTheme.typography.labelMedium, color = AuraColors.SolarGold)
            }
        }
    }
}
