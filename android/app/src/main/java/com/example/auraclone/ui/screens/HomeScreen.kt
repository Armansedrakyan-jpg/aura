package com.example.auraclone.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.components.*
import com.example.auraclone.ui.theme.AuraColors

@Composable
fun HomeScreen(
    viewModel: AuraViewModel,
    onCreateAnalysis: () -> Unit,
    onHistory: () -> Unit,
    onNavigateToBattle: (String) -> Unit,
    onLeaderboard: () -> Unit,
    onShop: () -> Unit,
    onProfile: () -> Unit
) {
    var showJoinDialog by remember { mutableStateOf(false) }
    var inputBattleId by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val myProfile by viewModel.myProfile.collectAsState()
    val myStats by viewModel.myPublicStats.collectAsState()
    val history by viewModel.history.collectAsState()
    val cooldownState by viewModel.cooldownState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyProfile()
        viewModel.loadHistory()
        viewModel.loadCooldown()
    }

    val username = myProfile?.username ?: viewModel.getCachedUsername()
    val latestAura = history.firstOrNull()
    val heroScore = latestAura?.score ?: myStats?.bestScore?.takeIf { it > 0 }
    val heroLevel = latestAura?.level ?: myStats?.bestLevel ?: "ГОТОВ К СКАНУ"

    AuraScaffold(
        bottomBar = {
            AuraBottomBar(
                currentTab = AuraTab.HOME,
                onSelectTab = { tab ->
                    when (tab) {
                        AuraTab.HOME -> Unit
                        AuraTab.LEADERBOARD -> onLeaderboard()
                        AuraTab.SHOP -> onShop()
                        AuraTab.PROFILE -> onProfile()
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 108.dp)
        ) {
            HomeTopBar(
                username = username, avatarFrame = myStats?.avatarFrame, avatarEffect = myStats?.avatarEffect,
                credits = cooldownState.credits,
                onProfile = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onProfile() },
                onShop = onShop
            )
            Spacer(Modifier.height(20.dp))
            HomeHeroCard(
                heroScore = heroScore, heroLevel = heroLevel, latestVerdict = latestAura?.verdict,
                canGenerateFree = cooldownState.canGenerateFree, credits = cooldownState.credits,
                onCreateAnalysis = onCreateAnalysis
            )
            Spacer(Modifier.height(18.dp))
            HomeArenaSection(
                historyCount = history.size,
                onCreateBattle = { viewModel.startBattle { battleId -> onNavigateToBattle(battleId) } },
                onShowJoinDialog = { showJoinDialog = true },
                onHistory = onHistory
            )
        }
    }
    JoinBattleDialog(
        visible = showJoinDialog, battleId = inputBattleId, onBattleIdChange = { inputBattleId = it },
        onDismiss = { showJoinDialog = false },
        onJoin = {
            val id = inputBattleId.trim()
            if (id.isNotEmpty()) viewModel.joinBattle(id) { showJoinDialog = false; onNavigateToBattle(id) }
        }
    )
}

