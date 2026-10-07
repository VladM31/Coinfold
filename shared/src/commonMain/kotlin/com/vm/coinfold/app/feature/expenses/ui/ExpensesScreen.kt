package com.vm.coinfold.app.feature.expenses.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_rates_missing
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.category_add
import coinfold.shared.generated.resources.category_delete_message
import coinfold.shared.generated.resources.category_delete_title
import coinfold.shared.generated.resources.expenses_income
import coinfold.shared.generated.resources.expenses_left
import coinfold.shared.generated.resources.expenses_overspent
import coinfold.shared.generated.resources.expenses_spent
import com.vm.coinfold.app.feature.expenses.main.CategorySpend
import com.vm.coinfold.app.feature.expenses.main.ExpenseSummary
import com.vm.coinfold.app.feature.expenses.main.ExpensesDialog
import com.vm.coinfold.app.feature.expenses.main.ExpensesEffect
import com.vm.coinfold.app.feature.expenses.main.ExpensesIntent
import com.vm.coinfold.app.feature.expenses.main.ExpensesState
import com.vm.coinfold.app.feature.expenses.main.ExpensesViewModel
import com.vm.coinfold.app.feature.expenses.main.Period
import com.vm.coinfold.app.shared.ui.DonutChart
import com.vm.coinfold.app.shared.ui.DonutSegment
import com.vm.coinfold.app.shared.ui.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val GRID_COLUMNS = 4

@Composable
fun ExpensesScreen(viewModel: ExpensesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExpensesEffect.ShowMessage -> snackbar.showSnackbar(getString(effect.message))
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
            state.period?.let { period -> item { PeriodSwitcher(period, onIntent) } }
            state.summary?.let { summary -> item { SummaryRing(summary) } }
            // Categories as a grid of circles, the last cell is the "add" button.
            val cells = state.summary?.perCategory.orEmpty()
            val rows = (cells.map<CategorySpend, CategorySpend?> { it } + null).chunked(GRID_COLUMNS)
            items(rows.size) { index ->
                CategoryRow(rows[index], onIntent)
            }
        }
    }
    ExpensesDialogs(state, onIntent)
}

@Composable
private fun PeriodSwitcher(period: Period, onIntent: (ExpensesIntent) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { onIntent(ExpensesIntent.PreviousPeriod) }) { Text("‹", style = MaterialTheme.typography.titleLarge) }
        Text(
            text = "${period.start.format()} – ${period.endInclusive.format()}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = { onIntent(ExpensesIntent.NextPeriod) }) { Text("›", style = MaterialTheme.typography.titleLarge) }
    }
}

@Composable
private fun SummaryRing(summary: ExpenseSummary) {
    val language = LocalAppLanguage.current
    val remaining = summary.remaining

    val segments = buildList {
        summary.perCategory.forEach { add(DonutSegment(it.spent.minorUnits, Color(it.category.color))) }
        add(DonutSegment(summary.other.minorUnits, MaterialTheme.colorScheme.outline))
        // Green "left" part only exists while income exceeds spending.
        add(DonutSegment(remaining.minorUnits, IncomeGreen))
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DonutChart(
            segments = segments,
            emptyColor = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.widthIn(max = 260.dp).fillMaxWidth(0.8f),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(if (remaining.minorUnits < 0) Res.string.expenses_overspent else Res.string.expenses_left),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    remaining.format(language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (remaining.minorUnits < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Amount(stringResource(Res.string.expenses_income), summary.income.format(language), IncomeGreen)
            Amount(stringResource(Res.string.expenses_spent), summary.spent.format(language), MaterialTheme.colorScheme.onSurface)
        }
        if (summary.hasMissingRates) {
            Text(
                stringResource(Res.string.accounts_rates_missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun Amount(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun CategoryRow(row: List<CategorySpend?>, onIntent: (ExpensesIntent) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        repeat(GRID_COLUMNS) { column ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                if (column < row.size) {
                    val item = row[column]
                    if (item == null) AddCategoryCircle(onIntent) else CategoryCircle(item, onIntent)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryCircle(item: CategorySpend, onIntent: (ExpensesIntent) -> Unit) {
    val language = LocalAppLanguage.current
    val category = item.category
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = { onIntent(ExpensesIntent.CategoryClicked(category)) },
                onLongClick = { onIntent(ExpensesIntent.CategoryLongClicked(category)) },
            )
            .padding(4.dp),
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(Color(category.color)),
            contentAlignment = Alignment.Center,
        ) { Text(category.icon, style = MaterialTheme.typography.titleLarge) }
        Text(category.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (item.spent.minorUnits > 0) {
            Text(
                item.spent.format(language),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddCategoryCircle(onIntent: (ExpensesIntent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = { onIntent(ExpensesIntent.AddCategoryClicked) })
            .padding(4.dp),
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text("+", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) }
        Text(stringResource(Res.string.category_add), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun ExpensesDialogs(state: ExpensesState, onIntent: (ExpensesIntent) -> Unit) {
    val dismiss = { onIntent(ExpensesIntent.DismissDialog) }
    when (val dialog = state.dialog) {
        null -> Unit
        ExpensesDialog.AddCategory -> CategoryFormSheet(
            category = null,
            canMoveUp = false,
            canMoveDown = false,
            onSave = { name, color, icon -> onIntent(ExpensesIntent.SaveCategory(null, name, color, icon)) },
            onMove = {},
            onDelete = null,
            onDismiss = dismiss,
        )
        is ExpensesDialog.EditCategory -> {
            val index = state.categories.indexOfFirst { it.id == dialog.category.id }
            CategoryFormSheet(
                category = dialog.category,
                canMoveUp = index > 0,
                canMoveDown = index in 0 until state.categories.lastIndex,
                onSave = { name, color, icon ->
                    onIntent(ExpensesIntent.SaveCategory(dialog.category.id, name, color, icon))
                },
                onMove = { up -> onIntent(ExpensesIntent.MoveCategory(dialog.category, up)) },
                onDelete = { onIntent(ExpensesIntent.DeleteCategoryClicked(dialog.category)) },
                onDismiss = dismiss,
            )
        }
        is ExpensesDialog.AddExpense -> AddExpenseSheet(
            category = dialog.category,
            accounts = state.accounts,
            initialCurrency = state.lastUsedCurrency,
            onSave = { amount, currency, accountId, note, date ->
                onIntent(ExpensesIntent.SaveExpense(amount, currency, accountId, note, date))
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
