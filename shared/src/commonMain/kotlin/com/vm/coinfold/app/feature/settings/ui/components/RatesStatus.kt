package com.vm.coinfold.app.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.settings_rates_never
import coinfold.shared.generated.resources.settings_rates_refresh
import coinfold.shared.generated.resources.settings_rates_updated_at
import com.vm.coinfold.app.utils.formatDateTime
import org.jetbrains.compose.resources.stringResource

/** When the exchange rates were last updated, plus a manual refresh button. */
@Composable
fun RatesStatus(
    updatedAt: Long?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (updatedAt == null) {
                stringResource(Res.string.settings_rates_never)
            } else {
                stringResource(Res.string.settings_rates_updated_at, formatDateTime(updatedAt))
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        OutlinedButton(onClick = onRefresh, enabled = !isRefreshing) {
            Text(stringResource(Res.string.settings_rates_refresh))
        }
    }
}
