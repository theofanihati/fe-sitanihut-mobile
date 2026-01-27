package com.dishut_lampung.sitanihut.presentation.shared.navigation

sealed class Screen(val route: String) {

    data object LandingPage : Screen("landing_screen")
    data object HomePetani : Screen("home_screen_petani")
    data object HomePenyuluh : Screen("home_screen_penyuluh")
    data object HomeKkph : Screen("home_screen_penanggung-jawab")

    object Information : Screen("information")
    object About : Screen("information/about-app")
    object Contact : Screen("information/contact")
    object Dishut : Screen("information/about-company")

    object DataCommodity : Screen("data_commodity")

    object ReportList : Petani("report_list")
    data class ReportDetail(val id : String) : Petani("report_detail/{reportId}")
    object ReportForm : Petani("report_form?reportId={reportId}") {
        fun createRoute(reportId: String? = null): String {
            return if (reportId != null) {
                "report_form?reportId=$reportId"
            } else {
                "report_form"
            }
        }
    }

    object DataKth : Screen("data_kth")
    data class KthDetail(val id : String) : Screen("kth_detail/{kthId}")
    object KthForm : Screen("kth_form?kthId={id}"){
        fun createRoute(id: String? =null): String{
            return if (id != null){
                "kth_form?kthId=$id"
            }else{
                "kth_form"
            }
        }
    }

    object DataPetani : Screen("data_petani")
    data class PetaniDetail(val id : String) : Screen("petani_detail/{id}")
    object PetaniForm : Screen("petani_form?petaniId={id}"){
        fun createRoute(id: String? =null): String{
            return if (id != null){
                "petani_form?petaniId=$id"
            }else{
                "petani_form"
            }
        }
    }

    object UserManagement : Screen("user-management")
    data class UserDetail(val id : String) : Screen("user_detail/{id}")
    object UserForm : Screen("user_form?id={id}"){
        fun createRoute(id: String? =null): String{
            return if (id != null){
                "user_form?id=$id"
            }else{
                "user_form"
            }
        }
    }

    sealed class Auth(route : String) : Screen(route) {
        data object Login : Screen("login_screen")
        data object ForgotPassword : Screen("forgot_password_screen")
    }

    sealed class  Petani(route: String) : Screen(route) {
        data object ProfilePetani : Petani("profile_petani_screen")
    }

    sealed class  Penyuluh(route: String) : Screen(route) {
        data object ProfilePenyuluh : Penyuluh("profile_penyuluh_screen")
    }

    sealed class  PenanggungJawab(route: String) : Screen(route) {
        data object ProfilePenanggungJawab : PenanggungJawab("profile_penanggung_jawab_screen")
        object DataPenyuluh: PenanggungJawab("data_penyuluh")
        data class PenyuluhDetail(val id : String): PenanggungJawab("data_penyuluh/{id}")
    }
}