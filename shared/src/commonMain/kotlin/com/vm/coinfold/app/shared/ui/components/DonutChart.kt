package com.vm.coinfold.app.shared.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class DonutSegment(val value: Long, val color: Color)

/** Ring chart; segments with non-positive values are skipped. [center] is drawn inside the ring. */
@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    emptyColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 36f,
    center: @Composable () -> Unit,
) {
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize().padding(strokeWidth.dp / 4)) {
            val stroke = Stroke(width = strokeWidth)
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)
            val visible = segments.filter { it.value > 0 }
            val total = visible.sumOf { it.value }
            if (total <= 0) {
                drawArc(emptyColor, 0f, 360f, false, topLeft, arcSize, style = stroke)
            } else {
                var start = -90f
                for (segment in visible) {
                    val sweep = 360f * segment.value / total
                    drawArc(segment.color, start, sweep, false, topLeft, arcSize, style = stroke)
                    start += sweep
                }
            }
        }
        center()
    }
}
