package com.vm.coinfold.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vm.coinfold.app.config.Route
import com.vm.coinfold.app.feature.accounts.ui.screens.AccountsScreen
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.expenses.ui.screens.ExpensesScreen
import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.models.ThemeMode
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.feature.settings.ui.screens.SettingsScreen
import com.vm.coinfold.app.feature.transactions.ui.screens.TransactionsScreen
import com.vm.coinfold.app.shared.domain.models.ResolvedLanguage
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.components.NavIcon
import com.vm.coinfold.app.shared.ui.locale.LocalAppLocale
import com.vm.coinfold.app.shared.ui.theme.CoinfoldTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun App() {
    // Refresh exchange rates on start; the repository throttles to once per 5 minutes.
    val currencyRepository = koinInject<CurrencyRepository>()
    LaunchedEffect(Unit) { currencyRepository.refresh() }

    // Wait for the stored settings so the app does not flash the default theme/language.
    val settings = koinInject<SettingsRepository>().settings.collectAsState(initial = null).value ?: return

    val darkTheme = when (settings.theme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // null tag = follow the device language (resources fall back to English for unsupported ones).
    val localeTag = when (settings.language) {
        AppLanguage.SYSTEM -> null
        AppLanguage.EN -> "en"
        AppLanguage.UK -> "uk"
    }
    val resolvedLanguage = when (localeTag ?: LocalAppLocale.systemTag.take(2)) {
        "uk" -> ResolvedLanguage.UK
        else -> ResolvedLanguage.EN
    }

    CompositionLocalProvider(
        LocalAppLocale provides localeTag,
        LocalAppLanguage provides resolvedLanguage,
    ) {
        CoinfoldTheme(darkTheme = darkTheme) {
            // The controller lives outside the key, so switching language keeps the current tab.
            val navController = rememberNavController()
            key(localeTag) { AppScaffold(navController) }
        }
    }
}

@Composable
private fun AppScaffold(navController: androidx.navigation.NavHostController) {
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
                        icon = {
                            NavIcon(
                                kind = route.icon,
                                tint = if (current == route.path) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        label = { Text(stringResource(route.label)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = Route.Expenses.path, modifier = Modifier.padding(padding)) {
            Route.entries.forEach { route ->
                composable(route.path) {
                    when (route) {
                        Route.Accounts -> AccountsScreen()
                        Route.Expenses -> ExpensesScreen()
                        Route.Transactions -> TransactionsScreen()
                        Route.Settings -> SettingsScreen()
                    }
                }
            }
        }
    }
}
