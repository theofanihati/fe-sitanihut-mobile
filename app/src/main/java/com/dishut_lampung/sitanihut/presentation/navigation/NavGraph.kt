package com.dishut_lampung.sitanihut.presentation.navigation

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
                    navController.navigate("information")
                },
                onNavigateToCommodity = {
                    navController.navigate("data_commodity")
                },
                onNavigateToReportSubmission = {
                    navController.navigate("report_list")
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
                onNavigateToDetail = {
                    navController.navigate("profile_penyuluh_screen")
                },
                onNavigateToCommodity = {
                    navController.navigate("data_commodity")
                },
                onNavigateToKTH = {
                    navController.navigate("data_kth")
                },
                onNavigateToPetani = {
                    navController.navigate("data_petani")
                },
                onNavigateToReportVerification = {
                    navController.navigate("report_list")
                },
                onNavigateToUserManagement = {
                    navController.navigate("user-management")
                },
                onNavigateToInfo = {
                    navController.navigate("information")
                },
            )
        }
        composable(route = Screen.HomeKkph.route) {
            HomePagePenanggungJawabRoute(
                modifier = modifier,
                onNavigateToDetail = {
                    navController.navigate("profile_kkph_screen")
                },
                onNavigateToCommodity = {
                    navController.navigate("data_commodity")
                },
                onNavigateToKTH = {
                    navController.navigate("data_kth")
                },
                onNavigateToPetani = {
                    navController.navigate("data_petani")
                },
                onNavigateToPenyuluh = {
                    navController.navigate("data_penyuluh")
                },
                onNavigateToReportVerification = {
                    navController.navigate("report_list")
                },
                onNavigateToUserManagement = {
                    navController.navigate("user-management")
                },
                onNavigateToInfo = {
                    navController.navigate("information")
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