package com.example.auraclone.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillingProductInfo(
    val productId: String,
    val formattedPrice: String,
    val productDetails: ProductDetails
)

class BillingManager(
    context: Context,
    private val onPurchaseTokenReady: (productId: String, purchaseToken: String, onVerified: () -> Unit) -> Unit,
    private val onBillingError: (String) -> Unit
) {
    private val _products = MutableStateFlow<Map<String, BillingProductInfo>>(emptyMap())
    val products: StateFlow<Map<String, BillingProductInfo>> = _products.asStateFlow()

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: StateFlow<Boolean> = _isBillingReady.asStateFlow()

    private val pendingProductIds = mutableSetOf<String>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                onBillingError("Покупка отменена")
            }
            else -> {
                onBillingError("Ошибка Google Play Billing: ${billingResult.debugMessage.ifBlank { billingResult.responseCode.toString() }}")
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun startConnection(productIds: List<String>) {
        pendingProductIds.addAll(productIds.filter { it.isNotBlank() })
        if (billingClient.isReady) {
            _isBillingReady.value = true
            queryProducts(pendingProductIds.toList())
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isBillingReady.value = true
                    if (pendingProductIds.isNotEmpty()) {
                        queryProducts(pendingProductIds.toList())
                    }
                    queryUnconsumedPurchases()
                } else {
                    _isBillingReady.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                _isBillingReady.value = false
            }
        })
    }

    fun queryProducts(productIds: List<String>) {
        val cleanIds = productIds.filter { it.isNotBlank() }.distinct()
        pendingProductIds.addAll(cleanIds)
        if (!billingClient.isReady || cleanIds.isEmpty()) return

        val productList = cleanIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val mapped = productDetailsList.associate { details ->
                    val price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    details.productId to BillingProductInfo(
                        productId = details.productId,
                        formattedPrice = price,
                        productDetails = details
                    )
                }
                _products.value = mapped
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        if (!billingClient.isReady) {
            onBillingError("Google Play Billing недоступен на этом устройстве или сборка не из Play Console")
            return
        }
        val info = _products.value[productId]
        if (info == null) {
            onBillingError("Товар $productId не найден в Google Play Console")
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(info.productDetails)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    private fun queryUnconsumedPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val productId = purchase.products.firstOrNull() ?: return
        onPurchaseTokenReady(productId, purchase.purchaseToken) {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.consumeAsync(consumeParams) { _, _ -> }
        }
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
