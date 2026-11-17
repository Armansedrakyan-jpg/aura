package com.example.auraclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auraclone.ui.AuraViewModel
import com.example.auraclone.ui.components.AuraTab
import com.example.auraclone.ui.screens.*
import com.example.auraclone.ui.theme.AuraColors
import com.example.auraclone.ui.theme.AuraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraTheme {
                Surface(modifier = Modifier, color = AuraColors.VoidBlack) {
                    AuraApp()
                }
            }
        }
    }
}

@Composable
fun AuraApp() {
    val navController = rememberNavController()
    val viewModel: AuraViewModel = viewModel()

    val startDestination = if (viewModel.isLoggedIn()) "home" else "auth"

    LaunchedEffect(Unit) {
        viewModel.sessionExpiredEvents.collect {
            navController.navigate("auth") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(280)) { it / 14 } },
        exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(240)) { -it / 18 } },
        popEnterTransition = { fadeIn(tween(240)) + slideInHorizontally(tween(260)) { -it / 16 } },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(240)) { it / 14 } }
    ) {
        composable("auth") {
            AuthScreen(viewModel = viewModel, onLoggedIn = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onCreateAnalysis = { navController.navigate("create") },
                onHistory = { navController.navigate("history") },
                onNavigateToBattle = { battleId -> navController.navigate("battle/$battleId") },
                onLeaderboard = { navController.switchTab(AuraTab.LEADERBOARD) },
                onShop = { navController.switchTab(AuraTab.SHOP) },
                onProfile = { navController.switchTab(AuraTab.PROFILE) }
            )
        }

        composable("shop") {
            ShopScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSelectTab = { tab -> navController.switchTab(tab) }
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSelectTab = { tab -> navController.switchTab(tab) }
            )
        }

        composable("leaderboard") {
            LeaderboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onUserClick = { userId -> navController.navigate("user/$userId") },
                onSelectTab = { tab -> navController.switchTab(tab) }
            )
        }

        composable("user/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: 0L
            PublicProfileScreen(
                viewModel = viewModel,
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("create") {
            CreateAnalysisScreen(
                viewModel = viewModel,
                onResult = { navController.navigate("result") },
                onOpenShop = { navController.navigate("shop") }
            )
        }

        composable("result") {
            ResultScreen(viewModel = viewModel, onBackToHome = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            })
        }

        composable("history") {
            HistoryScreen(viewModel = viewModel, onBack = {
                navController.popBackStack()
            })
        }

        composable("battle/{battleId}") { backStackEntry ->
            val battleId = backStackEntry.arguments?.getString("battleId") ?: ""
            BattleScreen(viewModel = viewModel, battleId = battleId, onBack = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            })
        }
    }
}

private fun NavHostController.switchTab(tab: AuraTab) {
    val route = when (tab) {
        AuraTab.HOME -> "home"
        AuraTab.LEADERBOARD -> "leaderboard"
        AuraTab.SHOP -> "shop"
        AuraTab.PROFILE -> "profile"
    }
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}


