package com.dishut_lampung.sitanihut.presentation.navigation

sealed class Screen(val route: String) {

    data object LandingPage : Screen("landing_screen")
    data object HomePetani : Screen("home_screen_petani")
    data object HomePenyuluh : Screen("home_screen_penyuluh")
    data object HomeKkph :Screen("home_screen_kkph")
    data object Profile : Screen("profile_screen")

    sealed class Auth(route : String) : Screen(route) {
        data object Login : Screen("login_screen")
        data object ForgotPassword : Screen("forgot_password_screen")
    }
}