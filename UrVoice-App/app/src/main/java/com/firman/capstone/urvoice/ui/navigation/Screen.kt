package com.firman.capstone.urvoice.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {
    @Serializable
    object Splash : Screen("splash")

    @Serializable
    object OnBoarding : Screen("onboarding")

    @Serializable
    object Sign: Screen ("sign")

    @Serializable
    object Login : Screen("login")

    @Serializable
    object Register : Screen("register")

    @Serializable
    object Home : Screen("home")

    @Serializable
    object Analyze: Screen("analyze")

    @Serializable
    object History : Screen("history")

    @Serializable
    data class HistoryDetail(val id: Int) : Screen("history_detail/$id") {
        fun createRoute() = "history_detail/$id"
    }

    @Serializable
    object Profile : Screen("profile")

    @Serializable
    object EditProfile : Screen("editProfile")

    @Serializable
    object ImageProfilePreview : Screen("image_profile_preview")

    @Serializable
    object Record : Screen("record")

    @Serializable
    object SpeechToText: Screen("speech-to-text")

    @Serializable
    data class Article(val id: String) : Screen("article/{id}") {
        fun articleRoute() = "article/$id"
    }
}
