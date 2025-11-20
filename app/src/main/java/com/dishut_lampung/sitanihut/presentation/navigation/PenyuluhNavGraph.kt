package com.dishut_lampung.sitanihut.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.home_page.penyuluh.HomePagePenyuluhScreen
import com.dishut_lampung.sitanihut.presentation.login.LoginRoute
import com.dishut_lampung.sitanihut.presentation.profile.PetaniProfileScreen

fun NavGraphBuilder.penyuluhNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.HomePenyuluh.route, route = "penyuluh") {
        composable(route = Screen.HomePenyuluh.route) {
            HomePagePenyuluhScreen(modifier = Modifier)
        }
        composable(route = Screen.Penyuluh.ProfilePenyuluh.route){
            PetaniProfileScreen(navController = navController)
        }
    }
}