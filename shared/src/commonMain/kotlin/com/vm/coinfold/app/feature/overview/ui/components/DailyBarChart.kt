package com.vm.coinfold.app.feature.overview.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vm.coinfold.app.feature.overview.domain.models.DayBar
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.utils.format

/** Spending per day as stacked bars colored by category, with the scale at the right edge. */
@Composable
fun DailyBarChart(bars: List<DayBar>, modifier: Modifier = Modifier) {
    if (bars.isEmpty()) return
    val language = LocalAppLanguage.current
    val max = bars.maxOf { it.total.minorUnits }
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val otherColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val currency = bars.first().total.currency

    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(160.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                // Baseline, half and top gridlines.
                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = size.height * (1 - fraction)
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }
                if (max <= 0) return@Canvas
                val slot = size.width / bars.size
                val barWidth = slot * 0.7f
                bars.forEachIndexed { index, bar ->
                    var bottom = size.height
                    val x = index * slot + (slot - barWidth) / 2
                    bar.segments.forEach { segment ->
                        val height = size.height * segment.amount.minorUnits.toFloat() / max
                        val top = bottom - height
                        drawRect(
                            color = segment.color?.let { Color(it) } ?: otherColor,
                            topLeft = Offset(x, top),
                            size = Size(barWidth, height),
                        )
                        bottom = top
                    }
                }
            }
            if (max > 0) {
                Text(
                    Money(max, currency).format(language),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
                Text(
                    Money(max / 2, currency).format(language),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(bars.first(), bars[bars.size / 2], bars.last()).forEach { bar ->
                Text(
                    // dd.MM
                    bar.date.let { "${it.day.toString().padStart(2, '0')}.${it.month.ordinal.plus(1).toString().padStart(2, '0')}" },
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
        }
    }
}
