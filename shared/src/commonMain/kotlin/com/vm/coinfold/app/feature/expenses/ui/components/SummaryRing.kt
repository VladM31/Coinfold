package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_rates_missing
import coinfold.shared.generated.resources.expenses_income
import coinfold.shared.generated.resources.expenses_left
import coinfold.shared.generated.resources.expenses_overspent
import coinfold.shared.generated.resources.expenses_spent
import com.vm.coinfold.app.feature.expenses.domain.models.ExpenseSummary
import com.vm.coinfold.app.shared.ui.components.DonutChart
import com.vm.coinfold.app.shared.ui.components.DonutSegment
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

@Composable
fun SummaryRing(summary: ExpenseSummary) {
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
