package com.example.auraclone.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.CooldownUiState
import com.example.auraclone.ui.UiState
import com.example.auraclone.ui.components.*
import com.example.auraclone.ui.theme.AuraColors
import java.util.Locale

@Composable
fun CreateAnalysisScreen(
    viewModel: AuraViewModel,
    onResult: () -> Unit,
    onOpenShop: () -> Unit = {}
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var aboutMe by remember { mutableStateOf("") }

    val analyzeState by viewModel.analyzeState.collectAsState()
    val cooldownState by viewModel.cooldownState.collectAsState()
    val haptic = LocalHapticFeedback.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    LaunchedEffect(Unit) { viewModel.loadCooldown() }
    LaunchedEffect(analyzeState) { if (analyzeState is UiState.Success) onResult() }

    val isAnalyzing = analyzeState is UiState.Loading
    val canAnalyzeNow = cooldownState.canGenerateFree || cooldownState.credits > 0

    AuraScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("СКАНЕР АУРЫ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp), color = AuraColors.ElectricCyan)
            Spacer(Modifier.height(4.dp))
            Text("Новый AI-анализ ✨", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
            Spacer(Modifier.height(18.dp))
            CooldownStatusCard(cooldownState = cooldownState, onOpenShop = onOpenShop)
            Spacer(Modifier.height(20.dp))
            PhotoPickerBox(
                imageUri = imageUri, isAnalyzing = isAnalyzing,
                onPickPhoto = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); launcher.launch("image/*") }
            )
            Spacer(Modifier.height(20.dp))
            GlassCard(cornerRadius = 22.dp, contentPadding = PaddingValues(16.dp)) {
                AuraTextField(
                    value = aboutMe, onValueChange = { aboutMe = it }, label = "Расскажи о себе",
                    placeholder = "Вайб, настроение, стиль или цель...", singleLine = false, maxLines = 4,
                    modifier = Modifier.heightIn(min = 104.dp)
                )
            }
            AnimatedVisibility(visible = analyzeState is UiState.Error, enter = fadeIn(), exit = fadeOut()) {
                if (analyzeState is UiState.Error) {
                    Column {
                        Spacer(Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AuraColors.CrimsonAlert.copy(alpha = 0.14f))
                                .border(1.dp, AuraColors.CrimsonAlert.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text((analyzeState as UiState.Error).message, style = MaterialTheme.typography.bodySmall, color = AuraColors.CrimsonAlert)
                        }
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            val buttonLabel = when {
                cooldownState.canGenerateFree -> "Анализировать бесплатно 🔮"
                cooldownState.credits > 0 -> "Анализировать за кредиты ⚡"
                else -> "Ожидание ${formatDuration(cooldownState.remainingSeconds)}"
            }
            NeonPrimaryButton(
                text = buttonLabel,
                onClick = { imageUri?.let { viewModel.analyze(it, aboutMe) } },
                enabled = imageUri != null && canAnalyzeNow,
                isLoading = isAnalyzing,
                loadingText = "Считываем твою ауру...",
                modifier = Modifier.fillMaxWidth(),
                gradient = Brush.horizontalGradient(listOf(AuraColors.ElectricCyan, AuraColors.PlasmaViolet, AuraColors.HyperMagenta)),
                glowColor = AuraColors.PlasmaViolet
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun CooldownStatusCard(
    cooldownState: CooldownUiState,
    onOpenShop: () -> Unit
) {
    GlassCard(cornerRadius = 22.dp, contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("БАЛАНС КРЕДИТОВ", style = MaterialTheme.typography.labelSmall, color = AuraColors.TextMuted)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "⚡ ${cooldownState.credits} кредитов",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = AuraColors.SolarGold
                )
            }
            NeonChip(accentColor = AuraColors.ElectricCyan, onClick = onOpenShop) {
                Text("Пополнить", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AuraColors.ElectricCyan)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AuraColors.GlassBorderSoft)
        when {
            cooldownState.isLoading -> Text("Проверяем лимиты...", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
            cooldownState.canGenerateFree -> Text("Бесплатный скан ауры доступен сейчас", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AuraColors.EmeraldPulse)
            else -> {
                Text(
                    text = "До бесплатного скана: ${formatDuration(cooldownState.remainingSeconds)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    color = AuraColors.ElectricCyan
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (cooldownState.credits > 0) "Можно запустить прямо сейчас за кредиты" else "Лимит исчерпан — пополни кредиты в магазине",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (cooldownState.credits > 0) AuraColors.TextSecondary else AuraColors.CrimsonAlert
                )
            }
        }
    }
}

@Composable
private fun PhotoPickerBox(
    imageUri: Uri?,
    isAnalyzing: Boolean,
    onPickPhoto: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(220.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(AuraColors.ObsidianGlass)
            .border(width = 1.5.dp, brush = AuraColors.AuroraGradient, shape = RoundedCornerShape(28.dp))
            .clickable(enabled = !isAnalyzing, onClick = onPickPhoto),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AuraColors.VoidBlack.copy(alpha = 0.78f))
                    .border(1.dp, AuraColors.GlassBorderSoft, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text("Сменить фото", style = MaterialTheme.typography.labelSmall, color = AuraColors.TextPrimary)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                AuraEnergyOrb(size = 86.dp, subtitle = "PHOTO", isScanning = false)
                Spacer(Modifier.height(12.dp))
                Text("Выбрать фото 📸", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AuraColors.ElectricCyan)
                Spacer(Modifier.height(4.dp))
                Text("Нажми, чтобы загрузить снимок", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
