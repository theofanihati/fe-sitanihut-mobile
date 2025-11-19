package com.dishut_lampung.sitanihut.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dishut_lampung.sitanihut.presentation.components.topbar.DetailTopBar
import com.dishut_lampung.sitanihut.presentation.components.topbar.HomeTopBar
import com.dishut_lampung.sitanihut.presentation.navigation.NavGraph
import com.dishut_lampung.sitanihut.presentation.scaffold.TopBarTheme
import com.dishut_lampung.sitanihut.presentation.scaffold.scaffoldConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    startDestination: String,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val config = scaffoldConfig(currentRoute)
    val userProfile by viewModel.userProfileState.collectAsState()

    val lightTopBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
    )

    val darkTopBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        titleContentColor = Color.White,
        navigationIconContentColor = Color.White
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            val currentTopBarColors = if (config.topBarTheme == TopBarTheme.DARK_BACKGROUND) {
                darkTopBarColors
            } else {
                lightTopBarColors
            }

            when {
                config.showMainNav -> {
                    HomeTopBar(
                        nama = userProfile.name,
                        role = userProfile.role,
                        imageUrl = userProfile.imageUrl,
                        onLogoutClick = {
                            viewModel.logout()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                config.showBackNav -> {
                    DetailTopBar(
                        onBackClick = { navController.popBackStack() },
                        colors = currentTopBarColors
                    )
                }
                 else -> {}
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            modifier = Modifier
                .padding(bottom = paddingValues.calculateBottomPadding()),
            startDestination = startDestination,
        )
    }
}