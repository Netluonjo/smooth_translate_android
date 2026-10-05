package com.smooth.translate.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object TextTranslate : Screen("text_translate")
    data object CameraTranslate : Screen("camera_translate")
    data object VoiceTranslate : Screen("voice_translate")
    data object FaceToFace : Screen("face_to_face")
    data object OfflineModels : Screen("offline_models")
    data object Settings : Screen("settings")
}
