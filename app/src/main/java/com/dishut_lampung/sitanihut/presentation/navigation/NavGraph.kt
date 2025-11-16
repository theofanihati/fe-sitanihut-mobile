package com.dishut_lampung.sitanihut.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dishut_lampung.sitanihut.presentation.SplashScreen

@Composable
fun NavGraph(
    navController : NavHostController,
    modifier : Modifier,
    startDestination : String = Screen.SplashScreen.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash-screen") {
            SplashScreen(
                onNavigateToMain = { navController.navigate("home") }
            )
        }
//        composable(
//            route = Screen.Home.route,
////      ! Uncomment kalo dibutuhin
////      enterTransition = {
////        slideInHorizontally(
////          initialOffsetX = { it },
////          animationSpec = tween(700)
////        )
////      }
//        ) {
//            LandingPageScreen(
//                modifier = modifier,
//            )
//        }

        authNavGraph(
            modifier = modifier,
            navController = navController
        )
    }
}