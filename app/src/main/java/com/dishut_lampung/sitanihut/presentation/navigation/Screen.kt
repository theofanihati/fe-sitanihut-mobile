package com.dishut_lampung.sitanihut.presentation.navigation

sealed class Screen(val route: String) {

    data object LandingPage : Screen("landing_screen")
    data object HomePetani : Screen("home_screen_petani")
    data object HomePenyuluh : Screen("home_screen_penyuluh")
    data object HomeKkph :Screen("home_screen_kkph")

    sealed class Auth(route : String) : Screen(route) {
        data object Login : Screen("login_screen")
        data object ForgotPassword : Screen("forgot_password_screen")
    }

    sealed class  Petani(route: String) : Screen(route) {
        data object ProfilePetani : Screen("profile_petani_screen")
    }

    sealed class  Penyuluh(route: String) : Screen(route) {
        data object ProfilePenyuluh : Screen("profile_penyuluh_screen")
    }

    sealed class  Kkph(route: String) : Screen(route) {
        data object ProfileKkph : Screen("profile_kkph_screen")
    }

}