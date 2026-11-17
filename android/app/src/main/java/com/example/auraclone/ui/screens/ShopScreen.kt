package com.example.auraclone.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auraclone.billing.BillingManager
import com.example.auraclone.billing.BillingProductInfo
import com.example.auraclone.data.ShopItem
import com.example.auraclone.ui.ActionState
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.components.*
import com.example.auraclone.ui.theme.AuraColors

@Composable
fun ShopScreen(
    viewModel: AuraViewModel,
    onBack: () -> Unit,
    onSelectTab: ((AuraTab) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current
    val catalog by viewModel.shopCatalog.collectAsState()
    val shopState by viewModel.shopState.collectAsState()
    val shopMessage by viewModel.shopActionMessage.collectAsState()
    var selectedTab by remember { mutableStateOf("FRAME") }
    var previewFrame by remember { mutableStateOf<String?>(null) }
    var previewEffect by remember { mutableStateOf<String?>(null) }

    val billingManager = remember {
        BillingManager(context, { pid, token, cb -> viewModel.verifyGooglePlayPurchase(pid, token, cb) }, { viewModel.setShopMessage(it) })
    }
    val billingProducts by billingManager.products.collectAsState()
    DisposableEffect(Unit) { onDispose { billingManager.endConnection() } }
    LaunchedEffect(Unit) { viewModel.setShopMessage(null); viewModel.loadShopItems() }
    LaunchedEffect(catalog) {
        val ids = catalog?.items?.filter { it.type == "CREDIT_PACK" && !it.googlePlayProductId.isNullOrBlank() }?.mapNotNull { it.googlePlayProductId }.orEmpty()
        if (ids.isNotEmpty()) billingManager.startConnection(ids)
    }

    val displayedFrame = previewFrame ?: catalog?.avatarFrame ?: "DEFAULT"
    val displayedEffect = previewEffect ?: catalog?.avatarEffect
    val filteredItems = catalog?.items?.filter { it.type == selectedTab }.orEmpty()

    AuraScaffold(
        primaryOrbColor = AuraColors.SolarGold, secondaryOrbColor = AuraColors.PlasmaViolet,
        bottomBar = onSelectTab?.let { selectTab -> { AuraBottomBar(AuraTab.SHOP, selectTab) } }
    ) {
        ShopScreenBody(
            username = viewModel.getCachedUsername(), credits = catalog?.credits ?: 0,
            displayedFrame = displayedFrame, displayedEffect = displayedEffect,
            isPreviewMode = previewFrame != null || previewEffect != null,
            onResetPreview = { previewFrame = null; previewEffect = null },
            shopMessage = shopMessage, shopState = shopState, selectedTab = selectedTab,
            onSelectCategory = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); selectedTab = it },
            filteredItems = filteredItems, billingProducts = billingProducts,
            showBackButton = onSelectTab == null, onBack = onBack,
            onPreviewItem = { if (it.type == "FRAME") previewFrame = it.code; if (it.type == "EFFECT") previewEffect = it.code },
            onBuyItem = { viewModel.buyShopItem(it.id) },
            onEquipItem = { viewModel.equipShopItem(it.id) },
            onUnequipItem = { viewModel.unequipShopItem(it.type) },
            onBuyCredits = { item ->
                val prodId = item.googlePlayProductId
                if (activity != null && !prodId.isNullOrBlank()) billingManager.launchPurchase(activity, prodId)
                else viewModel.setShopMessage("Товар Google Play не настроен")
            },
            bottomPadding = if (onSelectTab != null) 96.dp else 16.dp
        )
    }
}

