package com.dishut_lampung.sitanihut.presentation.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dishut_lampung.sitanihut.presentation.home_page.kkph.HomePagePenanggungJawabScreen
import com.dishut_lampung.sitanihut.presentation.home_page.kkph.HomePagePenanggungJawabRoute
import com.dishut_lampung.sitanihut.presentation.home_page.penyuluh.HomePagePenyuluhRoute
import com.dishut_lampung.sitanihut.presentation.home_page.penyuluh.HomePagePenyuluhScreen
import com.dishut_lampung.sitanihut.presentation.home_page.petani.HomePagePetaniRoute
import com.dishut_lampung.sitanihut.presentation.home_page.petani.HomePagePetaniScreen
import com.dishut_lampung.sitanihut.presentation.landing_page.LandingPageRoute

@Composable
fun NavGraph(
    navController : NavHostController,
    modifier : Modifier,
    startDestination : String = "landing_screen"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("landing_screen") {
            LandingPageRoute(
                onNavigateToLogin = {
                    navController.navigate("auth") {
                        popUpTo("landing_screen") { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.HomePetani.route) {
            HomePagePetaniRoute(
                modifier = modifier,
                onNavigateToDetail = { id ->
                    navController.navigateSingleTop("report_detail/$id")
                },
                onNavigateToInfo = {
                    navController.navigateToMenu("information")
                },
                onNavigateToCommodity = {
                    navController.navigateToMenu("data_commodity")
                },
                onNavigateToReportSubmission = {
                    navController.navigateToMenu("report_list")
                },
                onNavigateToEdit = { id: String ->
                    navController.navigateSingleTop(
                        Screen.ReportForm.createRoute(reportId = id)
                )
            }

            )
        }
        composable(route = Screen.HomePenyuluh.route) {
            HomePagePenyuluhRoute(
                modifier = modifier,
                onNavigateToDetail = { id : String ->
                    navController.navigateSingleTop("report_detail/$id")
                },
                onNavigateToCommodity = {
                    navController.navigateToMenu("data_commodity")
                },
                onNavigateToKTH = {
                    navController.navigateToMenu("data_kth")
                },
                onNavigateToPetani = {
                    navController.navigateToMenu("data_petani")
                },
                onNavigateToReportVerification = {
                    navController.navigateToMenu("report_list")
                },
                onNavigateToUserManagement = {
                    navController.navigateToMenu("user-management")
                },
                onNavigateToInfo = {
                    navController.navigateToMenu("information")
                },
            )
        }
        composable(route = Screen.HomeKkph.route) {
            HomePagePenanggungJawabRoute(
                modifier = modifier,
                onNavigateToDetail = { id : String ->
                    navController.navigateSingleTop("report_detail/$id")
                },
                onNavigateToCommodity = {
                    navController.navigateToMenu("data_commodity")
                },
                onNavigateToKTH = {
                    navController.navigateToMenu("data_kth")
                },
                onNavigateToPetani = {
                    navController.navigateToMenu("data_petani")
                },
                onNavigateToPenyuluh = {
                    navController.navigateToMenu("data_penyuluh")
                },
                onNavigateToReportVerification = {
                    navController.navigateToMenu("report_list")
                },
                onNavigateToUserManagement = {
                    navController.navigateToMenu("user-management")
                },
                onNavigateToInfo = {
                    navController.navigateToMenu("information")
                },
            )
        }

        authNavGraph(
            modifier = modifier,
            navController = navController
        )
        petaniNavGraph(
            modifier = modifier,
            navController = navController
        )
        penyuluhNavGraph(
            modifier = modifier,
            navController = navController
        )
        kkphNavGraph(
            modifier = modifier,
            navController = navController
        )
    }
}