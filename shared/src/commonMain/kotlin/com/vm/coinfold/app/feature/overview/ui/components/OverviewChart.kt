package com.vm.coinfold.app.feature.overview.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.overview_chart_daily
import coinfold.shared.generated.resources.overview_chart_trend
import com.vm.coinfold.app.feature.overview.domain.models.OverviewSummary
import org.jetbrains.compose.resources.stringResource

private enum class ChartMode { DAILY, TREND }

/** One chart slot with a switch between spending per day and income/spending over the last periods. */
@Composable
fun OverviewChart(summary: OverviewSummary, modifier: Modifier = Modifier) {
    var mode by rememberSaveable { mutableStateOf(ChartMode.DAILY) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == ChartMode.DAILY,
                onClick = { mode = ChartMode.DAILY },
                label = { Text(stringResource(Res.string.overview_chart_daily)) },
            )
            FilterChip(
                selected = mode == ChartMode.TREND,
                onClick = { mode = ChartMode.TREND },
                label = { Text(stringResource(Res.string.overview_chart_trend)) },
            )
        }
        when (mode) {
            ChartMode.DAILY -> DailyBarChart(summary.bars)
            ChartMode.TREND -> MonthlyTrendChart(summary.trend)
        }
    }
}
