package com.example.auraclone.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auraclone.data.*
import com.google.firebase.database.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Error(val message: String) : UiState()
    data class Success(val result: AuraAnalysis) : UiState()
}

sealed class ActionState {
    object Idle : ActionState()
    object Loading : ActionState()
    data class Error(val message: String) : ActionState()
    object Success : ActionState()
}

data class CooldownUiState(
    val isLoading: Boolean = false,
    val canGenerateFree: Boolean = true,
    val remainingSeconds: Long = 0L,
    val nextFreeAt: String? = null,
    val credits: Int = 0,
    val error: String? = null
)

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _authState = MutableStateFlow<UiState>(UiState.Idle)
    val authState: StateFlow<UiState> = _authState

    private val _analyzeState = MutableStateFlow<UiState>(UiState.Idle)
    val analyzeState: StateFlow<UiState> = _analyzeState

    private val _history = MutableStateFlow<List<AuraAnalysis>>(emptyList())
    val history: StateFlow<List<AuraAnalysis>> = _history

    private val _myProfile = MutableStateFlow<UserProfileResponse?>(null)
    val myProfile: StateFlow<UserProfileResponse?> = _myProfile.asStateFlow()

    private val _myPublicStats = MutableStateFlow<PublicUserProfileResponse?>(null)
    val myPublicStats: StateFlow<PublicUserProfileResponse?> = _myPublicStats.asStateFlow()

    private val _profileLoadState = MutableStateFlow<ActionState>(ActionState.Idle)
    val profileLoadState: StateFlow<ActionState> = _profileLoadState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<ActionState>(ActionState.Idle)
    val deleteAccountState: StateFlow<ActionState> = _deleteAccountState.asStateFlow()

    private val _cooldownState = MutableStateFlow(CooldownUiState())
    val cooldownState: StateFlow<CooldownUiState> = _cooldownState.asStateFlow()
    private var cooldownTickerJob: Job? = null

    private val _publicProfile = MutableStateFlow<PublicUserProfileResponse?>(null)
    val publicProfile: StateFlow<PublicUserProfileResponse?> = _publicProfile.asStateFlow()

    private val _publicProfileState = MutableStateFlow<ActionState>(ActionState.Idle)
    val publicProfileState: StateFlow<ActionState> = _publicProfileState.asStateFlow()

    private val _shopCatalog = MutableStateFlow<ShopCatalogResponse?>(null)
    val shopCatalog: StateFlow<ShopCatalogResponse?> = _shopCatalog.asStateFlow()

    private val _shopState = MutableStateFlow<ActionState>(ActionState.Idle)
    val shopState: StateFlow<ActionState> = _shopState.asStateFlow()

    private val _shopActionMessage = MutableStateFlow<String?>(null)
    val shopActionMessage: StateFlow<String?> = _shopActionMessage.asStateFlow()

    private val _sessionExpiredEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvents: SharedFlow<Unit> = _sessionExpiredEvents.asSharedFlow()

    // --- ФУНКЦИИ FIREBASE REALTIME DATABASE ДЛЯ ДУЭЛЕЙ ---
    private val database = FirebaseDatabase.getInstance()
    private val _currentBattle = MutableStateFlow<Battle?>(null)
    val currentBattle: StateFlow<Battle?> = _currentBattle.asStateFlow()
    private var battleListener: ValueEventListener? = null
    private var currentBattleId: String? = null

    init {
        RetrofitClient.init(application)
        viewModelScope.launch {
            RetrofitClient.unauthorizedEvents.collect {
                clearLocalSessionState()
                _sessionExpiredEvents.tryEmit(Unit)
            }
        }
    }

    fun listenToBattle(battleId: String) {
        currentBattleId?.let { oldId ->
            battleListener?.let { database.getReference("battles").child(oldId).removeEventListener(it) }
        }
        currentBattleId = battleId

        val ref = database.getReference("battles").child(battleId)

        battleListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _currentBattle.value = snapshot.getValue(Battle::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                _analyzeState.value = UiState.Error("Ошибка базы данных: ${error.message}")
            }
        }
        ref.addValueEventListener(battleListener!!)
    }
    // -----------------------------------------------------

    fun isLoggedIn(): Boolean = tokenManager.getToken() != null

    fun getCachedUsername(): String = tokenManager.getUsername() ?: "Игрок"

    fun register(username: String, email: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val response = RetrofitClient.api.register(RegisterRequest(username, email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    tokenManager.saveSession(body.token, body.userId, body.username)
                    _authState.value = UiState.Idle
                    onDone()
                } else {
                    val err = parseApiError(response, "Ошибка регистрации")
                    _authState.value = UiState.Error(err.message)
                }
            } catch (e: Exception) {
                _authState.value = UiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun login(usernameOrEmail: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val response = RetrofitClient.api.login(LoginRequest(usernameOrEmail, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    tokenManager.saveSession(body.token, body.userId, body.username)
                    _authState.value = UiState.Idle
                    onDone()
                } else {
                    val err = parseApiError(response, "Неверный логин или пароль")
                    _authState.value = UiState.Error(err.message)
                }
            } catch (e: Exception) {
                _authState.value = UiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun loadMyProfile() {
        viewModelScope.launch {
            _profileLoadState.value = ActionState.Loading
            try {
                val meResponse = RetrofitClient.api.getMe()
                if (meResponse.isSuccessful && meResponse.body() != null) {
                    val profile = meResponse.body()!!
                    _myProfile.value = profile
                    val token = tokenManager.getToken()
                    if (!token.isNullOrBlank()) {
                        tokenManager.saveSession(token, profile.userId, profile.username)
                    }
                    _profileLoadState.value = ActionState.Success

                    try {
                        val publicResponse = RetrofitClient.api.getUserProfile(profile.userId)
                        if (publicResponse.isSuccessful && publicResponse.body() != null) {
                            _myPublicStats.value = publicResponse.body()
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    val err = parseApiError(meResponse, "Не удалось загрузить профиль")
                    _profileLoadState.value = ActionState.Error(err.message)
                }
            } catch (e: Exception) {
                _profileLoadState.value = ActionState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        tokenManager.clear()
        clearLocalSessionState()
        onDone()
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _deleteAccountState.value = ActionState.Loading
            try {
                val response = RetrofitClient.api.deleteAccount()
                if (response.isSuccessful) {
                    tokenManager.clear()
                    clearLocalSessionState()
                    _deleteAccountState.value = ActionState.Success
                    onDeleted()
                } else {
                    val err = parseApiError(response, "Не удалось удалить аккаунт")
                    _deleteAccountState.value = ActionState.Error(err.message)
                }
            } catch (e: Exception) {
                _deleteAccountState.value = ActionState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = ActionState.Idle
    }

    private fun clearLocalSessionState() {
        cooldownTickerJob?.cancel()
        _authState.value = UiState.Idle
        _analyzeState.value = UiState.Idle
        _history.value = emptyList()
        _myProfile.value = null
        _myPublicStats.value = null
        _profileLoadState.value = ActionState.Idle
        _deleteAccountState.value = ActionState.Idle
        _cooldownState.value = CooldownUiState()
        _publicProfile.value = null
        _publicProfileState.value = ActionState.Idle
        _shopCatalog.value = null
        _shopState.value = ActionState.Idle
        _shopActionMessage.value = null
        _currentBattle.value = null
    }

    fun loadCooldown() {
        viewModelScope.launch {
            _cooldownState.value = _cooldownState.value.copy(isLoading = true, error = null)
            try {
                val response = RetrofitClient.api.getCooldown()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    updateCooldown(
                        canGenerateFree = body.canGenerateFree,
                        remainingSeconds = body.remainingSeconds,
                        nextFreeAt = body.nextFreeAt,
                        credits = body.credits
                    )
                } else {
                    val err = parseApiError(response, "Не удалось загрузить лимиты")
                    _cooldownState.value = _cooldownState.value.copy(isLoading = false, error = err.message)
                }
            } catch (e: Exception) {
                _cooldownState.value = _cooldownState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка сети"
                )
            }
        }
    }

    private fun updateCooldown(
        canGenerateFree: Boolean,
        remainingSeconds: Long,
        nextFreeAt: String?,
        credits: Int
    ) {
        cooldownTickerJob?.cancel()
        val safeRemaining = remainingSeconds.coerceAtLeast(0L)
        _cooldownState.value = CooldownUiState(
            isLoading = false,
            canGenerateFree = canGenerateFree || safeRemaining == 0L,
            remainingSeconds = safeRemaining,
            nextFreeAt = nextFreeAt,
            credits = credits,
            error = null
        )

        if (!canGenerateFree && safeRemaining > 0L) {
            cooldownTickerJob = viewModelScope.launch {
                var current = safeRemaining
                while (current > 0L) {
                    delay(1000L)
                    current -= 1L
                    _cooldownState.value = _cooldownState.value.copy(
                        remainingSeconds = current,
                        canGenerateFree = current <= 0L,
                        nextFreeAt = if (current <= 0L) null else _cooldownState.value.nextFreeAt
                    )
                }
            }
        }
    }

    fun loadPublicProfile(userId: Long) {
        viewModelScope.launch {
            _publicProfileState.value = ActionState.Loading
            _publicProfile.value = null
            try {
                val response = RetrofitClient.api.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    _publicProfile.value = response.body()!!
                    _publicProfileState.value = ActionState.Success
                } else {
                    val err = parseApiError(response, "Не удалось загрузить профиль игрока")
                    _publicProfileState.value = ActionState.Error(err.message)
                }
            } catch (e: Exception) {
                _publicProfileState.value = ActionState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun loadShopItems() {
        viewModelScope.launch {
            _shopState.value = ActionState.Loading
            try {
                val response = RetrofitClient.api.getShopItems()
                if (response.isSuccessful && response.body() != null) {
                    val catalog = response.body()!!
                    _shopCatalog.value = catalog
                    _cooldownState.value = _cooldownState.value.copy(credits = catalog.credits)
                    _shopState.value = ActionState.Success
                } else {
                    val err = parseApiError(response, "Не удалось загрузить магазин")
                    _shopState.value = ActionState.Error(err.message)
                }
            } catch (e: Exception) {
                _shopState.value = ActionState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun buyShopItem(itemId: Long) {
        viewModelScope.launch {
            _shopState.value = ActionState.Loading
            try {
                val response = RetrofitClient.api.buyShopItem(itemId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _shopActionMessage.value = "Куплено! Остаток: ${body.credits} ⚡"
                    loadShopItems()
                } else {
                    val err = parseApiError(response, "Не удалось купить предмет")
                    _shopState.value = ActionState.Error(err.message)
                }
            } catch (e: Exception) {
                _shopState.value = ActionState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun equipShopItem(itemId: Long) {
        viewModelScope.launch {
            _shopState.value = ActionState.Loading
            try {
                val response = RetrofitClient.api.equipShopItem(itemId)
                if (response.isSuccessful) {
                    _shopActionMessage.value = "Предмет надет!"
                    loadShopItems()
                } else {
                    val err = parseApiError(response, "Не удалось надеть предмет")
                    _shopState.value = ActionState.Error(err.message)
                }
            } catch (e: Exception) {
                _shopState.value = ActionState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun unequipShopItem(type: String) {
        viewModelScope.launch {
            _shopState.value = ActionState.Loading
            try {
                val response = RetrofitClient.api.unequipShopItem(type)
                if (response.isSuccessful) {
                    _shopActionMessage.value = "Снято!"
                    loadShopItems()
                } else {
                    val err = parseApiError(response, "Не удалось снять предмет")
                    _shopState.value = ActionState.Error(err.message)
                }
            } catch (e: Exception) {
                _shopState.value = ActionState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun verifyGooglePlayPurchase(
        productId: String,
        purchaseToken: String,
        onVerified: () -> Unit
    ) {
        viewModelScope.launch {
            _shopState.value = ActionState.Loading
            try {
                val response = RetrofitClient.api.verifyPurchase(
                    VerifyPurchaseRequest(productId = productId, purchaseToken = purchaseToken)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _shopActionMessage.value = "Начислено +${body.creditsAdded} ⚡ (Всего: ${body.totalCredits})"
                    onVerified()
                    loadShopItems()
                } else {
                    val err = parseApiError(response, "Ошибка подтверждения покупки")
                    _shopState.value = ActionState.Error(err.message)
                }
            } catch (e: Exception) {
                _shopState.value = ActionState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun setShopMessage(message: String?) {
        _shopActionMessage.value = message
    }

    fun analyze(imageUri: Uri, aboutMe: String) {
        viewModelScope.launch {
            _analyzeState.value = UiState.Loading
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val tempFile = File.createTempFile("photo", ".jpg", context.cacheDir)
                FileOutputStream(tempFile).use { output ->
                    inputStream?.copyTo(output)
                }

                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("photo", tempFile.name, requestFile)
                val aboutMePart = aboutMe.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = RetrofitClient.api.analyze(photoPart, aboutMePart)

                if (response.isSuccessful && response.body() != null) {
                    _analyzeState.value = UiState.Success(response.body()!!)
                    loadCooldown()
                } else {
                    val err = parseApiError(response, "Ошибка анализа")
                    if (response.code() == 429 && err.remainingSeconds != null) {
                        updateCooldown(
                            canGenerateFree = false,
                            remainingSeconds = err.remainingSeconds,
                            nextFreeAt = err.nextFreeAt,
                            credits = err.credits ?: _cooldownState.value.credits
                        )
                    }
                    _analyzeState.value = UiState.Error(err.message)
                }
            } catch (e: Exception) {
                _analyzeState.value = UiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.history()
                if (response.isSuccessful && response.body() != null) {
                    _history.value = response.body()!!
                }
            } catch (_: Exception) {
            }
        }
    }

    fun resetAnalyzeState() {
        _analyzeState.value = UiState.Idle
    }

    private fun buildFallbackPlayer(): Player {
        val latest = _history.value.firstOrNull()
        val uid = tokenManager.getUserId().takeIf { it > 0L } ?: _myProfile.value?.userId ?: 0L
        val uname = tokenManager.getUsername() ?: _myProfile.value?.username ?: "Player"
        return Player(
            userId = uid,
            username = uname,
            score = latest?.score ?: 50,
            title = latest?.level ?: "NEWBIE"
        )
    }

    fun startBattle(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            var battleId: String? = null
            try {
                val player = buildFallbackPlayer()
                val response = RetrofitClient.api.startBattle(player)
                if (response.isSuccessful && response.body() != null) {
                    battleId = response.body()!!.battleId
                } else {
                    val err = parseApiError(response, "Ошибка создания дуэли")
                    _analyzeState.value = UiState.Error(err.message)
                }
            } catch (e: Exception) {
                android.util.Log.e("AuraError", "Сетевая ошибка создания дуэли", e)
                _analyzeState.value = UiState.Error("Ошибка создания дуэли: ${e.message}")
            }

            battleId?.let { onCreated(it) }
        }
    }

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard

    fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getLeaderboard()
                if (response.isSuccessful && response.body() != null) {
                    _leaderboard.value = response.body()!!
                }
            } catch (_: Exception) {
            }
        }
    }

    fun joinBattle(battleId: String, onJoined: () -> Unit) {
        viewModelScope.launch {
            try {
                val player = buildFallbackPlayer()
                val response = RetrofitClient.api.joinBattle(battleId, player)
                if (response.isSuccessful) {
                    onJoined()
                } else {
                    val err = parseApiError(response, "Не удалось войти в дуэль")
                    _analyzeState.value = UiState.Error(err.message)
                }
            } catch (e: Exception) {
                android.util.Log.e("AuraError", "Ошибка вызова joinBattle", e)
                _analyzeState.value = UiState.Error("Ошибка подключения: ${e.message}")
            }
        }
    }

}
