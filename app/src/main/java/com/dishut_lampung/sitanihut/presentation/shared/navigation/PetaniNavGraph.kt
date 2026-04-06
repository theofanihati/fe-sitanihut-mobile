package com.dishut_lampung.sitanihut.presentation.shared.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.commodity.CommodityRoute
import com.dishut_lampung.sitanihut.presentation.home_page.petani.HomePagePetaniRoute
import com.dishut_lampung.sitanihut.presentation.information.InformationRoute
import com.dishut_lampung.sitanihut.presentation.information.about_app.AboutScreen
import com.dishut_lampung.sitanihut.presentation.information.contact.ContactScreen
import com.dishut_lampung.sitanihut.presentation.information.about_company.DishutRoute
import com.dishut_lampung.sitanihut.presentation.profile.petani.PetaniProfileRoute
import com.dishut_lampung.sitanihut.presentation.report.detail.ReportDetailRoute
import com.dishut_lampung.sitanihut.presentation.report.form.ReportFormRoute
import com.dishut_lampung.sitanihut.presentation.report.list.ReportListRoute

fun NavGraphBuilder.petaniNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.HomePetani.route, route = "petani") {
        composable(route = Screen.HomePetani.route) {
            val onNavigateToDetail = { id : String ->
                navController.navigateSingleTop("report_detail/$id")
            }
            val onNavigateToCommodity = {
                navController.navigateSingleTop("data_commodity")
            }
            val onNavigateToReportSubmission = {
                navController.navigateSingleTop("report_list")
            }
            val onNavigateInfo = {
                navController.navigateSingleTop("information")
            }
            val onNavigateToEdit = { id: String ->
                navController.navigateSingleTop(
                    Screen.ReportForm.createRoute(reportId = id)
                )
            }
            val onNavigateToSettings = {
                navController.navigateToMenu("settings")
            }

            HomePagePetaniRoute(
                modifier = modifier,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToCommodity = onNavigateToCommodity,
                onNavigateToReportSubmission = onNavigateToReportSubmission,
                onNavigateToInfo = onNavigateInfo,
                onNavigateToEdit = onNavigateToEdit,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        composable(route = Screen.Petani.ProfilePetani.route){
            PetaniProfileRoute()
        }
        composable(route = Screen.DataCommodity.route){
            CommodityRoute()
        }
        composable(route = Screen.ReportList.route){
            ReportListRoute(
                onNavigateToAddReport = {
                    navController.navigateSingleTop(
                        Screen.ReportForm.createRoute(reportId = null)
                    )
                },
                onNavigateToDetail = { id ->
                    navController.navigateSingleTop("report_detail/$id")
                },
                onNavigateToEdit = { id ->
                    navController.navigateSingleTop(
                        Screen.ReportForm.createRoute(reportId = id)
                    )
                },
            )
        }

        composable(
            route = Screen.ReportForm.route,
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ){
            ReportFormRoute(
                onNavigateUp = { navController.popBackStack() }
            )
        }

        composable(
            route = "report_detail/{reportId}",
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.StringType
                }
            )
        ){
            ReportDetailRoute(navController)
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