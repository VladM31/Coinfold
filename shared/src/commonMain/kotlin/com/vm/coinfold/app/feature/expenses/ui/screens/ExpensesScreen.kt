package com.vm.coinfold.app.feature.expenses.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.category_delete_message
import coinfold.shared.generated.resources.category_delete_title
import com.vm.coinfold.app.feature.expenses.domain.models.CategorySpend
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesDialog
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesEffect
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesIntent
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesState
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesViewModel
import com.vm.coinfold.app.feature.expenses.ui.components.AddExpenseSheet
import com.vm.coinfold.app.feature.expenses.ui.components.CategoryFormSheet
import com.vm.coinfold.app.feature.expenses.ui.components.CategoryGrid
import com.vm.coinfold.app.feature.expenses.ui.components.SummaryRing
import com.vm.coinfold.app.shared.ui.components.PeriodSwitcher
import com.vm.coinfold.app.shared.ui.components.showUndo
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExpensesScreen(viewModel: ExpensesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExpensesEffect.ShowMessage -> snackbar.showSnackbar(getString(effect.message))
                is ExpensesEffect.ShowUndo -> if (snackbar.showUndo(effect.message)) viewModel.onIntent(ExpensesIntent.UndoDeleteCategory)
            }
        }
    }

    ExpensesContent(state, viewModel::onIntent, snackbar)
}

@Composable
private fun ExpensesContent(state: ExpensesState, onIntent: (ExpensesIntent) -> Unit, snackbar: SnackbarHostState) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.period?.let { period ->
                item {
                    PeriodSwitcher(
                        period = period,
                        onPrevious = { onIntent(ExpensesIntent.PreviousPeriod) },
                        onNext = { onIntent(ExpensesIntent.NextPeriod) },
                    )
                }
            }
            state.summary?.let { summary -> item { SummaryRing(summary) } }
            // Categories as a draggable grid of circles; the last cell is the "add" button.
            item {
                CategoryGrid(
                    items = state.summary?.perCategory.orEmpty(),
                    onCategoryClick = { onIntent(ExpensesIntent.CategoryClicked(it)) },
                    onCategoryEdit = { onIntent(ExpensesIntent.CategoryLongClicked(it)) },
                    onAddClick = { onIntent(ExpensesIntent.AddCategoryClicked) },
                    onReorder = { onIntent(ExpensesIntent.ReorderCategories(it)) },
                )
            }
        }
    }
    ExpensesDialogs(state, onIntent)
}

@Composable
private fun ExpensesDialogs(state: ExpensesState, onIntent: (ExpensesIntent) -> Unit) {
    val dismiss = { onIntent(ExpensesIntent.DismissDialog) }
    when (val dialog = state.dialog) {
        null -> Unit
        ExpensesDialog.AddCategory -> CategoryFormSheet(
            category = null,
            onSave = { name, color, icon -> onIntent(ExpensesIntent.SaveCategory(null, name, color, icon)) },
            onDelete = null,
            onDismiss = dismiss,
        )
        is ExpensesDialog.EditCategory -> {
            CategoryFormSheet(
                category = dialog.category,
                onSave = { name, color, icon ->
                    onIntent(ExpensesIntent.SaveCategory(dialog.category.id, name, color, icon))
                },
                onDelete = { onIntent(ExpensesIntent.DeleteCategoryClicked(dialog.category)) },
                onDismiss = dismiss,
            )
        }
        is ExpensesDialog.AddExpense -> AddExpenseSheet(
            category = dialog.category,
            accounts = state.accounts,
            categories = state.categories,
            noteSuggestions = state.noteSuggestions,
            rates = state.rates,
            initialCurrency = state.lastUsedCurrency,
            onSave = { amount, currency, accountId, categoryId, note, date, rate ->
                onIntent(ExpensesIntent.SaveExpense(amount, currency, accountId, categoryId, note, date, rate))
            },
            onDismiss = dismiss,
        )
        is ExpensesDialog.ConfirmDeleteCategory -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(stringResource(Res.string.category_delete_title)) },
            text = { Text(stringResource(Res.string.category_delete_message)) },
            confirmButton = {
                TextButton(onClick = { onIntent(ExpensesIntent.ConfirmDeleteCategory) }) {
                    Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = dismiss) { Text(stringResource(Res.string.action_cancel)) } },
        )
    }
}
