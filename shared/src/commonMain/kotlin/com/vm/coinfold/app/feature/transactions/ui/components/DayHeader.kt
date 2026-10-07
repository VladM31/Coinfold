package com.vm.coinfold.app.feature.transactions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vm.coinfold.app.feature.transactions.domain.models.DayGroup
import com.vm.coinfold.app.feature.transactions.domain.viewmodels.TransactionsState
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format

@Composable
fun DayHeader(group: DayGroup, state: TransactionsState) {
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
