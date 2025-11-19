package com.dishut_lampung.sitanihut.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.login.LoginRoute
import com.dishut_lampung.sitanihut.presentation.profile.PetaniProfileScreen

fun NavGraphBuilder.kkphNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.HomeKkph.route, route = "kkph") {
        composable(route = Screen.HomeKkph.route) {
            LoginRoute(navController = navController)
        }
        composable(route = Screen.Kkph.ProfileKkph.route){
            PetaniProfileScreen(navController = navController)
        }
    }
}