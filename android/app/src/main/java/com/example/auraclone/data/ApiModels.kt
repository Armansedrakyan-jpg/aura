package com.example.auraclone.data

import com.google.gson.Gson
import retrofit2.Response

data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginRequest(val usernameOrEmail: String, val password: String)

data class AuthResponse(val token: String, val userId: Long, val username: String)

data class UserProfileResponse(
    val userId: Long,
    val username: String,
    val email: String
)

data class DeleteAccountResponse(
    val status: String,
    val message: String
)

data class AchievementResponse(
    val code: String,
    val title: String,
    val description: String,
    val unlockedAt: String
)

data class PublicUserProfileResponse(
    val userId: Long,
    val username: String,
    val createdAt: String,
    val avatarFrame: String? = "DEFAULT",
    val avatarEffect: String? = null,
    val equippedFrameId: Long? = null,
    val equippedEffectId: Long? = null,
    val bestScore: Int = 0,
    val bestLevel: String? = null,
    val averageScore: Double = 0.0,
    val totalAnalyses: Int = 0,
    val totalBattles: Int = 0,
    val winsCount: Int = 0,
    val achievements: List<AchievementResponse> = emptyList()
)

data class CooldownStatusResponse(
    val canGenerateFree: Boolean,
    val remainingSeconds: Long,
    val nextFreeAt: String? = null,
    val credits: Int = 0
)

data class ShopItem(
    val id: Long,
    val code: String,
    val type: String,
    val name: String,
    val description: String,
    val rarity: String,
    val priceCredits: Int? = null,
    val googlePlayProductId: String? = null,
    val creditsAmount: Int? = null,
    val owned: Boolean = false,
    val equipped: Boolean = false
)

data class ShopCatalogResponse(
    val credits: Int = 0,
    val equippedFrameId: Long? = null,
    val avatarFrame: String? = "DEFAULT",
    val equippedEffectId: Long? = null,
    val avatarEffect: String? = null,
    val items: List<ShopItem> = emptyList()
)

data class BuyItemResponse(
    val itemId: Long,
    val code: String,
    val type: String,
    val credits: Int,
    val owned: Boolean
)

data class EquipItemResponse(
    val equippedFrameId: Long? = null,
    val avatarFrame: String? = "DEFAULT",
    val equippedEffectId: Long? = null,
    val avatarEffect: String? = null
)

data class VerifyPurchaseRequest(
    val productId: String,
    val purchaseToken: String
)

data class VerifyPurchaseResponse(
    val transactionId: Long,
    val productId: String,
    val creditsAdded: Int,
    val totalCredits: Int,
    val status: String
)

data class AuraAnalysis(
    val id: Long,
    val userId: Long,
    val score: Int,
    val level: String,
    val vibe: String,
    val energy: String,
    val confidence: String,
    val personality: String,
    val verdict: String,
    val createdAt: String
)

data class ApiErrorResponse(
    val status: Int = 0,
    val message: String = "",
    val remainingSeconds: Long? = null,
    val nextFreeAt: String? = null,
    val credits: Int? = null
)

private val errorGson = Gson()

fun parseApiError(response: Response<*>, fallbackMessage: String): ApiErrorResponse {
    val raw = try {
        response.errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    if (!raw.isNullOrBlank()) {
        try {
            val parsed = errorGson.fromJson(raw, ApiErrorResponse::class.java)
            if (parsed != null && parsed.message.isNotBlank()) {
                return parsed.copy(status = if (parsed.status != 0) parsed.status else response.code())
            }
        } catch (_: Exception) {
        }
        return ApiErrorResponse(status = response.code(), message = raw)
    }
    return ApiErrorResponse(status = response.code(), message = fallbackMessage)
}

