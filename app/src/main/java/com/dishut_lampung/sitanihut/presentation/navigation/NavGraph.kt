package com.dishut_lampung.sitanihut.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dishut_lampung.sitanihut.presentation.home_page.kkph.HomePageKkphScreen
import com.dishut_lampung.sitanihut.presentation.home_page.penyuluh.HomePagePenyuluhScreen
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
            HomePagePetaniScreen(
//                modifier = modifier,
            )
        }
        composable(route = Screen.HomePenyuluh.route) {
            HomePagePenyuluhScreen(
                modifier = modifier,
            )
        }
        composable(route = Screen.HomeKkph.route) {
            HomePageKkphScreen(
                modifier = modifier,
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