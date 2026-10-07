package com.vm.coinfold.app.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class NavIconKind { ACCOUNTS, EXPENSES, TRANSACTIONS, SETTINGS }

/**
 * Bottom navigation icons drawn on a 24x24 grid with the same stroke width, so all four have
 * identical visual size regardless of the platform font.
 */
@Composable
fun NavIcon(kind: NavIconKind, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val unit = size.minDimension / 24f
        val stroke = Stroke(width = 2f * unit, cap = StrokeCap.Round)
        when (kind) {
            NavIconKind.ACCOUNTS -> drawWallet(tint, unit, stroke)
            NavIconKind.EXPENSES -> drawRing(tint, unit, stroke)
            NavIconKind.TRANSACTIONS -> drawList(tint, unit, stroke)
            NavIconKind.SETTINGS -> drawGear(tint, unit, stroke)
        }
    }
}

private fun DrawScope.drawWallet(tint: Color, u: Float, stroke: Stroke) {
    drawRoundRect(
        tint,
        topLeft = Offset(3f * u, 5f * u),
        size = Size(18f * u, 14f * u),
        cornerRadius = CornerRadius(3f * u),
        style = stroke,
    )
    drawLine(tint, Offset(3f * u, 9f * u), Offset(21f * u, 9f * u), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawCircle(tint, radius = 1.4f * u, center = Offset(16.5f * u, 14f * u))
}

private fun DrawScope.drawRing(tint: Color, u: Float, stroke: Stroke) {
    // A donut with a gap, echoing the chart on the main screen.
    drawArc(
        tint,
        startAngle = -60f,
        sweepAngle = 300f,
        useCenter = false,
        topLeft = Offset(3f * u, 3f * u),
        size = Size(18f * u, 18f * u),
        style = stroke,
    )
    drawCircle(tint, radius = 1.6f * u, center = Offset(12f * u, 12f * u))
}

private fun DrawScope.drawList(tint: Color, u: Float, stroke: Stroke) {
    for (row in 0..2) {
        val y = (6f + row * 6f) * u
        drawCircle(tint, radius = 1.3f * u, center = Offset(4.5f * u, y))
        drawLine(tint, Offset(9f * u, y), Offset(20f * u, y), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawGear(tint: Color, u: Float, stroke: Stroke) {
    val center = Offset(12f * u, 12f * u)
    drawCircle(tint, radius = 3.4f * u, center = center, style = stroke)
    drawCircle(tint, radius = 7f * u, center = center, style = stroke)
    for (i in 0 until 8) {
        val angle = i * PI / 4
        val from = Offset(center.x + (7f * u) * cos(angle).toFloat(), center.y + (7f * u) * sin(angle).toFloat())
        val to = Offset(center.x + (10f * u) * cos(angle).toFloat(), center.y + (10f * u) * sin(angle).toFloat())
        drawLine(tint, from, to, strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
