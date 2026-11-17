package com.example.auraclone.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.UiState
import com.example.auraclone.ui.components.*
import com.example.auraclone.ui.theme.AuraColors

@Composable
fun AuthScreen(viewModel: AuraViewModel, onLoggedIn: () -> Unit) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val isLoading = authState is UiState.Loading

    AuraScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuraEnergyOrb(size = 132.dp, isScanning = isLoading, subtitle = if (isLoading) "SYNC..." else "AURA")
            Spacer(Modifier.height(16.dp))
            Text("AURACLONE", style = MaterialTheme.typography.headlineLarge.copy(letterSpacing = 3.sp), fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isLoginMode) "Войди, чтобы измерить свой вайб" else "Создай профиль и вступай в дуэли",
                style = MaterialTheme.typography.bodyMedium, color = AuraColors.TextSecondary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(26.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AuraColors.SubtleGlass, RoundedCornerShape(18.dp))
                    .border(1.dp, AuraColors.GlassBorderSoft, RoundedCornerShape(18.dp))
                    .padding(4.dp)
            ) {
                AuthModeTab("Вход", isLoginMode, Modifier.weight(1f)) {
                    if (!isLoginMode) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); isLoginMode = true }
                }
                AuthModeTab("Регистрация", !isLoginMode, Modifier.weight(1f)) {
                    if (isLoginMode) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); isLoginMode = false }
                }
            }
            Spacer(Modifier.height(18.dp))
            AuthFormCard(
                isLoginMode = isLoginMode,
                username = username, onUsernameChange = { username = it },
                email = email, onEmailChange = { email = it },
                password = password, onPasswordChange = { password = it },
                authState = authState,
                onSubmit = {
                    if (isLoginMode) viewModel.login(username.trim(), password, onLoggedIn)
                    else viewModel.register(username.trim(), email.trim(), password, onLoggedIn)
                }
            )
        }
    }
}

@Composable
private fun AuthFormCard(
    isLoginMode: Boolean,
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    authState: UiState,
    onSubmit: () -> Unit
) {
    val isLoading = authState is UiState.Loading
    GlassCard(
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(20.dp),
        glowColor = AuraColors.PlasmaViolet
    ) {
        AuraTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = if (isLoginMode) "Username или Email" else "Username"
        )
        AnimatedVisibility(visible = !isLoginMode, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column {
                Spacer(Modifier.height(12.dp))
                AuraTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        AuraTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Пароль",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        AnimatedVisibility(visible = authState is UiState.Error, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            if (authState is UiState.Error) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraColors.CrimsonAlert.copy(alpha = 0.14f))
                            .border(1.dp, AuraColors.CrimsonAlert.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(authState.message, style = MaterialTheme.typography.bodySmall, color = AuraColors.CrimsonAlert)
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        NeonPrimaryButton(
            text = if (isLoginMode) "Войти в систему" else "Создать аккаунт",
            onClick = onSubmit,
            isLoading = isLoading,
            loadingText = if (isLoginMode) "Подключаемся..." else "Создаём ауру...",
            modifier = Modifier.fillMaxWidth(),
            gradient = Brush.horizontalGradient(listOf(AuraColors.ElectricCyan, AuraColors.PlasmaViolet, AuraColors.HyperMagenta)),
            glowColor = AuraColors.ElectricCyan
        )
    }
}

@Composable
private fun AuthModeTab(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) AuraColors.PlasmaViolet.copy(alpha = 0.42f) else Color.Transparent,
        label = "auth_tab_bg"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor, RoundedCornerShape(14.dp))
            .then(
                if (selected) Modifier.border(1.dp, AuraColors.CyanVioletGradient, RoundedCornerShape(14.dp)) else Modifier
            )
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) AuraColors.TextPrimary else AuraColors.TextMuted
        )
    }
}


