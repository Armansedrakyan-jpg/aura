package com.example.auraclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.UiState
import com.example.auraclone.ui.components.AuraEnergyOrb
import com.example.auraclone.ui.components.AuraScaffold
import com.example.auraclone.ui.components.GlassCard
import com.example.auraclone.ui.components.NeonPrimaryButton
import com.example.auraclone.ui.theme.AuraColors

@Composable
fun ResultScreen(viewModel: AuraViewModel, onBackToHome: () -> Unit) {
    val analyzeState by viewModel.analyzeState.collectAsState()
    val result = (analyzeState as? UiState.Success)?.result
    val accent = result?.let { AuraColors.scoreAccentColor(it.score) } ?: AuraColors.ElectricCyan

    AuraScaffold(primaryOrbColor = accent, secondaryOrbColor = AuraColors.PlasmaViolet) {
        if (result != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("РЕЗУЛЬТАТ СКАНИРОВАНИЯ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp), color = accent)
                Spacer(Modifier.height(14.dp))
                AuraEnergyOrb(size = 224.dp, score = result.score, subtitle = result.level)
                Spacer(Modifier.height(22.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TraitGlassCard("Вайб", result.vibe, AuraColors.ElectricCyan, Modifier.weight(1f))
                    TraitGlassCard("Энергия", result.energy, AuraColors.HyperMagenta, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TraitGlassCard("Уверенность", result.confidence, AuraColors.SolarGold, Modifier.weight(1f))
                    TraitGlassCard("Личность", result.personality, AuraColors.PlasmaViolet, Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                VerdictCard(verdict = result.verdict, accent = accent)
                Spacer(Modifier.height(24.dp))
                NeonPrimaryButton(
                    text = "На главную",
                    onClick = { viewModel.resetAnalyzeState(); onBackToHome() },
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.horizontalGradient(listOf(AuraColors.ElectricCyan, AuraColors.PlasmaViolet, AuraColors.HyperMagenta)),
                    glowColor = accent
                )
                Spacer(Modifier.height(16.dp))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Нет данных результата", style = MaterialTheme.typography.titleLarge, color = AuraColors.TextSecondary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                NeonPrimaryButton(text = "На главную", onClick = onBackToHome, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun VerdictCard(verdict: String, accent: Color) {
    GlassCard(cornerRadius = 22.dp, glowColor = accent, contentPadding = PaddingValues(18.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.verticalGradient(listOf(accent, AuraColors.PlasmaViolet)))
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("ВЕРДИКТ AI", style = MaterialTheme.typography.labelSmall, color = accent)
                Spacer(Modifier.height(4.dp))
                Text(verdict, style = MaterialTheme.typography.bodyMedium, color = AuraColors.TextPrimary)
            }
        }
    }
}

@Composable
private fun TraitGlassCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        borderBrush = Brush.linearGradient(listOf(accent.copy(0.6f), Color.White.copy(0.12f))),
        contentPadding = PaddingValues(14.dp)
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = accent)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary)
    }
}


