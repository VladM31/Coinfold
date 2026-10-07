package com.vm.coinfold.app.feature.accounts.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_add
import coinfold.shared.generated.resources.accounts_delete_message
import coinfold.shared.generated.resources.accounts_delete_message_archive
import coinfold.shared.generated.resources.accounts_delete_title
import coinfold.shared.generated.resources.accounts_empty
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.action_delete
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsDialog
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsEffect
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsIntent
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsState
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsViewModel
import com.vm.coinfold.app.feature.accounts.ui.components.AccountFormSheet
import com.vm.coinfold.app.feature.accounts.ui.components.AccountListItem
import com.vm.coinfold.app.feature.accounts.ui.components.AccountOperationSheet
import com.vm.coinfold.app.feature.accounts.ui.components.TotalCard
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
                AccountListItem(
                    item = item,
                    onClick = { onIntent(AccountsIntent.AccountClicked(item)) },
                    onLongClick = { onIntent(AccountsIntent.EditAccountClicked(item)) },
                )
            }
        }
    }

    AccountDialogs(state, onIntent)
}

@Composable
private fun AccountDialogs(state: AccountsState, onIntent: (AccountsIntent) -> Unit) {
    val dismiss = { onIntent(AccountsIntent.DismissDialog) }
    when (val dialog = state.dialog) {
        null -> Unit
        AccountsDialog.AddAccount -> AccountFormSheet(
            item = null,
            onSave = { name, currency, initial, color, icon ->
                onIntent(AccountsIntent.SaveAccount(null, name, currency, initial, color, icon))
            },
            onDelete = null,
            onDismiss = dismiss,
        )
        is AccountsDialog.EditAccount -> AccountFormSheet(
            item = dialog.item,
            onSave = { name, currency, initial, color, icon ->
                onIntent(AccountsIntent.SaveAccount(dialog.item.account.id, name, currency, initial, color, icon))
            },
            onDelete = { onIntent(AccountsIntent.DeleteAccountClicked(dialog.item)) },
            onDismiss = dismiss,
        )
        is AccountsDialog.Operation -> AccountOperationSheet(
            item = dialog.item,
            customSources = state.customSources,
            onSave = { type, amount, source, note, date ->
                onIntent(AccountsIntent.SaveOperation(type, amount, source, note, date))
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
