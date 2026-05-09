package com.cityfix.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.cityfix.presentation.screens.add_report.AddReportScreen
import com.cityfix.presentation.screens.auth.AuthViewModel
import com.cityfix.presentation.screens.auth.LoginScreen
import com.cityfix.presentation.screens.auth.RegisterScreen
import com.cityfix.presentation.screens.profile.ProfileScreen
import com.cityfix.presentation.screens.report_detail.ReportDetailScreen
import com.cityfix.presentation.screens.report_list.ReportListScreen
import com.cityfix.presentation.screens.settings.SettingsScreen
import com.cityfix.presentation.screens.splash.SplashScreen
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List

private data class BottomNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavDestinations = listOf(
    BottomNavDestination(
        route = NavRoutes.REPORT_LIST,
        label = "Reports",
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List
    ),
    BottomNavDestination(
        route = NavRoutes.PROFILE,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    ),
    BottomNavDestination(
        route = NavRoutes.SETTINGS,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

@Composable
fun CityFixNavHost() {
    var showSplash by remember { mutableStateOf(true) }

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
        return
    }

    val navController = rememberNavController()
    val startDestination = if (authViewModel.isLoggedIn()) NavRoutes.REPORT_LIST else NavRoutes.LOGIN

    LaunchedEffect(authState.isLoggedIn) {
        val currentRoute = navController.currentDestination?.route
        // Don't auto-navigate from Register: register completion has its own flow
        // (sign out + send the user to Login with a confirmation message).
        if (currentRoute == NavRoutes.REGISTER) return@LaunchedEffect

        if (authState.isLoggedIn) {
            navController.navigate(NavRoutes.REPORT_LIST) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else if (currentRoute != NavRoutes.LOGIN) {
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val bottomBarRoutes = setOf(
        NavRoutes.REPORT_LIST,
        NavRoutes.PROFILE,
        NavRoutes.SETTINGS
    )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes && authState.isLoggedIn

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CityFixBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(NavRoutes.LOGIN) {
                LoginScreen(
                    uiState = authState,
                    onEvent = authViewModel::onEvent,
                    onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) }
                )
            }
            composable(NavRoutes.REGISTER) {
                RegisterScreen(
                    uiState = authState,
                    onEvent = authViewModel::onEvent,
                    onNavigateBack = { navController.navigateUp() },
                    onRegistrationComplete = {
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(NavRoutes.REGISTER) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(NavRoutes.REPORT_LIST) {
                ReportListScreen(
                    onReportClick = { reportId ->
                        navController.navigate(NavRoutes.reportDetail(reportId))
                    },
                    onAddReport = {
                        navController.navigate(NavRoutes.ADD_REPORT)
                    }
                )
            }

            composable(NavRoutes.ADD_REPORT) {
                AddReportScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }

            composable(
                route = NavRoutes.REPORT_DETAIL,
                arguments = listOf(
                    navArgument(NavArgs.REPORT_ID) { type = NavType.StringType }
                )
            ) {
                ReportDetailScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }

            composable(NavRoutes.PROFILE) {
                ProfileScreen(navController = navController, authViewModel = authViewModel)
            }

            composable(NavRoutes.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun CityFixBottomBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavDestinations.forEach { destination ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == destination.route
            } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}
