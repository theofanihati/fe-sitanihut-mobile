package com.dishut_lampung.sitanihut.presentation.navigation

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
import com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.PengajuanLaporanRoute
import com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.PengajuanLaporanScreen
import com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.create.AddReportRoute
import com.dishut_lampung.sitanihut.presentation.profile.petani.PetaniProfileRoute
import com.dishut_lampung.sitanihut.presentation.profile.petani.PetaniProfileScreen

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
            PetaniProfileRoute()
        }
        composable(route = Screen.DataCommodity.route){
            CommodityRoute()
        }
        composable(route = Screen.Petani.ReportSubmission.route){
            PengajuanLaporanRoute(
                onNavigateToAddReport = {
                    navController.navigateSingleTop(
                        Screen.Petani.ReportForm.createRoute(reportId = null))
                },
                onNavigateToDetail = {
                    navController.navigateSingleTop("report-submission-detail/$id")
                },
                onNavigateToEdit = { id ->
                    navController.navigateSingleTop(
                        Screen.Petani.ReportForm.createRoute(reportId = id)
                    )
                },
            )
        }
        composable(
            route = Screen.Petani.ReportForm.route,
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ){
            AddReportRoute(
                onNavigateUp = { navController.popBackStack() }
            )
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