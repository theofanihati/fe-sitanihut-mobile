package com.dishut_lampung.sitanihut.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.forgot_password.ForgotPasswordRoute
import com.dishut_lampung.sitanihut.presentation.login.LoginRoute
import com.dishut_lampung.sitanihut.presentation.profile.PetaniProfileScreen

fun NavGraphBuilder.petaniNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.HomePetani.route, route = "petani") {
        composable(route = Screen.HomePetani.route) {
            LoginRoute(navController = navController)
        }
        composable(route = Screen.Petani.ProfilePetani.route){
            PetaniProfileScreen(navController = navController)
        }
    }
}