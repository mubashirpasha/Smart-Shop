package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Pos : Screen("pos")
    object Inventory : Screen("inventory")
    object Analytics : Screen("analytics")
    object Reports : Screen("reports")
    object Hub : Screen("hub")
    object Customers : Screen("customers")
    object Suppliers : Screen("suppliers")
    object Expenses : Screen("expenses")
    object Sales : Screen("sales")
    object Settings : Screen("settings")
}

