package com.blackpirateapps.urlshortener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.blackpirateapps.urlshortener.ui.navigation.AppNavigation
import com.blackpirateapps.urlshortener.ui.theme.BlackPirateUrlShortenerTheme
import com.blackpirateapps.urlshortener.viewmodel.UrlViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: UrlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            BlackPirateUrlShortenerTheme(darkTheme = uiState.darkMode) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
