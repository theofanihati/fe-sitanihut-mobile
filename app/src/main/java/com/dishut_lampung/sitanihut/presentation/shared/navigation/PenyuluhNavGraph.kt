package com.dishut_lampung.sitanihut.presentation.shared.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.dishut_lampung.sitanihut.presentation.commodity.CommodityRoute
import com.dishut_lampung.sitanihut.presentation.home_page.penyuluh.HomePagePenyuluhRoute
import com.dishut_lampung.sitanihut.presentation.information.InformationRoute
import com.dishut_lampung.sitanihut.presentation.information.about_app.AboutScreen
import com.dishut_lampung.sitanihut.presentation.information.contact.ContactScreen
import com.dishut_lampung.sitanihut.presentation.information.about_company.DishutRoute
import com.dishut_lampung.sitanihut.presentation.kth.detail.KthDetailRoute
import com.dishut_lampung.sitanihut.presentation.kth.form.KthFormRoute
import com.dishut_lampung.sitanihut.presentation.kth.list.KthListRoute
import com.dishut_lampung.sitanihut.presentation.petani.detail.PetaniDetailRoute
import com.dishut_lampung.sitanihut.presentation.petani.form.PetaniFormRoute
import com.dishut_lampung.sitanihut.presentation.petani.list.PetaniListRoute
import com.dishut_lampung.sitanihut.presentation.profile.penyuluh.PenyuluhProfileRoute
import com.dishut_lampung.sitanihut.presentation.user_management.list.UserListRoute

fun NavGraphBuilder.penyuluhNavGraph(
    modifier : Modifier,
    navController : NavHostController
) {
    navigation(startDestination = Screen.HomePenyuluh.route, route = "penyuluh") {
        composable(route = Screen.HomePenyuluh.route) {
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
            val onNavigateToReportVerification = {
                navController.navigate("report_list")
            }
            val onNavigateToUserManagement = {
                navController.navigate("user-management")
            }
            val onNavigateToInfo = {
                navController.navigate("information")
            }
            HomePagePenyuluhRoute(
                modifier = Modifier,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToCommodity = onNavigateToCommodity,
                onNavigateToKTH = onNavigateToKTH,
                onNavigateToPetani = onNavigateToPetani,
                onNavigateToReportVerification = onNavigateToReportVerification,
                onNavigateToUserManagement = onNavigateToUserManagement,
                onNavigateToInfo = onNavigateToInfo
                )
        }
        composable(route = Screen.Penyuluh.ProfilePenyuluh.route){
            PenyuluhProfileRoute()
        }
        composable(route = Screen.DataCommodity.route){
            CommodityRoute()
        }
        // KTH
        composable(route = Screen.DataKth.route){
            KthListRoute(
                onNavigateToAddKth = {
                    navController.navigateSingleTop(
                        Screen.KthForm.createRoute(id = null)
                    )
                },
                onNavigateToDetail = { id : String ->
                    navController.navigateSingleTop("kth_detail/$id")
                },
                onNavigateToEdit = { id : String ->
                    navController.navigateSingleTop(
                        Screen.KthForm.createRoute(id = id)
                    )
                }
            )
        }
        composable(
            route = Screen.KthForm.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ){
            KthFormRoute(navController = navController)
        }
        composable(
            route = "kth_detail/{kthId}",
            arguments = listOf(
                navArgument("kthId") {
                    type = NavType.StringType
                }
            )
        ){
            KthDetailRoute()
        }

        // MASTER PETANI
        composable(route = Screen.DataPetani.route){
            PetaniListRoute(
                onNavigateToAddPetani = {
                    navController.navigateSingleTop(
                        Screen.PetaniForm.createRoute(id = null)
                    )
                },
                onNavigateToDetail = { id : String ->
                    navController.navigateSingleTop("petani_detail/$id")
                },
                onNavigateToEdit = { id : String ->
                    navController.navigateSingleTop(
                        Screen.PetaniForm.createRoute(id = id)
                    )
                }
            )
        }
        composable(
            route = Screen.PetaniForm.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ){
            PetaniFormRoute(navController = navController)
        }
        composable(
            route = "petani_detail/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ){
            PetaniDetailRoute()
        }

        // USER MANAGEMENT
        composable(route = Screen.UserManagement.route){
            UserListRoute(
                onNavigateToAddUser = {
                    navController.navigateSingleTop(
                        Screen.KthForm.createRoute(id = null)
                    )
                },
                onNavigateToDetail = { id : String ->
                    navController.navigateSingleTop("user_detail/$id")
                },
                onNavigateToEdit = { id : String ->
                    navController.navigateSingleTop(
                        Screen.UserForm.createRoute(id = id)
                    )
                }
            )
        }

        // INFORMATION
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