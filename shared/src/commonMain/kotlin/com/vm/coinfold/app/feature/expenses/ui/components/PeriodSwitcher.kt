package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesIntent
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.utils.format

@Composable
fun PeriodSwitcher(period: Period, onIntent: (ExpensesIntent) -> Unit) {
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
