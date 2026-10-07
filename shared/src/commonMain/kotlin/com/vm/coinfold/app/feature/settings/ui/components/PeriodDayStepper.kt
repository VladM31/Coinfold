package com.vm.coinfold.app.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.settings_period_start_value
import org.jetbrains.compose.resources.stringResource

const val MIN_PERIOD_DAY = 1
const val MAX_PERIOD_DAY = 28

/** Chooses the day of month (1..28) on which the spending period starts. */
@Composable
fun PeriodDayStepper(day: Int, onDayChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FilledTonalIconButton(onClick = { onDayChange(day - 1) }, enabled = day > MIN_PERIOD_DAY) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text(stringResource(Res.string.settings_period_start_value, day), style = MaterialTheme.typography.bodyLarge)
        FilledTonalIconButton(onClick = { onDayChange(day + 1) }, enabled = day < MAX_PERIOD_DAY) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}
