package com.vm.coinfold.app.feature.overview.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.overview_trend_expenses
import coinfold.shared.generated.resources.overview_trend_income
import com.vm.coinfold.app.feature.overview.domain.models.MonthlyPoint
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

/**
 * Income (green) and spending (violet) of the last periods as pairs of bars, oldest first. The newest
 * period, the one selected on the screen, is drawn at full strength and the older ones a little lighter.
 */
@Composable
fun MonthlyTrendChart(points: List<MonthlyPoint>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return
    val language = LocalAppLanguage.current
    val max = points.maxOf { maxOf(it.income.minorUnits, it.spent.minorUnits) }
    val currency = points.first().income.currency
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val spentColor = MaterialTheme.colorScheme.primary

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Legend(IncomeGreen, stringResource(Res.string.overview_trend_income))
            Legend(spentColor, stringResource(Res.string.overview_trend_expenses))
        }
        Box(Modifier.fillMaxWidth().height(160.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = size.height * (1 - fraction)
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }
                if (max <= 0) return@Canvas
                val slot = size.width / points.size
                val barWidth = slot * 0.36f
                points.forEachIndexed { index, point ->
                    val alpha = if (index == points.lastIndex) 1f else 0.55f
                    val left = index * slot + slot * 0.1f
                    listOf(point.income.minorUnits to IncomeGreen, point.spent.minorUnits to spentColor)
                        .forEachIndexed { i, (value, color) ->
                            val height = size.height * value.toFloat() / max
                            drawRoundRect(
                                color = color.copy(alpha = alpha),
                                topLeft = Offset(left + i * (barWidth + slot * 0.04f), size.height - height),
                                size = Size(barWidth, height),
                                cornerRadius = CornerRadius(3f, 3f),
                            )
                        }
                }
            }
            if (max > 0) {
                Text(
                    Money(max, currency).format(language),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(points.first(), points[points.size / 2], points.last()).forEach { point ->
                val start = point.periodStart
                Text(
                    // MM.yy
                    "${start.month.ordinal.plus(1).toString().padStart(2, '0')}.${(start.year % 100).toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
