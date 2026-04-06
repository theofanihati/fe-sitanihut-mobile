package com.dishut_lampung.sitanihut.presentation.shared.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.commodity.CommodityRoute
import com.dishut_lampung.sitanihut.presentation.home_page.kkph.HomePagePenanggungJawabRoute
import com.dishut_lampung.sitanihut.presentation.information.InformationRoute
import com.dishut_lampung.sitanihut.presentation.information.about_app.AboutScreen
import com.dishut_lampung.sitanihut.presentation.information.contact.ContactScreen
import com.dishut_lampung.sitanihut.presentation.information.about_company.DishutRoute
import com.dishut_lampung.sitanihut.presentation.penyuluh.detail.PenyuluhDetailRoute
import com.dishut_lampung.sitanihut.presentation.penyuluh.list.PenyuluhRoute
import com.dishut_lampung.sitanihut.presentation.profile.kkph.KkphProfileRoute
import com.dishut_lampung.sitanihut.presentation.report.detail.ReportDetailRoute

fun NavGraphBuilder.kkphNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.HomeKkph.route, route = "penanggung-jawab") {
        composable(route = Screen.HomeKkph.route) {
            val onNavigateToDetail = { id : String ->
                navController.navigateSingleTop("report_detail/$id")
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
                navController.navigate("report_list")
            }
            val onNavigateToUserManagement = {
                navController.navigate("user-management")
            }
            val onNavigateToInfo = {
                navController.navigate("information")
            }
            val onNavigateToSettings = {
                navController.navigateToMenu("settings")
            }
            HomePagePenanggungJawabRoute(
                modifier = Modifier,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToCommodity = onNavigateToCommodity,
                onNavigateToKTH = onNavigateToKTH,
                onNavigateToPetani = onNavigateToPetani,
                onNavigateToPenyuluh = onNavigateToPenyuluh,
                onNavigateToReportVerification = onNavigateToReportVerification,
                onNavigateToUserManagement = onNavigateToUserManagement,
                onNavigateToInfo = onNavigateToInfo,
                onNavigateToSettings = onNavigateToSettings
            )
        }
        composable(route = Screen.PenanggungJawab.ProfilePenanggungJawab.route){
            KkphProfileRoute ()
        }
        composable(route = Screen.DataCommodity.route){
            CommodityRoute()
        }
        composable(route = Screen.PenanggungJawab.DataPenyuluh.route){
            PenyuluhRoute(
                onNavigateToDetail = { id : String -> navController.navigateSingleTop("penyuluh_detail/$id") },
                )
        }
        composable(
            route = "penyuluh_detail/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ){
            PenyuluhDetailRoute()
        }
        composable(route = Screen.Information.route){
            InformationRoute(
                navController = navController,
                onNavigateToAboutApp = {
                    navController.navigate("information/about-app")
                },
                onNavigateToAboutCompany = {
                    navController.navigate("information/about-company")
                },
                onNavigateToContact = {
                    navController.navigate("information/contact")
                }
            )
        }
        composable(route = Screen.About.route){
            AboutScreen()
        }
        composable(route = Screen.Contact.route){
            ContactScreen()
        }
        composable(route = Screen.Dishut.route){
            DishutRoute(
                navController = navController
            )
        }
    }
}