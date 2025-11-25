package com.dishut_lampung.sitanihut.presentation.navigation

sealed class Screen(val route: String) {

    data object LandingPage : Screen("landing_screen")
    data object HomePetani : Screen("home_screen_petani")
    data object HomePenyuluh : Screen("home_screen_penyuluh")
    data object HomeKkph :Screen("home_screen_penanggung-jawab")

    object Information : Screen("information")
    object About : Screen("information/about-app")
    object Contact : Screen("information/contact")
    object Dishut : Screen("information/about-company")

    object DataCommodity : Screen("data_commodity")

    object DataKth : Screen("data_kth")
    object DataPetani : Screen("data_petani")
    object ReportVerification : Screen("report-verification")
    data class DetailReportVerification(val id : String) : Screen("report-verification/$id")
    object UserManagement : Screen("user-management")

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
    }

    sealed class  PenanggungJawab(route: String) : Screen(route) {
        data object ProfilePenanggungJawab : PenanggungJawab("profile_penanggung_jawab_screen")
        object DataPenyuluh: PenanggungJawab("data_penyuluh")
    }

}