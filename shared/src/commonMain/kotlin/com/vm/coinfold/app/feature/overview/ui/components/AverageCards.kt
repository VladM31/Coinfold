package com.vm.coinfold.app.feature.overview.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.overview_day_avg
import coinfold.shared.generated.resources.overview_month_avg
import coinfold.shared.generated.resources.overview_week_avg
import com.vm.coinfold.app.feature.overview.domain.models.OverviewSummary
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

/** Average spending per day, week and month of the period. */
@Composable
fun AverageCards(summary: OverviewSummary, modifier: Modifier = Modifier) {
    val language = LocalAppLanguage.current
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            stringResource(Res.string.overview_day_avg) to summary.dayAverage,
            stringResource(Res.string.overview_week_avg) to summary.weekAverage,
            stringResource(Res.string.overview_month_avg) to summary.monthAverage,
        ).forEach { (label, value) ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                    Text(
                        value.format(language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