@Composable
private fun ShopScreenBody(
    username: String, credits: Int, displayedFrame: String, displayedEffect: String?,
    isPreviewMode: Boolean, onResetPreview: () -> Unit, shopMessage: String?, shopState: ActionState,
    selectedTab: String, onSelectCategory: (String) -> Unit, filteredItems: List<ShopItem>,
    billingProducts: Map<String, BillingProductInfo>, showBackButton: Boolean, onBack: () -> Unit,
    onPreviewItem: (ShopItem) -> Unit, onBuyItem: (ShopItem) -> Unit, onEquipItem: (ShopItem) -> Unit,
    onUnequipItem: (ShopItem) -> Unit, onBuyCredits: (ShopItem) -> Unit, bottomPadding: androidx.compose.ui.unit.Dp
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 14.dp, bottom = bottomPadding)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("КАСТОМИЗАЦИЯ", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp), color = AuraColors.SolarGold)
                Text("Магазин Ауры ✨", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = AuraColors.TextPrimary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                NeonChip(accentColor = AuraColors.SolarGold) { Text("⚡ $credits", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = AuraColors.SolarGold) }
                if (showBackButton) NeonSecondaryButton("Назад", onBack, height = 36.dp, cornerRadius = 12.dp)
            }
        }
        Spacer(Modifier.height(14.dp))
        ShopPreviewBanner(username, displayedFrame, displayedEffect, isPreviewMode, onResetPreview)
        AnimatedVisibility(!shopMessage.isNullOrBlank(), enter = fadeIn(), exit = fadeOut()) {
            shopMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AuraColors.ElectricCyan, modifier = Modifier.padding(top = 8.dp)) }
        }
        AnimatedVisibility(shopState is ActionState.Error, enter = fadeIn(), exit = fadeOut()) {
            if (shopState is ActionState.Error) Text(shopState.message, style = MaterialTheme.typography.bodySmall, color = AuraColors.CrimsonAlert, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AuraColors.SubtleGlass, RoundedCornerShape(16.dp))
                .border(1.dp, AuraColors.GlassBorderSoft, RoundedCornerShape(16.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShopTabButton("Рамки", selectedTab == "FRAME", Modifier.weight(1f)) { onSelectCategory("FRAME") }
            ShopTabButton("Эффекты", selectedTab == "EFFECT", Modifier.weight(1f)) { onSelectCategory("EFFECT") }
            ShopTabButton("Кредиты", selectedTab == "CREDIT_PACK", Modifier.weight(1f)) { onSelectCategory("CREDIT_PACK") }
        }
        Spacer(Modifier.height(12.dp))
        if (shopState is ActionState.Loading && filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AuraColors.ElectricCyan) }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 14.dp)) {
                items(filteredItems, key = { it.id }) { item ->
                    ShopItemCard(
                        item = item, userCredits = credits, billingProduct = item.googlePlayProductId?.let { billingProducts[it] },
                        isBusy = shopState is ActionState.Loading, onPreview = { onPreviewItem(item) }, onBuy = { onBuyItem(item) },
                        onEquip = { onEquipItem(item) }, onUnequip = { onUnequipItem(item) }, onBuyCredits = { onBuyCredits(item) }
                    )
                }
            }
        }
    }
}



@Composable
private fun ShopPreviewBanner(username: String, displayedFrame: String, displayedEffect: String?, isPreviewMode: Boolean, onResetPreview: () -> Unit) {
    GlassCard(cornerRadius = 22.dp, glowColor = if (isPreviewMode) AuraColors.ElectricCyan else AuraColors.PlasmaViolet, contentPadding = PaddingValues(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                AuraAvatar(username = username, avatarFrame = displayedFrame, avatarEffect = displayedEffect, size = 62.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(if (isPreviewMode) "ЖИВАЯ ПРИМЕРКА" else "ТЕКУЩИЙ СТИЛЬ", style = MaterialTheme.typography.labelSmall, color = if (isPreviewMode) AuraColors.ElectricCyan else AuraColors.PlasmaViolet)
                    Spacer(Modifier.height(2.dp))
                    Text("Рамка: $displayedFrame", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary)
                    Text("Эффект: ${displayedEffect ?: "Нет"}", style = MaterialTheme.typography.labelSmall, color = AuraColors.TextSecondary)
                }
            }
            if (isPreviewMode) NeonChip(accentColor = AuraColors.HyperMagenta, onClick = onResetPreview) { Text("Сброс", style = MaterialTheme.typography.labelSmall, color = AuraColors.HyperMagenta) }
        }
    }
}

