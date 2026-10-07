package com.vm.coinfold.app.feature.recurring.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import coinfold.shared.generated.resources.recurring_add
import coinfold.shared.generated.resources.recurring_empty
import coinfold.shared.generated.resources.recurring_title
import com.vm.coinfold.app.feature.recurring.domain.viewmodels.RecurringDialog
import com.vm.coinfold.app.feature.recurring.domain.viewmodels.RecurringEffect
import com.vm.coinfold.app.feature.recurring.domain.viewmodels.RecurringIntent
import com.vm.coinfold.app.feature.recurring.domain.viewmodels.RecurringState
import com.vm.coinfold.app.feature.recurring.domain.viewmodels.RecurringViewModel
import com.vm.coinfold.app.feature.recurring.ui.components.RecurringFormSheet
import com.vm.coinfold.app.feature.recurring.ui.components.RecurringListItem
import com.vm.coinfold.app.shared.ui.components.showUndo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Reached from Settings; [onBack] returns there. */
@Composable
fun RecurringScreen(onBack: () -> Unit, viewModel: RecurringViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RecurringEffect.ShowUndo ->
                    if (snackbar.showUndo(effect.message)) viewModel.onIntent(RecurringIntent.UndoDelete)
            }
        }
    }

    RecurringContent(state, viewModel::onIntent, onBack, snackbar)
}

@Composable
private fun RecurringContent(
    state: RecurringState,
    onIntent: (RecurringIntent) -> Unit,
    onBack: () -> Unit,
    snackbar: SnackbarHostState,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onIntent(RecurringIntent.AddClicked) }) {
                Text("+  " + stringResource(Res.string.recurring_add))
            }
        },
    ) { padding ->
        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineSmall) }
                Text(stringResource(Res.string.recurring_title), style = MaterialTheme.typography.titleLarge)
            }
            if (state.payments.isEmpty() && !state.isLoading) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.recurring_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    items(state.payments, key = { it.id }) { payment ->
                        RecurringListItem(
                            payment = payment,
                            onClick = { onIntent(RecurringIntent.ItemClicked(payment)) },
                            onActiveChange = { onIntent(RecurringIntent.ToggleActive(payment, it)) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
    RecurringDialogs(state, onIntent)
}

@Composable
private fun RecurringDialogs(state: RecurringState, onIntent: (RecurringIntent) -> Unit) {
    val dismiss = { onIntent(RecurringIntent.DismissDialog) }
    val (payment, visible) = when (val dialog = state.dialog) {
        null -> return
        RecurringDialog.Add -> null to true
        is RecurringDialog.Edit -> dialog.payment to true
    }
    if (!visible) return
    RecurringFormSheet(
        payment = payment,
        accounts = state.accounts,
        categories = state.categories,
        customSources = state.customSources,
        onSave = { type, accountId, categoryId, source, amount, note, frequency, firstDate ->
            onIntent(
                RecurringIntent.Save(
                    id = payment?.id,
                    type = type,
                    accountId = accountId,
                    categoryId = categoryId,
                    source = source,
                    amount = amount,
                    note = note,
                    frequency = frequency,
                    firstDate = firstDate,
                    isActive = payment?.isActive ?: true,
                ),
            )
        },
        onDelete = payment?.let { { onIntent(RecurringIntent.Delete(it)) } },
        onDismiss = dismiss,
    )
}
