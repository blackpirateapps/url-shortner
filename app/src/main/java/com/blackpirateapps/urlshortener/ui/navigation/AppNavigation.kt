package com.blackpirateapps.urlshortener.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.blackpirateapps.urlshortener.ui.components.CupertinoBottomNavBar
import com.blackpirateapps.urlshortener.ui.components.TabItem
import com.blackpirateapps.urlshortener.ui.screens.HistoryScreen
import com.blackpirateapps.urlshortener.ui.screens.HomeScreen
import com.blackpirateapps.urlshortener.ui.screens.LinkDetailsScreen
import com.blackpirateapps.urlshortener.ui.screens.SettingsScreen
import com.blackpirateapps.urlshortener.viewmodel.UrlViewModel

@Composable
fun AppNavigation(
    viewModel: UrlViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val tabs = remember {
        listOf(
            TabItem("Home", Icons.Outlined.Home, "home"),
            TabItem("History", Icons.Outlined.History, "history"),
            TabItem("Settings", Icons.Outlined.Settings, "settings")
        )
    }

    var selectedRoute by remember { mutableStateOf("home") }

    // Show link details screen as an overlay when a link is selected
    if (uiState.selectedLink != null) {
        BackHandler { viewModel.clearSelectedLink() }
        LinkDetailsScreen(
            link = uiState.selectedLink!!,
            viewModel = viewModel,
            onBack = { viewModel.clearSelectedLink() }
        )
        return
    }

    Scaffold(
        bottomBar = {
            CupertinoBottomNavBar(
                tabs = tabs,
                selectedRoute = selectedRoute,
                onTabSelected = { route -> selectedRoute = route }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedRoute) {
                "home" -> HomeScreen(viewModel = viewModel)
                "history" -> HistoryScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
