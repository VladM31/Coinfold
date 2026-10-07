package com.vm.coinfold.app.feature.transactions.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.transaction_delete_message
import coinfold.shared.generated.resources.transaction_delete_title
import coinfold.shared.generated.resources.transaction_manual_withdrawal
import coinfold.shared.generated.resources.transactions_empty
import coinfold.shared.generated.resources.transactions_filter
import coinfold.shared.generated.resources.transactions_filter_reset
import coinfold.shared.generated.resources.transactions_title
import com.vm.coinfold.app.feature.transactions.domain.models.DayGroup
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.viewmodels.TransactionsDialog
import com.vm.coinfold.app.feature.transactions.domain.viewmodels.TransactionsEffect
import com.vm.coinfold.app.feature.transactions.domain.viewmodels.TransactionsIntent
import com.vm.coinfold.app.feature.transactions.domain.viewmodels.TransactionsState
import com.vm.coinfold.app.feature.transactions.domain.viewmodels.TransactionsViewModel
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.shared.ui.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.displayName
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TransactionsEffect.ShowMessage -> snackbar.showSnackbar(getString(effect.message))
            }
        }
    }

    TransactionsContent(state, viewModel::onIntent, snackbar)
}

@Composable
private fun TransactionsContent(
    state: TransactionsState,
    onIntent: (TransactionsIntent) -> Unit,
    snackbar: SnackbarHostState,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.transactions_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (state.filter.isActive) {
                    TextButton(onClick = { onIntent(TransactionsIntent.ResetFilter) }) {
                        Text(stringResource(Res.string.transactions_filter_reset))
                    }
                }
                TextButton(onClick = { onIntent(TransactionsIntent.FilterClicked) }) {
                    Text(stringResource(Res.string.transactions_filter) + if (state.filter.isActive) " •" else "")
                }
            }

            if (state.groups.isEmpty() && !state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.transactions_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    state.groups.forEach { group ->
                        item(key = "day-${group.date}") { DayHeader(group, state) }
                        items(group.items, key = { it.id }) { item ->
                            TransactionRow(item) { onIntent(TransactionsIntent.ItemClicked(item)) }
                        }
                    }
                    if (state.canLoadMore) {
                        // Composed only when the user scrolls to the end of the loaded page.
                        item(key = "load-more") {
                            LaunchedEffect(Unit) { onIntent(TransactionsIntent.LoadMore) }
                        }
                    }
                }
            }
        }
    }
    TransactionsDialogs(state, onIntent)
}

@Composable
private fun DayHeader(group: DayGroup, state: TransactionsState) {
    val language = LocalAppLanguage.current
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                group.date.format(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                group.total.format(language, showPlus = true),
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    group.total.minorUnits > 0 -> IncomeGreen
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun TransactionRow(item: TransactionItem, onClick: () -> Unit) {
    val language = LocalAppLanguage.current
    val isIncome = item.type == TransactionType.INCOME
    val title = when {
        item.category != null -> item.category.name
        isIncome -> item.source?.displayName().orEmpty()
        else -> stringResource(Res.string.transaction_manual_withdrawal)
    }
    val badgeColor = item.category?.let { Color(it.color) } ?: if (isIncome) IncomeGreen else MaterialTheme.colorScheme.outline
    val badgeText = item.category?.icon ?: if (isIncome) "+" else "−"

    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(badgeColor),
            contentAlignment = Alignment.Center,
        ) { Text(badgeText, style = MaterialTheme.typography.titleMedium, color = Color.White) }

        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val subtitle = listOf(item.note, item.accountName).filter { it.isNotBlank() }.joinToString(" · ")
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                item.signedAmount.format(language, showPlus = true),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface,
            )
            if (item.amount.currency != item.accountCurrency) {
                // What actually moved on the account, at the rate fixed when the expense was made.
                val accountSide = if (isIncome) item.accountAmount else -item.accountAmount
                Text(
                    accountSide.format(language, showPlus = true),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun TransactionsDialogs(state: TransactionsState, onIntent: (TransactionsIntent) -> Unit) {
    val dismiss = { onIntent(TransactionsIntent.DismissDialog) }
    when (val dialog = state.dialog) {
        null -> Unit
        TransactionsDialog.Filter -> FilterSheet(
            filter = state.filter,
            accounts = state.accounts,
            categories = state.categories,
            onApply = { onIntent(TransactionsIntent.ApplyFilter(it)) },
            onDismiss = dismiss,
        )
        is TransactionsDialog.Edit -> EditTransactionSheet(
            item = dialog.item,
            accounts = state.accounts,
            categories = state.categories,
            customSources = state.customSources,
            onSave = { amount, currency, accountId, categoryId, source, note, date ->
                onIntent(TransactionsIntent.SaveEdit(amount, currency, accountId, categoryId, source, note, date))
            },
            onDelete = { onIntent(TransactionsIntent.DeleteClicked(dialog.item)) },
            onDismiss = dismiss,
        )
        is TransactionsDialog.ConfirmDelete -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(stringResource(Res.string.transaction_delete_title)) },
            text = { Text(stringResource(Res.string.transaction_delete_message)) },
            confirmButton = {
                TextButton(onClick = { onIntent(TransactionsIntent.ConfirmDelete) }) {
                    Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = dismiss) { Text(stringResource(Res.string.action_cancel)) } },
        )
    }
}
