package com.dishut_lampung.sitanihut.presentation.shared.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder


fun NavController.navigateSingleTop(route : String) {
    navigate(route) {
        popUpTo(route) { inclusive = true }
        launchSingleTop = true
        restoreState = true
    }
}


fun NavController.navigateAndClearStack(route : String) {
    this.navigate(route) {
        popUpTo(graph.startDestinationId) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

fun NavController.navigateWithClearUntil(route : String, clearUntilRoute : String) {
    this.navigate(route) {
        popUpTo(clearUntilRoute) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

fun NavController.navigateWithAnimation(
    route : String,
    builder : NavOptionsBuilder.() -> Unit = {}
) {
    this.navigate(route) {
        launchSingleTop = true
        builder()
    }
}


//fun NavController.navigateAndClearStackButHome(
//    route : String,
//) {
//    navigate(route) {
//        popUpTo(Screen.Home.route) { inclusive = false }
//        launchSingleTop = true
//    }
//}
