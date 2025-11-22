package com.dishut_lampung.sitanihut.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.home_page.kkph.HomePageKkphRoute
import com.dishut_lampung.sitanihut.presentation.login.LoginRoute
import com.dishut_lampung.sitanihut.presentation.profile.PetaniProfileScreen

fun NavGraphBuilder.kkphNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.HomeKkph.route, route = "kkph") {
        composable(route = Screen.HomeKkph.route) {
            val onNavigateToDetail = { id : String ->
                navController.navigateSingleTop("report-verification/$id")
            }
            val onNavigateToCommodity = {
                navController.navigate("data_commodity")
            }
            val onNavigateToKTH = {
                navController.navigate("data_kth")
            }
            val onNavigateToPetani = {
                navController.navigate("data_petani")
            }
            val onNavigateToPenyuluh = {
                navController.navigate("data_penyuluh")
            }
            val onNavigateToReportVerification = {
                navController.navigate("report-verification")
            }
            val onNavigateToUserManagement = {
                navController.navigate("user-management")
            }
            val onNavigateToInfo = {
                navController.navigate("information")
            }
            HomePageKkphRoute(
                modifier = Modifier,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToCommodity = onNavigateToCommodity,
                onNavigateToKTH = onNavigateToKTH,
                onNavigateToPetani = onNavigateToPetani,
                onNavigateToPenyuluh = onNavigateToPenyuluh,
                onNavigateToReportVerification = onNavigateToReportVerification,
                onNavigateToUserManagement = onNavigateToUserManagement,
                onNavigateToInfo = onNavigateToInfo
            )
        }
        composable(route = Screen.Kkph.ProfileKkph.route){
            PetaniProfileScreen(navController = navController)
        }
    }
}