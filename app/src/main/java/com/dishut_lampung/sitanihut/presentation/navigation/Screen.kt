package com.dishut_lampung.sitanihut.presentation.navigation

sealed class Screen(val route: String) {

    data object LandingPage : Screen("landing_screen")
    data object HomePetani : Screen("home_screen_petani")
    data object HomePenyuluh : Screen("home_screen_penyuluh")
    data object HomeKkph :Screen("home_screen_kkph")

    object Information : Screen("information")
    object DataCommodity : Screen("data_commodity")

    sealed class Auth(route : String) : Screen(route) {
        data object Login : Screen("login_screen")
        data object ForgotPassword : Screen("forgot_password_screen")
    }

    sealed class  Petani(route: String) : Screen(route) {
        data object ProfilePetani : Petani("profile_petani_screen")

        object ReportSubmission : Petani("report-submission")
        data class DetailReportSubmission(val id : String) : Petani("report-submission/$id")
    }

    sealed class  Penyuluh(route: String) : Screen(route) {
        data object ProfilePenyuluh : Penyuluh("profile_penyuluh_screen")
        object DataKth : Penyuluh("data_kth")
        object DataPetani : Penyuluh("data_petani")
        object ReportVerification : Penyuluh("report-verification")
        data class DetailReportVerification(val id : String) : Penyuluh("report-verification/$id")
        object UserManagement : Penyuluh("user-management")
    }

    sealed class  Kkph(route: String) : Screen(route) {
        data object ProfileKkph : Kkph("profile_kkph_screen")
    }

}