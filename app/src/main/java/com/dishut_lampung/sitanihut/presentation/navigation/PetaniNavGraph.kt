package com.dishut_lampung.sitanihut.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.home_page.petani.HomePagePetaniRoute
import com.dishut_lampung.sitanihut.presentation.profile.PetaniProfileScreen

fun NavGraphBuilder.petaniNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.HomePetani.route, route = "petani") {
        composable(route = Screen.HomePetani.route) {
            val onNavigateToDetail = { id : String ->
                navController.navigateSingleTop("report-submission/$id")
            }
            val onNavigateToCommodity = {
                navController.navigateSingleTop("data_commodity")
            }
            val onNavigateToReportSubmission = {
                navController.navigateSingleTop("report-submission")
            }
            val onNavigateInfo = {
                navController.navigateSingleTop("information")
            }

            HomePagePetaniRoute(
                modifier = modifier,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToCommodity = onNavigateToCommodity,
                onNavigateToReportSubmission = onNavigateToReportSubmission,
                onNavigateToInfo = onNavigateInfo
            )
        }

        composable(route = Screen.Petani.ProfilePetani.route){
            PetaniProfileScreen(navController = navController)
        }
    }
}