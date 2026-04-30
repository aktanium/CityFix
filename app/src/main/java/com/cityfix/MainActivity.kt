package com.cityfix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cityfix.domain.usecase.GetAppSettingsUseCase
import com.cityfix.presentation.navigation.CityFixNavHost
import com.cityfix.presentation.theme.CityFixTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var getAppSettingsUseCase: GetAppSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by getAppSettingsUseCase().collectAsStateWithLifecycle(null)
            val isDarkTheme = settings?.isDarkMode ?: isSystemInDarkTheme()

            CityFixTheme(darkTheme = isDarkTheme) {
                CityFixNavHost()
            }
        }
    }
}
