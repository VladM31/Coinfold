package com.vm.coinfold.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vm.coinfold.app.config.BACKUP_ROUTE
import com.vm.coinfold.app.config.RECURRING_ROUTE
import com.vm.coinfold.app.config.Route
import com.vm.coinfold.app.feature.accounts.ui.screens.AccountsScreen
import com.vm.coinfold.app.feature.backup.ui.screens.BackupScreen
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.expenses.domain.usecases.SeedDefaultCategoriesUseCase
import com.vm.coinfold.app.feature.expenses.ui.screens.ExpensesScreen
import com.vm.coinfold.app.feature.overview.ui.screens.OverviewScreen
import com.vm.coinfold.app.feature.recurring.domain.usecases.ProcessRecurringPaymentsUseCase
import com.vm.coinfold.app.feature.recurring.ui.screens.RecurringScreen
import com.vm.coinfold.app.feature.security.domain.models.LockStatus
import com.vm.coinfold.app.feature.security.domain.services.LockController
import com.vm.coinfold.app.feature.security.ui.components.PrivacyCover
import com.vm.coinfold.app.feature.security.ui.screens.LockScreen
import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.models.ThemeMode
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.feature.settings.ui.screens.SettingsScreen
import com.vm.coinfold.app.feature.transactions.ui.screens.TransactionsScreen
import com.vm.coinfold.app.shared.domain.models.ResolvedLanguage
import com.vm.coinfold.app.shared.launch.LaunchAction
import com.vm.coinfold.app.shared.launch.LaunchActions
import com.vm.coinfold.app.shared.platform.HomeWidgets
import com.vm.coinfold.app.shared.platform.SecureWindowEffect
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.components.NavIcon
import com.vm.coinfold.app.shared.ui.locale.LocalAppLocale
import com.vm.coinfold.app.shared.ui.theme.CoinfoldTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun App() {
    // Refresh exchange rates on start; the repository throttles to once per 5 minutes.
    val currencyRepository = koinInject<CurrencyRepository>()
    LaunchedEffect(Unit) { currencyRepository.refresh() }
    // First launch only: create the starter categories (the use case guards itself with a flag).
    val seedCategories = koinInject<SeedDefaultCategoriesUseCase>()
    // Create the transactions of scheduled payments that are due, now and every time the app comes to the front.
    val processRecurring = koinInject<ProcessRecurringPaymentsUseCase>()
    val scope = rememberCoroutineScope()
    // App lock: remember when the app left the foreground and lock again after a while away.
    val lock = koinInject<LockController>()
    val lockStatus by lock.status.collectAsState()
    val isProtected by lock.isProtected.collectAsState()
    var covered by remember { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        lock.onBackground()
        // numbers on the home screen widget should match what the user just saw
        HomeWidgets.refresh()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { covered = true }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { covered = false }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        lock.onForeground()
        scope.launch { processRecurring() }
    }
    // Hide the content in the recent-apps list (and from screenshots) while a PIN protects the app.
    SecureWindowEffect(enabled = isProtected)

    // Wait for the stored settings so the app does not flash the default theme/language.
    val settings = koinInject<SettingsRepository>().settings.collectAsState(initial = null).value ?: return
    // Also wait for the lock decision, so a protected app never shows its content for a moment.
    if (lockStatus == LockStatus.LOADING) return

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

    // Runs after the locale is applied, so the names match the language the user sees.
    LaunchedEffect(Unit) {
        seedCategories()
        processRecurring()
    }

    CompositionLocalProvider(
        LocalAppLocale provides localeTag,
        LocalAppLanguage provides resolvedLanguage,
    ) {
        CoinfoldTheme(darkTheme = darkTheme) {
            // The controller lives outside the key, so switching language keeps the current tab.
            val navController = rememberNavController()
            // Shortcuts and the widget start on the tab that handles them (the screen then consumes the request).
            val launchAction by LaunchActions.pending.collectAsState()
            LaunchedEffect(launchAction) {
                val route = when (launchAction) {
                    LaunchAction.ADD_EXPENSE -> Route.Expenses
                    LaunchAction.ADD_INCOME -> Route.Accounts
                    null -> null
                }
                if (route != null) {
                    navController.navigate(route.path) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            Box(Modifier.fillMaxSize()) {
                key(localeTag) { AppScaffold(navController) }
                // The lock screen is drawn over the app, so the screen the user was on is still there afterwards.
                if (lockStatus == LockStatus.LOCKED) LockScreen()
                if (covered && isProtected) PrivacyCover()
            }
        }
    }
}

@Composable
private fun AppScaffold(navController: androidx.navigation.NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route?.let { if (it == RECURRING_ROUTE || it == BACKUP_ROUTE) Route.Settings.path else it }

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
                        label = { Text(stringResource(route.label), maxLines = 1, softWrap = false) },
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
                        Route.Overview -> OverviewScreen()
                        Route.Transactions -> TransactionsScreen()
                        Route.Settings -> SettingsScreen(
                            onOpenRecurring = { navController.navigate(RECURRING_ROUTE) },
                            onOpenBackup = { navController.navigate(BACKUP_ROUTE) },
                        )
                    }
                }
            }
            composable(RECURRING_ROUTE) { RecurringScreen(onBack = { navController.popBackStack() }) }
            composable(BACKUP_ROUTE) { BackupScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
