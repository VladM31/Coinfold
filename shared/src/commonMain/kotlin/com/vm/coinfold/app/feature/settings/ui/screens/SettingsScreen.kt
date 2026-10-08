package com.vm.coinfold.app.feature.settings.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.biometric_cancel
import coinfold.shared.generated.resources.biometric_subtitle
import coinfold.shared.generated.resources.biometric_title
import coinfold.shared.generated.resources.language_en
import coinfold.shared.generated.resources.language_system
import coinfold.shared.generated.resources.language_uk
import coinfold.shared.generated.resources.settings_backup
import coinfold.shared.generated.resources.settings_backup_hint
import coinfold.shared.generated.resources.settings_language
import coinfold.shared.generated.resources.settings_main_currency
import coinfold.shared.generated.resources.settings_main_currency_hint
import coinfold.shared.generated.resources.settings_period_hint
import coinfold.shared.generated.resources.settings_period_start
import coinfold.shared.generated.resources.settings_rates
import coinfold.shared.generated.resources.settings_recurring
import coinfold.shared.generated.resources.settings_recurring_hint
import coinfold.shared.generated.resources.settings_recurring_open
import coinfold.shared.generated.resources.settings_theme
import coinfold.shared.generated.resources.settings_title
import coinfold.shared.generated.resources.theme_dark
import coinfold.shared.generated.resources.theme_light
import coinfold.shared.generated.resources.theme_system
import com.vm.coinfold.app.feature.security.domain.viewmodels.SecurityIntent
import com.vm.coinfold.app.feature.security.domain.viewmodels.SecurityState
import com.vm.coinfold.app.feature.security.domain.viewmodels.SecurityViewModel
import com.vm.coinfold.app.feature.security.ui.components.PinDialog
import com.vm.coinfold.app.feature.security.ui.components.SecuritySection
import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.models.ThemeMode
import com.vm.coinfold.app.feature.settings.domain.viewmodels.SettingsEffect
import com.vm.coinfold.app.feature.settings.domain.viewmodels.SettingsIntent
import com.vm.coinfold.app.feature.settings.domain.viewmodels.SettingsState
import com.vm.coinfold.app.feature.settings.domain.viewmodels.SettingsViewModel
import com.vm.coinfold.app.feature.settings.ui.components.OptionChips
import com.vm.coinfold.app.feature.settings.ui.components.PeriodDayStepper
import com.vm.coinfold.app.feature.settings.ui.components.RatesStatus
import com.vm.coinfold.app.feature.settings.ui.components.SettingsSection
import com.vm.coinfold.app.shared.platform.rememberBiometricAuthenticator
import com.vm.coinfold.app.shared.ui.components.CurrencySelector
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onOpenRecurring: () -> Unit,
    onOpenBackup: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    securityViewModel: SecurityViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowMessage -> snackbar.showSnackbar(getString(effect.message))
            }
        }
    }

    val security by securityViewModel.state.collectAsStateWithLifecycle()
    val biometrics = rememberBiometricAuthenticator()
    val scope = rememberCoroutineScope()
    val promptTitle = stringResource(Res.string.biometric_title)
    val promptSubtitle = stringResource(Res.string.biometric_subtitle)
    val promptCancel = stringResource(Res.string.biometric_cancel)

    SettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenRecurring = onOpenRecurring,
        onOpenBackup = onOpenBackup,
        security = security,
        biometricAvailable = biometrics.isAvailable,
        onSecurityIntent = securityViewModel::onIntent,
        onBiometricToggle = { enable ->
            if (!enable) {
                securityViewModel.onIntent(SecurityIntent.BiometricToggled(false))
            } else {
                scope.launch {
                    if (biometrics.authenticate(promptTitle, promptSubtitle, promptCancel)) {
                        securityViewModel.onIntent(SecurityIntent.BiometricToggled(true))
                    }
                }
            }
        },
        snackbar = snackbar,
    )
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenBackup: () -> Unit,
    security: SecurityState,
    biometricAvailable: Boolean,
    onSecurityIntent: (SecurityIntent) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    snackbar: SnackbarHostState,
) {
    val settings = state.settings
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleLarge)

            SettingsSection(stringResource(Res.string.settings_theme)) {
                OptionChips(
                    options = listOf(
                        ThemeMode.SYSTEM to stringResource(Res.string.theme_system),
                        ThemeMode.LIGHT to stringResource(Res.string.theme_light),
                        ThemeMode.DARK to stringResource(Res.string.theme_dark),
                    ),
                    selected = settings.theme,
                    onSelected = { onIntent(SettingsIntent.ThemeSelected(it)) },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsSection(stringResource(Res.string.settings_language)) {
                OptionChips(
                    options = listOf(
                        AppLanguage.SYSTEM to stringResource(Res.string.language_system),
                        AppLanguage.EN to stringResource(Res.string.language_en),
                        AppLanguage.UK to stringResource(Res.string.language_uk),
                    ),
                    selected = settings.language,
                    onSelected = { onIntent(SettingsIntent.LanguageSelected(it)) },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsSection(
                title = stringResource(Res.string.settings_main_currency),
                hint = stringResource(Res.string.settings_main_currency_hint),
            ) {
                CurrencySelector(
                    selected = settings.mainCurrency,
                    onSelected = { onIntent(SettingsIntent.MainCurrencySelected(it)) },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsSection(
                title = stringResource(Res.string.settings_period_start),
                hint = stringResource(Res.string.settings_period_hint),
            ) {
                PeriodDayStepper(
                    day = settings.periodStartDay,
                    onDayChange = { onIntent(SettingsIntent.PeriodStartDaySelected(it)) },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Opens the list of scheduled payments (subscriptions, rent, salary).
            SettingsSection(
                title = stringResource(Res.string.settings_recurring),
                hint = stringResource(Res.string.settings_recurring_hint),
            ) {
                OutlinedButton(onClick = onOpenRecurring) {
                    Text(stringResource(Res.string.settings_recurring_open))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SecuritySection(security, biometricAvailable, onSecurityIntent, onBiometricToggle)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Backup, restore and export live on their own screen.
            SettingsSection(
                title = stringResource(Res.string.settings_backup),
                hint = stringResource(Res.string.settings_backup_hint),
            ) {
                OutlinedButton(onClick = onOpenBackup) { Text(stringResource(Res.string.settings_recurring_open)) }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsSection(stringResource(Res.string.settings_rates)) {
                RatesStatus(
                    updatedAt = state.ratesUpdatedAt,
                    isRefreshing = state.isRefreshingRates,
                    onRefresh = { onIntent(SettingsIntent.RefreshRatesClicked) },
                )
            }
        }
    }
    security.dialog?.let { dialog ->
        PinDialog(
            state = dialog,
            onDigit = { onSecurityIntent(SecurityIntent.Digit(it)) },
            onBackspace = { onSecurityIntent(SecurityIntent.Backspace) },
            onDismiss = { onSecurityIntent(SecurityIntent.DismissDialog) },
        )
    }
}
