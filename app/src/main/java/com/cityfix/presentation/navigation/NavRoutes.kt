package com.cityfix.presentation.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val REPORT_LIST = "report_list"
    const val ADD_REPORT = "add_report"
    const val REPORT_DETAIL = "report_detail/{${NavArgs.REPORT_ID}}"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    fun reportDetail(reportId: Long) = "report_detail/$reportId"
}

object NavArgs {
    const val REPORT_ID = "reportId"
}

enum class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: String
) {
    REPORTS(NavRoutes.REPORT_LIST, "Reports", "list"),
    PROFILE(NavRoutes.PROFILE, "Profile", "person"),
    SETTINGS(NavRoutes.SETTINGS, "Settings", "settings")
}
