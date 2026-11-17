package com.example.auraclone.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun ProfileScreen(
    viewModel: AuraViewModel,
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    onSelectTab: ((AuraTab) -> Unit)? = null
) {
    val myProfile by viewModel.myProfile.collectAsState()
    val myPublicStats by viewModel.myPublicStats.collectAsState()
    val profileLoadState by viewModel.profileLoadState.collectAsState()
    val deleteAccountState by viewModel.deleteAccountState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.resetDeleteAccountState(); viewModel.loadMyProfile() }
    val displayName = myProfile?.username ?: viewModel.getCachedUsername()

    AuraScaffold(bottomBar = onSelectTab?.let { selectTab -> { AuraBottomBar(AuraTab.PROFILE, selectTab) } }) {
        ProfileBody(
            displayName = displayName,
            email = myProfile?.email ?: "Загрузка...",
            userId = myProfile?.userId,
            myPublicStats = myPublicStats,
            profileLoadState = profileLoadState,
            showBackButton = onSelectTab == null,
            bottomPadding = if (onSelectTab != null) 108.dp else 24.dp,
            onBack = onBack,
            onLogout = { viewModel.logout { onSignedOut() } },
            onRequestDelete = { viewModel.resetDeleteAccountState(); showDeleteDialog = true }
        )
    }
    DeleteAccountDialog(showDeleteDialog, deleteAccountState, { showDeleteDialog = false }) {
        viewModel.deleteAccount { showDeleteDialog = false; onSignedOut() }
    }
}


@Composable
private fun ProfileBody(
    displayName: String,
    email: String,
    userId: Long?,
    myPublicStats: PublicUserProfileResponse?,
    profileLoadState: ActionState,
    showBackButton: Boolean,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onRequestDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("АККАУНТ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp), color = AuraColors.ElectricCyan)
                Text("Профиль ⚙️", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
            }
            if (showBackButton) NeonSecondaryButton("Назад", onBack, height = 36.dp, cornerRadius = 12.dp)
        }
        Spacer(Modifier.height(18.dp))
        GlassCard(cornerRadius = 26.dp, glowColor = AuraColors.PlasmaViolet, contentPadding = PaddingValues(20.dp)) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AuraAvatar(displayName, myPublicStats?.avatarFrame, myPublicStats?.avatarEffect, size = 86.dp)
                Spacer(Modifier.height(12.dp))
                Text(displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(email, style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
                userId?.let {
                    Spacer(Modifier.height(8.dp))
                    NeonChip(accentColor = AuraColors.ElectricCyan) { Text("ID: $it", style = MaterialTheme.typography.labelSmall, color = AuraColors.ElectricCyan) }
                }
                if (profileLoadState is ActionState.Loading) {
                    Spacer(Modifier.height(10.dp))
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = AuraColors.ElectricCyan, strokeWidth = 2.dp)
                } else if (profileLoadState is ActionState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(profileLoadState.message, style = MaterialTheme.typography.bodySmall, color = AuraColors.CrimsonAlert)
                }
            }
        }
        myPublicStats?.let { stats ->
            Spacer(Modifier.height(16.dp))
            val winRate = if (stats.totalBattles > 0) "${(stats.winsCount * 100) / stats.totalBattles}%" else "0%"
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileStatTile("Лучший балл", "${stats.bestScore} pts", AuraColors.ElectricCyan, Modifier.weight(1f))
                ProfileStatTile("Средний балл", String.format(Locale.US, "%.1f", stats.averageScore), AuraColors.PlasmaViolet, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileStatTile("Анализов", "${stats.totalAnalyses}", AuraColors.SolarGold, Modifier.weight(1f))
                ProfileStatTile("Побед ($winRate)", "${stats.winsCount} / ${stats.totalBattles}", AuraColors.EmeraldPulse, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(18.dp))
        NeonSecondaryButton("Выйти из аккаунта", onLogout, modifier = Modifier.fillMaxWidth(), accentColor = AuraColors.ElectricCyan)
        Spacer(Modifier.height(14.dp))
        ProfileDangerZone(onRequestDelete)
    }
}

@Composable
private fun ProfileStatTile(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
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

@Composable
private fun ProfileDangerZone(onRequestDelete: () -> Unit) {
    GlassCard(cornerRadius = 22.dp, borderBrush = AuraColors.DangerStrokeGradient, contentPadding = PaddingValues(16.dp)) {
        Text("ОПАСНАЯ ЗОНА", style = MaterialTheme.typography.labelSmall, color = AuraColors.CrimsonAlert)
        Spacer(Modifier.height(4.dp))
        Text("Удаление аккаунта навсегда сотрёт профиль, историю анализов, статистику и кредиты.", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
        Spacer(Modifier.height(12.dp))
        NeonPrimaryButton(
            text = "Удалить аккаунт",
            onClick = onRequestDelete,
            height = 48.dp,
            cornerRadius = 14.dp,
            modifier = Modifier.fillMaxWidth(),
            gradient = Brush.horizontalGradient(listOf(AuraColors.CrimsonAlert, AuraColors.HyperMagenta)),
            glowColor = AuraColors.CrimsonAlert
        )
    }
}

@Composable
private fun DeleteAccountDialog(
    visible: Boolean,
    deleteAccountState: ActionState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDeleting = deleteAccountState is ActionState.Loading
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) onDismiss() },
            containerColor = Color(0xFF130926),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.border(1.dp, AuraColors.DangerStrokeGradient, RoundedCornerShape(26.dp)),
            title = { Text("Удалить аккаунт навсегда?", fontWeight = FontWeight.ExtraBold, color = AuraColors.CrimsonAlert) },
            text = {
                Column {
                    Text(
                        text = "Это действие необратимо. Ваш профиль, история анализов, рейтинг и баланс кредитов будут полностью удалены с сервера.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraColors.TextSecondary
                    )
                    if (deleteAccountState is ActionState.Error) {
                        Spacer(Modifier.height(12.dp))
                        Text(deleteAccountState.message, style = MaterialTheme.typography.bodySmall, color = AuraColors.CrimsonAlert)
                    }
                }
            },
            confirmButton = {
                NeonPrimaryButton(
                    text = "Да, удалить",
                    onClick = onConfirm,
                    isLoading = isDeleting,
                    height = 44.dp,
                    cornerRadius = 14.dp,
                    gradient = Brush.horizontalGradient(listOf(AuraColors.CrimsonAlert, AuraColors.HyperMagenta)),
                    glowColor = AuraColors.CrimsonAlert
                )
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !isDeleting) {
                    Text("Отмена", color = AuraColors.TextSecondary)
                }
            }
        )
    }
}

