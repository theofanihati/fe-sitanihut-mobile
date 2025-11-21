package com.dishut_lampung.sitanihut.presentation.scaffold

import com.dishut_lampung.sitanihut.presentation.navigation.Screen

enum class TopBarTheme { LIGHT_BACKGROUND, DARK_BACKGROUND }
data class ScaffoldConfig(
    val showMainNav : Boolean = true,
    val showBackNav : Boolean = false,
    val topBarTheme: TopBarTheme = TopBarTheme.LIGHT_BACKGROUND
)

fun scaffoldConfig(currentRoute: String?): ScaffoldConfig {
    return when (currentRoute) {

        // NO TOP BAR TOP BAR CLUB
        Screen.LandingPage.route,
        Screen.Auth.Login.route,
        Screen.Auth.ForgotPassword.route -> ScaffoldConfig(
            showMainNav = false,
            showBackNav = false
        )

        // PAKE MAIN NAV
        Screen.HomePetani.route,
        Screen.HomePenyuluh.route,
        Screen.HomeKkph.route, -> ScaffoldConfig(
            showMainNav = true,
            showBackNav = false,
        )

        // PAKE BACK NAV
        Screen.Petani.ProfilePetani.route,
        Screen.Penyuluh.ProfilePenyuluh.route,
        Screen.Kkph.ProfileKkph.route-> ScaffoldConfig(
            showMainNav = false,
            showBackNav = true,
            topBarTheme = TopBarTheme.DARK_BACKGROUND
        )
        else -> ScaffoldConfig()
    }
}