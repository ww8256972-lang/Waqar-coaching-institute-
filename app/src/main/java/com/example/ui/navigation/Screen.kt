package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        filledIcon = Icons.Filled.Dashboard,
        outlinedIcon = Icons.Outlined.Dashboard
    )

    object Students : Screen(
        route = "students",
        title = "Students",
        filledIcon = Icons.Filled.People,
        outlinedIcon = Icons.Outlined.People
    )

    object FeeManagement : Screen(
        route = "fee_management",
        title = "Fees",
        filledIcon = Icons.Filled.AccountBalanceWallet,
        outlinedIcon = Icons.Outlined.AccountBalanceWallet
    )

    object Attendance : Screen(
        route = "attendance",
        title = "Attendance",
        filledIcon = Icons.Filled.HowToReg,
        outlinedIcon = Icons.Outlined.HowToReg
    )

    object Results : Screen(
        route = "results",
        title = "Results",
        filledIcon = Icons.Filled.Grade,
        outlinedIcon = Icons.Outlined.Grade
    )

    object Notifications : Screen(
        route = "notifications",
        title = "Alerts",
        filledIcon = Icons.Filled.Notifications,
        outlinedIcon = Icons.Outlined.Notifications
    )

    object Reports : Screen(
        route = "reports",
        title = "Reports",
        filledIcon = Icons.Filled.Assessment,
        outlinedIcon = Icons.Outlined.Assessment
    )

    object Admin : Screen(
        route = "admin",
        title = "Admin",
        filledIcon = Icons.Filled.AdminPanelSettings,
        outlinedIcon = Icons.Outlined.AdminPanelSettings
    )

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Students,
            FeeManagement,
            Attendance,
            Results
        )

        val moreNavItems = listOf(
            Notifications,
            Reports,
            Admin
        )
    }
}
