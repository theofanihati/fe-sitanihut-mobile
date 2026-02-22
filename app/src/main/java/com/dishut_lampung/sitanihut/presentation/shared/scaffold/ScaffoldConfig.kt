package com.dishut_lampung.sitanihut.presentation.shared.scaffold

import com.dishut_lampung.sitanihut.presentation.shared.navigation.Screen

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

        // PAKE BACK NAV DARK
        Screen.Petani.ProfilePetani.route,
        Screen.Penyuluh.ProfilePenyuluh.route,
        Screen.PenanggungJawab.ProfilePenanggungJawab.route,
        "penyuluh_detail/{id}",
        "kth_detail/{kthId}",
        "petani_detail/{id}",
        "user_detail/{id}" -> ScaffoldConfig(
            showMainNav = false,
            showBackNav = true,
            topBarTheme = TopBarTheme.DARK_BACKGROUND
        )

        // PAKE BACK NAV LIGHT
        Screen.Information.route,
        Screen.About.route,
        Screen.Dishut.route,
        Screen.Contact.route,
        Screen.DataCommodity.route,
        Screen.PenanggungJawab.DataPenyuluh.route,
        Screen.DataKth.route,
        Screen.KthForm.route,
        Screen.DataPetani.route,
        Screen.PetaniForm.route,
        Screen.UserManagement.route,
        Screen.UserForm.route,
        Screen.ReportList.route,
        Screen.ReportForm.route,
        Screen.KthForm.route,
        "report_detail/{reportId}"-> ScaffoldConfig(
            showMainNav = false,
            showBackNav = true,
            topBarTheme = TopBarTheme.LIGHT_BACKGROUND
        )
        else -> ScaffoldConfig()
    }
}