@Composable
private fun HomeTopBar(
    username: String,
    avatarFrame: String?,
    avatarEffect: String?,
    credits: Int,
    onProfile: () -> Unit,
    onShop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onProfile)
                .padding(vertical = 4.dp)
        ) {
            AuraAvatar(username = username, avatarFrame = avatarFrame, avatarEffect = avatarEffect, size = 46.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("AURA PULSE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp), color = AuraColors.ElectricCyan)
                Text(username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = AuraColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.width(8.dp))
        NeonChip(accentColor = AuraColors.SolarGold, onClick = onShop) {
            Text("⚡ $credits", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = AuraColors.SolarGold)
        }
    }
}

@Composable
private fun HomeHeroCard(
    heroScore: Int?,
    heroLevel: String,
    latestVerdict: String?,
    canGenerateFree: Boolean,
    credits: Int,
    onCreateAnalysis: () -> Unit
) {
    GlassCard(
        cornerRadius = 28.dp,
        glowColor = heroScore?.let { AuraColors.scoreAccentColor(it) } ?: AuraColors.PlasmaViolet,
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AuraEnergyOrb(
                size = 186.dp,
                score = heroScore,
                subtitle = if (heroScore != null) heroLevel else "SCAN AURA",
                isScanning = heroScore == null
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = if (heroScore != null) "Последний скан ауры" else "Раскрой свой энергетический профиль",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AuraColors.TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            if (!latestVerdict.isNullOrBlank()) {
                Text(latestVerdict, style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            } else {
                Text(
                    text = if (canGenerateFree) "Бесплатный AI-анализ доступен сейчас" else "Доступно кредитов: $credits ⚡",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canGenerateFree) AuraColors.EmeraldPulse else AuraColors.TextSecondary
                )
            }
            Spacer(Modifier.height(18.dp))
            NeonPrimaryButton(
                text = "Новый анализ ауры ✨",
                onClick = onCreateAnalysis,
                modifier = Modifier.fillMaxWidth(),
                gradient = Brush.horizontalGradient(listOf(AuraColors.ElectricCyan, AuraColors.PlasmaViolet, AuraColors.HyperMagenta)),
                glowColor = AuraColors.ElectricCyan
            )
        }
    }
}

@Composable
private fun HomeArenaSection(
    historyCount: Int,
    onCreateBattle: () -> Unit,
    onShowJoinDialog: () -> Unit,
    onHistory: () -> Unit
) {
    Text("АРЕНА ДУЭЛЕЙ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp), color = AuraColors.TextMuted, modifier = Modifier.padding(start = 4.dp, bottom = 10.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(
            modifier = Modifier.weight(1f), cornerRadius = 22.dp,
            borderBrush = Brush.linearGradient(listOf(AuraColors.PlasmaViolet.copy(0.75f), AuraColors.HyperMagenta.copy(0.45f))),
            glowColor = AuraColors.PlasmaViolet, contentPadding = PaddingValues(16.dp), onClick = onCreateBattle
        ) {
            Text("⚔️", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            Text("Создать\nдуэль", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = AuraColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Получить код комнаты", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
        }
        GlassCard(
            modifier = Modifier.weight(1f), cornerRadius = 22.dp,
            borderBrush = Brush.linearGradient(listOf(AuraColors.ElectricCyan.copy(0.75f), AuraColors.PlasmaViolet.copy(0.45f))),
            glowColor = AuraColors.ElectricCyan, contentPadding = PaddingValues(16.dp), onClick = onShowJoinDialog
        ) {
            Text("🔑", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            Text("Войти\nпо ID", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = AuraColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Бросить вызов другу", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
        }
    }
    Spacer(Modifier.height(14.dp))
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 15.dp), onClick = onHistory) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("История анализов 📜", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(if (historyCount == 0) "Посмотреть прошлые результаты" else "Сохранено сканов: $historyCount", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
            }
            Text("→", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = AuraColors.ElectricCyan)
        }
    }
}

@Composable
private fun JoinBattleDialog(
    visible: Boolean,
    battleId: String,
    onBattleIdChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF130926),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.border(1.dp, AuraColors.GlassStrokeGradient, RoundedCornerShape(26.dp)),
            title = { Text("Вход в дуэль ⚔️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = AuraColors.ElectricCyan) },
            text = {
                Column {
                    Text("Введи ID дуэли соперника, чтобы вступить в битву аур:", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
                    Spacer(Modifier.height(14.dp))
                    AuraTextField(value = battleId, onValueChange = onBattleIdChange, label = "ID дуэли")
                }
            },
            confirmButton = {
                NeonPrimaryButton(text = "В бой!", height = 44.dp, cornerRadius = 14.dp, enabled = battleId.isNotBlank(), onClick = onJoin)
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Отмена", color = AuraColors.TextSecondary) }
            }
        )
    }
}

