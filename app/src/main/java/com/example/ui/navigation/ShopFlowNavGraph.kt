package com.example.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.data.repository.ShopFlowRepository
import com.example.ui.components.ShopFlowBottomBar
import com.example.ui.screens.customers.CustomersScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.hub.BusinessHubScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.pos.PosTerminalScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.sales.SalesHistoryScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.suppliers.SuppliersScreen

/**
 * Convenient navigation helper extensions for switching between core application views.
 */
fun NavHostController.navigateToDashboard() {
    navigate(Screen.Dashboard.route) {
        popUpTo(Screen.Dashboard.route) {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.navigateToInventory() {
    navigate(Screen.Inventory.route) {
        popUpTo(Screen.Dashboard.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.navigateToAnalytics() {
    navigate(Screen.Analytics.route) {
        popUpTo(Screen.Dashboard.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Primary Navigation Graph using Navigation Compose library to handle switching
 * between the dashboard, inventory list, and analytics views, as well as secondary POS and hub routes.
 */
@Composable
fun ShopFlowNavGraph(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    repository: ShopFlowRepository,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Onboarding flow
        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            OnboardingScreen(
                onComplete = { lang, storeName, category ->
                    dashboardViewModel.completeOnboarding(lang, storeName, category)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 1. Dashboard View
        composable(
            route = Screen.Dashboard.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                repository = repository,
                onNavigateToPos = {
                    navController.navigate(Screen.Pos.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToInventory = {
                    navController.navigateToInventory()
                },
                onNavigateToAnalytics = {
                    navController.navigateToAnalytics()
                },
                onNavigateToReports = {
                    navController.navigateToAnalytics()
                },
                contentPadding = innerPadding
            )
        }

        // 2. Inventory List View
        composable(
            route = Screen.Inventory.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            InventoryScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = { navController.navigateToDashboard() },
                onNavigateToAnalytics = { navController.navigateToAnalytics() },
                contentPadding = innerPadding
            )
        }

        // 3. Analytics View
        composable(
            route = Screen.Analytics.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            ReportsScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = { navController.navigateToDashboard() },
                onNavigateToInventory = { navController.navigateToInventory() },
                contentPadding = innerPadding
            )
        }

        // Backward-compatible route for Reports alias
        composable(
            route = Screen.Reports.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            ReportsScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = { navController.navigateToDashboard() },
                onNavigateToInventory = { navController.navigateToInventory() },
                contentPadding = innerPadding
            )
        }

        // POS Terminal
        composable(
            route = Screen.Pos.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            PosTerminalScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { navController.popBackStack() },
                contentPadding = innerPadding
            )
        }

        // Business Hub
        composable(
            route = Screen.Hub.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            BusinessHubScreen(
                viewModel = dashboardViewModel,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                contentPadding = innerPadding
            )
        }

        // Customers
        composable(
            route = Screen.Customers.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            CustomersScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { navController.popBackStack() },
                contentPadding = innerPadding
            )
        }

        // Suppliers
        composable(
            route = Screen.Suppliers.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            SuppliersScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { navController.popBackStack() },
                contentPadding = innerPadding
            )
        }

        // Expenses
        composable(
            route = Screen.Expenses.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            ExpensesScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { navController.popBackStack() },
                contentPadding = innerPadding
            )
        }

        // Sales History
        composable(
            route = Screen.Sales.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            SalesHistoryScreen(
                viewModel = dashboardViewModel,
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                contentPadding = innerPadding
            )
        }

        // Settings
        composable(
            route = Screen.Settings.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            SettingsScreen(
                repository = repository,
                viewModel = dashboardViewModel,
                onNavigateBack = { navController.popBackStack() },
                contentPadding = innerPadding
            )
        }
    }
}

/**
 * Alias for ShopFlowNavGraph matching KarobarFlow / Smart Shop domain terminology.
 */
@Composable
fun KarobarNavGraph(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    repository: ShopFlowRepository,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Dashboard.route
) = ShopFlowNavGraph(
    navController = navController,
    dashboardViewModel = dashboardViewModel,
    repository = repository,
    innerPadding = innerPadding,
    modifier = modifier,
    startDestination = startDestination
)

/**
 * Root Application Composable hosting scaffold, bottom navigation, and the navigation graph.
 */
@Composable
fun ShopFlowApp(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    repository: ShopFlowRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val isUrdu = uiState.settings.language == "ur"
    val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val showBottomBar = currentRoute != Screen.Onboarding.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    ShopFlowBottomBar(
                        currentRoute = currentRoute,
                        language = uiState.settings.language,
                        onNavigate = { screen ->
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            ShopFlowNavGraph(
                navController = navController,
                dashboardViewModel = dashboardViewModel,
                repository = repository,
                innerPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
