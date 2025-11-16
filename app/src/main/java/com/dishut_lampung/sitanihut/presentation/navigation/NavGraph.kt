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
    startDestination : String = "auth"
) {
    NavHost(navController = navController, startDestination = startDestination) {
//                composable(
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
//        petaniNavGraph(
//            modifier = modifier,
//            navController = navController
//        )
//        penyuluhNavGraph(
//            modifier = modifier,
//            navController = navController
//        )
//        kkphNavGraph(
//            modifier = modifier,
//            navController = navController
//        )
    }
}