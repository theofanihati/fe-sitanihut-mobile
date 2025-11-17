package com.dishut_lampung.sitanihut.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.forgot_password.ForgotPasswordRoute
import com.dishut_lampung.sitanihut.presentation.login.LoginRoute

fun NavGraphBuilder.authNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.Auth.Login.route, route = "auth") {
        composable(route = Screen.Auth.Login.route) {
            LoginRoute(navController = navController)
        }
        composable(route = Screen.Auth.ForgotPassword.route){
            ForgotPasswordRoute(navController = navController)
        }
    }
}