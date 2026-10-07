package com.vm.coinfold.app.feature.overview.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_rates_missing
import coinfold.shared.generated.resources.overview_balance
import coinfold.shared.generated.resources.overview_expenses
import coinfold.shared.generated.resources.overview_income
import com.vm.coinfold.app.feature.overview.domain.models.OverviewSummary
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

/** Balance of the period (income minus expenses) with the expenses and income cards below it. */
@Composable
fun BalanceHeader(summary: OverviewSummary, modifier: Modifier = Modifier) {
    val language = LocalAppLanguage.current
    val balance = summary.balance
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(Res.string.overview_balance), style = MaterialTheme.typography.labelLarge)
            Text(
                balance.format(language),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance.minorUnits < 0) MaterialTheme.colorScheme.error else IncomeGreen,
            )
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AmountCard(
                label = stringResource(Res.string.overview_expenses),
                value = summary.spent.format(language),
                container = MaterialTheme.colorScheme.primary,
                content = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            AmountCard(
                label = stringResource(Res.string.overview_income),
                value = summary.income.format(language),
                container = IncomeGreen.copy(alpha = 0.16f),
                content = IncomeGreen,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
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
private fun AmountCard(label: String, value: String, container: Color, content: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
