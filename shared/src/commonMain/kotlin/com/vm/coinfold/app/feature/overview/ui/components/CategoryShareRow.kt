package com.vm.coinfold.app.feature.overview.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.overview_other
import com.vm.coinfold.app.feature.overview.domain.models.CategoryShare
import com.vm.coinfold.app.shared.ui.components.CategoryBadge
import com.vm.coinfold.app.shared.ui.components.CategoryIconCatalog
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

/** One category: badge, name, amount and a bar showing its percent of all spending. */
@Composable
fun CategoryShareRow(share: CategoryShare, modifier: Modifier = Modifier) {
    val language = LocalAppLanguage.current
    val category = share.category
    val color = category?.let { Color(it.color) } ?: MaterialTheme.colorScheme.outline

    Row(
        modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CategoryBadge(category?.icon ?: CategoryIconCatalog.stored("other"), color, size = 44)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    category?.name ?: stringResource(Res.string.overview_other),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(share.spent.format(language), style = MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { share.percent / 100f },
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${share.percent}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                )
                share.changePercent?.let { ChangeLabel(it) }
            }
        }
    }
}

/** "▲ 12%" in red (more spending than before) or "▼ 8%" in green (less); nothing when unchanged. */
@Composable
private fun ChangeLabel(change: Int) {
    if (change == 0) return
    Text(
        (if (change > 0) "▲ " else "▼ ") + "${kotlin.math.abs(change)}%",
        style = MaterialTheme.typography.labelSmall,
        color = if (change > 0) MaterialTheme.colorScheme.error else IncomeGreen,
    )
}
