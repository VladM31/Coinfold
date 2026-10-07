package com.vm.coinfold.app.feature.accounts.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_add
import coinfold.shared.generated.resources.accounts_delete_message
import coinfold.shared.generated.resources.accounts_delete_message_archive
import coinfold.shared.generated.resources.accounts_delete_title
import coinfold.shared.generated.resources.accounts_empty
import coinfold.shared.generated.resources.accounts_rates_missing
import coinfold.shared.generated.resources.accounts_top_up
import coinfold.shared.generated.resources.accounts_total_balance
import coinfold.shared.generated.resources.accounts_withdraw
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.action_delete
import com.vm.coinfold.app.feature.accounts.main.AccountWithBalance
import com.vm.coinfold.app.feature.accounts.main.AccountsDialog
import com.vm.coinfold.app.feature.accounts.main.AccountsEffect
import com.vm.coinfold.app.feature.accounts.main.AccountsIntent
import com.vm.coinfold.app.feature.accounts.main.AccountsState
import com.vm.coinfold.app.feature.accounts.main.AccountsViewModel
import com.vm.coinfold.app.shared.ui.ColorDot
import com.vm.coinfold.app.shared.ui.CurrencySelector
import com.vm.coinfold.app.shared.ui.LocalAppLanguage
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountsScreen(viewModel: AccountsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccountsEffect.ShowMessage -> snackbar.showSnackbar(getString(effect.message))
            }
        }
    }

    AccountsContent(state, viewModel::onIntent, snackbar)
}

@Composable
private fun AccountsContent(
    state: AccountsState,
    onIntent: (AccountsIntent) -> Unit,
    snackbar: SnackbarHostState,
) {
    Scaffold(
        // Insets are already handled by the root scaffold.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onIntent(AccountsIntent.AddAccountClicked) }) {
                Text("+  " + stringResource(Res.string.accounts_add))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { TotalCard(state, onIntent) }
            if (state.accounts.isEmpty() && !state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(Res.string.accounts_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            items(state.accounts, key = { it.account.id }) { item ->
                AccountCard(item, onIntent)
            }
        }
    }

    AccountDialogs(state, onIntent)
}

@Composable
private fun TotalCard(state: AccountsState, onIntent: (AccountsIntent) -> Unit) {
    val language = LocalAppLanguage.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(Res.string.accounts_total_balance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = state.total?.money?.format(language) ?: "",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (state.total?.hasMissingRates == true) {
                Text(
                    stringResource(Res.string.accounts_rates_missing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            CurrencySelector(
                selected = state.mainCurrency,
                onSelected = { onIntent(AccountsIntent.MainCurrencySelected(it)) },
            )
        }
    }
}

@Composable
private fun AccountCard(item: AccountWithBalance, onIntent: (AccountsIntent) -> Unit) {
    val language = LocalAppLanguage.current
    val account = item.account
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable { onIntent(AccountsIntent.EditAccountClicked(item)) },
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ColorDot(account.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary, size = 14)
                Column(Modifier.weight(1f)) {
                    Text(account.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        account.currency.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    item.balance.format(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row {
                TextButton(onClick = { onIntent(AccountsIntent.TopUpClicked(item)) }) {
                    Text("+ " + stringResource(Res.string.accounts_top_up))
                }
                TextButton(onClick = { onIntent(AccountsIntent.WithdrawClicked(item)) }) {
                    Text("− " + stringResource(Res.string.accounts_withdraw))
                }
            }
        }
    }
}

@Composable
private fun AccountDialogs(state: AccountsState, onIntent: (AccountsIntent) -> Unit) {
    val dismiss = { onIntent(AccountsIntent.DismissDialog) }
    when (val dialog = state.dialog) {
        null -> Unit
        AccountsDialog.AddAccount -> AccountFormSheet(
            item = null,
            onSave = { name, currency, initial, color ->
                onIntent(AccountsIntent.SaveAccount(null, name, currency, initial, color))
            },
            onDelete = null,
            onDismiss = dismiss,
        )
        is AccountsDialog.EditAccount -> AccountFormSheet(
            item = dialog.item,
            onSave = { name, currency, initial, color ->
                onIntent(AccountsIntent.SaveAccount(dialog.item.account.id, name, currency, initial, color))
            },
            onDelete = { onIntent(AccountsIntent.DeleteAccountClicked(dialog.item)) },
            onDismiss = dismiss,
        )
        is AccountsDialog.Operation -> OperationSheet(
            item = dialog.item,
            type = dialog.type,
            customSources = state.customSources,
            onSave = { amount, source, note, date ->
                onIntent(AccountsIntent.SaveOperation(amount, source, note, date))
            },
            onDismiss = dismiss,
        )
        is AccountsDialog.ConfirmDelete -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(stringResource(Res.string.accounts_delete_title)) },
            text = {
                Text(
                    stringResource(
                        if (dialog.item.transactionCount > 0) {
                            Res.string.accounts_delete_message_archive
                        } else {
                            Res.string.accounts_delete_message
                        },
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { onIntent(AccountsIntent.ConfirmDelete) }) {
                    Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = dismiss) { Text(stringResource(Res.string.action_cancel)) } },
        )
    }
}
