package com.vm.coinfold.app.feature.overview.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.overview_by_category
import coinfold.shared.generated.resources.overview_no_expenses
import com.vm.coinfold.app.feature.overview.domain.viewmodels.OverviewIntent
import com.vm.coinfold.app.feature.overview.domain.viewmodels.OverviewState
import com.vm.coinfold.app.feature.overview.domain.viewmodels.OverviewViewModel
import com.vm.coinfold.app.feature.overview.ui.components.AverageCards
import com.vm.coinfold.app.feature.overview.ui.components.BalanceHeader
import com.vm.coinfold.app.feature.overview.ui.components.CategoryShareRow
import com.vm.coinfold.app.feature.overview.ui.components.OverviewChart
import com.vm.coinfold.app.feature.transactions.domain.models.PeriodFilter
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionFilter
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.shared.ui.components.PeriodSwitcher
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OverviewScreen(
    onOpenTransactions: (TransactionFilter) -> Unit,
    viewModel: OverviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OverviewContent(state, viewModel::onIntent, onOpenTransactions)
}

@Composable
private fun OverviewContent(
    state: OverviewState,
    onIntent: (OverviewIntent) -> Unit,
    onOpenTransactions: (TransactionFilter) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.period?.let { period ->
            item {
                PeriodSwitcher(
                    period = period,
                    onPrevious = { onIntent(OverviewIntent.PreviousPeriod) },
                    onNext = { onIntent(OverviewIntent.NextPeriod) },
                )
            }
        }
        val summary = state.summary ?: return@LazyColumn
        item { BalanceHeader(summary) }
        item { OverviewChart(summary) }
        item { AverageCards(summary) }

        if (summary.shares.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.overview_no_expenses),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            item { Text(stringResource(Res.string.overview_by_category), style = MaterialTheme.typography.titleMedium) }
            items(summary.shares, key = { it.category?.id ?: -1L }) { share ->
                val categoryId = share.category?.id
                val period = state.period
                CategoryShareRow(
                    share,
                    // "Other" has no category to filter by, so it is not tappable.
                    modifier = if (categoryId != null && period != null) {
                        Modifier.clickable {
                            onOpenTransactions(
                                TransactionFilter(
                                    period = PeriodFilter.Custom(period.start, period.endInclusive),
                                    categoryId = categoryId,
                                    type = TransactionType.EXPENSE,
                                ),
                            )
                        }
                    } else Modifier,
                )
            }
        }
    }
}
