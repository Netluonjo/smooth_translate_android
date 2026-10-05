package com.smooth.translate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smooth.translate.ui.navigation.Screen
import com.smooth.translate.ui.screens.CameraTranslateScreen
import com.smooth.translate.ui.screens.FaceToFaceScreen
import com.smooth.translate.ui.screens.HomeScreen
import com.smooth.translate.ui.screens.OfflineModelsScreen
import com.smooth.translate.ui.screens.SettingsScreen
import com.smooth.translate.ui.screens.TextTranslateScreen
import com.smooth.translate.ui.screens.VoiceTranslateScreen
import com.smooth.translate.ui.theme.SmoothTranslateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmoothTranslateTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SmoothTranslateNavGraph()
                }
            }
        }
    }
}

@Composable
fun SmoothTranslateNavGraph() {
    val navController = rememberNavController()
    val app = SmoothTranslateApp.instance

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                translationRepo = app.translationRepository,
                onNavigate = { screen -> navController.navigate(screen.route) }
            )
        }

        composable(Screen.TextTranslate.route) {
            TextTranslateScreen(
                translationRepo = app.translationRepository,
                historyRepo = app.historyRepository,
                ttsManager = app.ttsManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CameraTranslate.route) {
            CameraTranslateScreen(
                translationRepo = app.translationRepository,
                ttsManager = app.ttsManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.VoiceTranslate.route) {
            VoiceTranslateScreen(
                translationRepo = app.translationRepository,
                ttsManager = app.ttsManager,
                speechRecognizer = app.speechRecognizerHelper,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FaceToFace.route) {
            FaceToFaceScreen(
                translationRepo = app.translationRepository,
                ttsManager = app.ttsManager,
                speechRecognizer = app.speechRecognizerHelper,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OfflineModels.route) {
            OfflineModelsScreen(
                translationRepo = app.translationRepository,
                historyRepo = app.historyRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                historyRepo = app.historyRepository,
                onNavigate = { screen -> navController.navigate(screen.route) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
