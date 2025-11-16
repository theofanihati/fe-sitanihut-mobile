package com.dishut_lampung.sitanihut.presentation.navigation

sealed class Screen(val route: String) {

    data object Home : Screen("home_screen")
    data object Profile : Screen("profile_screen")

    sealed class Auth(route : String) : Screen(route) {
        data object Login : Screen("login_screen")
        data object ForgotPassword : Screen("forgot_password_screen")
    }
}