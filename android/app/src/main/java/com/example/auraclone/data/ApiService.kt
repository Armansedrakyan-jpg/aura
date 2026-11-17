package com.example.auraclone.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserProfileResponse>

    @DELETE("api/auth/account")
    suspend fun deleteAccount(): Response<DeleteAccountResponse>

    @Multipart
    @POST("api/aura/analyze")
    suspend fun analyze(
        @Part photo: MultipartBody.Part,
        @Part("aboutMe") aboutMe: RequestBody
    ): Response<AuraAnalysis>

    @GET("api/aura/history")
    suspend fun history(): Response<List<AuraAnalysis>>

    @GET("api/aura/cooldown")
    suspend fun getCooldown(): Response<CooldownStatusResponse>

    @POST("api/battles/start")
    suspend fun startBattle(@Body player: Player? = null): Response<BattleResponse>

    @PUT("api/battles/join/{battleId}")
    suspend fun joinBattle(
        @Path("battleId") battleId: String,
        @Body player: Player? = null
    ): Response<Battle>

    @GET("api/aura/leaderboard")
    suspend fun getLeaderboard(): Response<List<LeaderboardEntry>>

    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: Long): Response<PublicUserProfileResponse>

    @GET("api/shop/items")
    suspend fun getShopItems(@Query("type") type: String? = null): Response<ShopCatalogResponse>

    @POST("api/shop/buy/{itemId}")
    suspend fun buyShopItem(@Path("itemId") itemId: Long): Response<BuyItemResponse>

    @POST("api/shop/equip/{itemId}")
    suspend fun equipShopItem(@Path("itemId") itemId: Long): Response<EquipItemResponse>

    @POST("api/shop/unequip/{type}")
    suspend fun unequipShopItem(@Path("type") type: String): Response<EquipItemResponse>

    @POST("api/shop/verify-purchase")
    suspend fun verifyPurchase(@Body request: VerifyPurchaseRequest): Response<VerifyPurchaseResponse>
}
