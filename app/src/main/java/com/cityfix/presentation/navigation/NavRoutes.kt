package com.cityfix.presentation.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val REPORT_LIST = "report_list"
    const val ADD_REPORT = "add_report"
    const val REPORT_DETAIL = "report_detail/{${NavArgs.REPORT_ID}}"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val STATS = "stats"

    fun reportDetail(reportId: String) = "report_detail/$reportId"
}

object NavArgs {
    const val REPORT_ID = "reportId"
}
