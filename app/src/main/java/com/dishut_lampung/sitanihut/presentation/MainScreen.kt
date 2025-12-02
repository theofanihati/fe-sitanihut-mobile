package com.dishut_lampung.sitanihut.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dishut_lampung.sitanihut.presentation.components.dialog.CustomConfirmationDialog
import com.dishut_lampung.sitanihut.presentation.components.topbar.DetailTopBar
import com.dishut_lampung.sitanihut.presentation.components.topbar.HomeTopBar
import com.dishut_lampung.sitanihut.presentation.navigation.NavGraph
import com.dishut_lampung.sitanihut.presentation.navigation.Screen
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
    var showLogoutDialog by remember { mutableStateOf(false) }

    val lightTopBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
    )

    val darkTopBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        titleContentColor = Color.White,
        navigationIconContentColor = Color.White
    )

    if (showLogoutDialog) {
        CustomConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
                navController.navigate("auth") {
                    popUpTo(0) { inclusive = true }
                }
            },
            title = "Keluar dari akun?",
            supportingText = "Aksi tidak dapat dikembalikan",
            confirmButtonText = "Keluar",
            dismissButtonText = "Batal",
            confirmColor = MaterialTheme.colorScheme.error
        )
    }

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
                            showLogoutDialog = true
                        },
                        onProfileClick = { role ->
                            val destination = when (role.lowercase()) {
                                "petani" -> Screen.Petani.ProfilePetani.route
                                "penyuluh" -> Screen.Penyuluh.ProfilePenyuluh.route
                                "penanggung jawab" -> Screen.PenanggungJawab.ProfilePenanggungJawab.route
                                else -> null
                            }
                            if (destination != null) {
                                navController.navigate(destination)
                            } else {
                                android.util.Log.e("Navigation", "Role tidak dikenali: $role")
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