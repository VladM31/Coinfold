package com.vm.coinfold.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vm.coinfold.app.config.Route
import com.vm.coinfold.app.feature.currency.main.CurrencyRepository
import com.vm.coinfold.app.feature.settings.main.Settings
import com.vm.coinfold.app.feature.settings.main.SettingsRepository
import com.vm.coinfold.app.feature.settings.main.ThemeMode
import com.vm.coinfold.app.shared.ui.ComingSoon
import com.vm.coinfold.app.shared.ui.theme.CoinfoldTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun App() {
    // Refresh exchange rates on start; the repository throttles to once per 5 minutes.
    val currencyRepository = koinInject<CurrencyRepository>()
    LaunchedEffect(Unit) { currencyRepository.refresh() }

    val settings by koinInject<SettingsRepository>().settings.collectAsState(initial = Settings())
    val darkTheme = when (settings.theme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CoinfoldTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val current = backStack?.destination?.route

        Scaffold(
            bottomBar = {
                NavigationBar {
                    Route.entries.forEach { route ->
                        NavigationBarItem(
                            selected = current == route.path,
                            onClick = {
                                navController.navigate(route.path) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(route.glyph) },
                            label = { Text(stringResource(route.label)) },
                        )
                    }
                }
            },
        ) { padding ->
            NavHost(navController, startDestination = Route.Expenses.path, modifier = Modifier.padding(padding)) {
                Route.entries.forEach { route ->
                    composable(route.path) { ComingSoon(stringResource(route.label)) }
                }
            }
        }
    }
}
