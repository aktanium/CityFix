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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.cityfix.presentation.screens.add_report.AddReportScreen
import com.cityfix.presentation.screens.profile.ProfileScreen
import com.cityfix.presentation.screens.report_detail.ReportDetailScreen
import com.cityfix.presentation.screens.report_list.ReportListScreen
import com.cityfix.presentation.screens.settings.SettingsScreen
import com.cityfix.presentation.screens.splash.SplashScreen

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
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List
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
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    val bottomBarRoutes = setOf(
        NavRoutes.REPORT_LIST,
        NavRoutes.PROFILE,
        NavRoutes.SETTINGS
    )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
        return
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CityFixBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.REPORT_LIST,
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
                    navArgument(NavArgs.REPORT_ID) { type = NavType.LongType }
                )
            ) {
                ReportDetailScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }

            composable(NavRoutes.PROFILE) {
                ProfileScreen()
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