@Composable
private fun ShopTabButton(title: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgColor by animateColorAsState(if (selected) AuraColors.PlasmaViolet.copy(alpha = 0.42f) else Color.Transparent, label = "shop_tab")
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(bgColor, RoundedCornerShape(12.dp))
            .then(if (selected) Modifier.border(1.dp, AuraColors.CyanVioletGradient, RoundedCornerShape(12.dp)) else Modifier)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick).padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium, color = if (selected) AuraColors.TextPrimary else AuraColors.TextMuted, maxLines = 1)
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem, userCredits: Int, billingProduct: BillingProductInfo?, isBusy: Boolean,
    onPreview: () -> Unit, onBuy: () -> Unit, onEquip: () -> Unit, onUnequip: () -> Unit, onBuyCredits: () -> Unit
) {
    val rarityUpper = item.rarity.uppercase()
    val rarityColor = when (rarityUpper) {
        "LEGENDARY" -> AuraColors.SolarGold
        "EPIC" -> AuraColors.PlasmaViolet
        "RARE" -> AuraColors.ElectricCyan
        else -> AuraColors.TextSecondary
    }
    val hasShimmer = rarityUpper == "LEGENDARY" || rarityUpper == "EPIC"
    val shimmerOffset by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = -0.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart), label = "shimmer_x"
    )

    GlassCard(
        modifier = Modifier.then(
            if (hasShimmer) {
                Modifier.drawWithContent {
                    drawContent()
                    val startX = size.width * shimmerOffset
                    drawRect(Brush.linearGradient(listOf(Color.Transparent, rarityColor.copy(0.12f), Color.White.copy(0.08f), Color.Transparent), Offset(startX, 0f), Offset(startX + size.width * 0.45f, size.height)))
                }
            } else Modifier
        ),
        cornerRadius = 20.dp, glowColor = if (item.equipped || hasShimmer) rarityColor else Color.Transparent,
        borderBrush = Brush.linearGradient(listOf(rarityColor.copy(alpha = 0.7f), AuraColors.GlassBorderSoft)),
        contentPadding = PaddingValues(16.dp), onClick = if (item.type != "CREDIT_PACK") onPreview else null
    ) {
        ShopItemCardContent(item, userCredits, billingProduct, isBusy, rarityColor, onPreview, onBuy, onEquip, onUnequip, onBuyCredits)
    }
}

@Composable
private fun ShopItemCardContent(
    item: ShopItem, userCredits: Int, billingProduct: BillingProductInfo?, isBusy: Boolean, rarityColor: Color,
    onPreview: () -> Unit, onBuy: () -> Unit, onEquip: () -> Unit, onUnequip: () -> Unit, onBuyCredits: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (item.type != "CREDIT_PACK") {
                AuraAvatar(item.name, if (item.type == "FRAME") item.code else "DEFAULT", if (item.type == "EFFECT") item.code else null, size = 50.dp)
                Spacer(Modifier.width(12.dp))
            }
            Column {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AuraColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.rarity, style = MaterialTheme.typography.labelSmall, color = rarityColor)
            }
        }
        if (item.equipped) NeonChip(accentColor = AuraColors.EmeraldPulse) { Text("Надето ✓", style = MaterialTheme.typography.labelSmall, color = AuraColors.EmeraldPulse) }
    }
    Spacer(Modifier.height(8.dp))
    Text(item.description, style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
    Spacer(Modifier.height(14.dp))
    if (item.type == "CREDIT_PACK") {
        val priceText = billingProduct?.formattedPrice?.takeIf { it.isNotBlank() } ?: "Google Play"
        NeonPrimaryButton(
            text = "+${item.creditsAmount ?: 0} ⚡ • Купить ($priceText)", onClick = onBuyCredits, enabled = !isBusy,
            height = 46.dp, cornerRadius = 14.dp, modifier = Modifier.fillMaxWidth(),
            gradient = Brush.horizontalGradient(listOf(AuraColors.SolarGold, AuraColors.HyperMagenta)), glowColor = AuraColors.SolarGold
        )
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonSecondaryButton("Примерить", onPreview, height = 44.dp, cornerRadius = 14.dp, modifier = Modifier.weight(1f))
            when {
                item.equipped -> NeonSecondaryButton("Снять", onUnequip, enabled = !isBusy, height = 44.dp, cornerRadius = 14.dp, accentColor = AuraColors.TextPrimary, modifier = Modifier.weight(1f))
                item.owned -> NeonPrimaryButton("Надеть", onEquip, enabled = !isBusy, height = 44.dp, cornerRadius = 14.dp, gradient = Brush.horizontalGradient(listOf(AuraColors.ElectricCyan, AuraColors.PlasmaViolet)), glowColor = AuraColors.ElectricCyan, textColor = AuraColors.VoidBlack, modifier = Modifier.weight(1f))
                else -> {
                    val price = item.priceCredits ?: 0
                    val canAfford = userCredits >= price
                    NeonPrimaryButton(if (canAfford) "Купить $price ⚡" else "Нужно $price ⚡", onBuy, enabled = !isBusy && canAfford, height = 44.dp, cornerRadius = 14.dp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
