package com.vm.coinfold.app.feature.transactions.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.filter_account
import coinfold.shared.generated.resources.filter_all
import coinfold.shared.generated.resources.filter_apply
import coinfold.shared.generated.resources.filter_category
import coinfold.shared.generated.resources.filter_from
import coinfold.shared.generated.resources.filter_period
import coinfold.shared.generated.resources.filter_period_all
import coinfold.shared.generated.resources.filter_period_current
import coinfold.shared.generated.resources.filter_period_custom
import coinfold.shared.generated.resources.filter_to
import coinfold.shared.generated.resources.filter_type
import coinfold.shared.generated.resources.type_expense
import coinfold.shared.generated.resources.type_income
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.transactions.domain.models.PeriodFilter
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionFilter
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.shared.ui.components.CategoryIcon
import com.vm.coinfold.app.shared.ui.components.DateField
import com.vm.coinfold.app.utils.today
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringResource

private enum class PeriodMode { ALL, CURRENT, CUSTOM }

/** Period, account, category and type filters; nothing is applied until the user confirms. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    filter: TransactionFilter,
    accounts: List<AccountWithBalance>,
    categories: List<Category>,
    onApply: (TransactionFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    var mode by remember {
        mutableStateOf(
            when (filter.period) {
                PeriodFilter.All -> PeriodMode.ALL
                PeriodFilter.CurrentPeriod -> PeriodMode.CURRENT
                is PeriodFilter.Custom -> PeriodMode.CUSTOM
            },
        )
    }
    var from by remember {
        mutableStateOf((filter.period as? PeriodFilter.Custom)?.from ?: today().minus(1, DateTimeUnit.MONTH))
    }
    var to by remember { mutableStateOf((filter.period as? PeriodFilter.Custom)?.to ?: today()) }
    var accountId by remember { mutableStateOf(filter.accountId) }
    var categoryId by remember { mutableStateOf(filter.categoryId) }
    var type by remember { mutableStateOf(filter.type) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Section(stringResource(Res.string.filter_period)) {
                Chip(stringResource(Res.string.filter_period_all), mode == PeriodMode.ALL) { mode = PeriodMode.ALL }
                Chip(stringResource(Res.string.filter_period_current), mode == PeriodMode.CURRENT) {
                    mode = PeriodMode.CURRENT
                }
                Chip(stringResource(Res.string.filter_period_custom), mode == PeriodMode.CUSTOM) {
                    mode = PeriodMode.CUSTOM
                }
            }
            if (mode == PeriodMode.CUSTOM) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.filter_from))
                    DateField(from, onDateChange = { from = it })
                    Text(stringResource(Res.string.filter_to))
                    DateField(to, onDateChange = { to = it })
                }
            }

            Section(stringResource(Res.string.filter_type)) {
                Chip(stringResource(Res.string.filter_all), type == null) { type = null }
                Chip(stringResource(Res.string.type_income), type == TransactionType.INCOME) {
                    type = TransactionType.INCOME
                }
                Chip(stringResource(Res.string.type_expense), type == TransactionType.EXPENSE) {
                    type = TransactionType.EXPENSE
                }
            }

            Section(stringResource(Res.string.filter_account)) {
                Chip(stringResource(Res.string.filter_all), accountId == null) { accountId = null }
                accounts.forEach { Chip(it.account.name, accountId == it.account.id) { accountId = it.account.id } }
            }

            Section(stringResource(Res.string.filter_category)) {
                Chip(stringResource(Res.string.filter_all), categoryId == null) { categoryId = null }
                categories.forEach {
                    Chip(it.name, categoryId == it.id, icon = it.icon) { categoryId = it.id }
                }
            }

            Button(
                onClick = {
                    val period = when (mode) {
                        PeriodMode.ALL -> PeriodFilter.All
                        PeriodMode.CURRENT -> PeriodFilter.CurrentPeriod
                        // Swap if the user picked the dates the wrong way round.
                        PeriodMode.CUSTOM -> if (from <= to) PeriodFilter.Custom(from, to) else PeriodFilter.Custom(to, from)
                    }
                    onApply(TransactionFilter(period, accountId, categoryId, type))
                },
                modifier = Modifier.align(Alignment.End),
            ) { Text(stringResource(Res.string.filter_apply)) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

@Composable
private fun Chip(label: String, selected: Boolean, icon: String? = null, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon?.let {
            { CategoryIcon(it, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18) }
        },
    )
}
