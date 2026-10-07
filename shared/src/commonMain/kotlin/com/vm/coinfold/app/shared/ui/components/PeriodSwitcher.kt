package com.vm.coinfold.app.shared.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.utils.format

/** Shows a period as a date range with buttons to move to the previous/next one. */
@Composable
fun PeriodSwitcher(
    period: Period,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onPrevious) { Text("‹", style = MaterialTheme.typography.titleLarge) }
        Text(
            text = "${period.start.format()} – ${period.endInclusive.format()}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onNext) { Text("›", style = MaterialTheme.typography.titleLarge) }
    }
}
