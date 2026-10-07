package com.vm.coinfold.app.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

private const val COLUMNS = 10
private const val HUES = 15

/** (saturation, value) of the ten tones of every hue, from dark to light. */
private val TONES = listOf(
    0.90f to 0.45f, 0.90f to 0.58f, 0.90f to 0.70f, 0.90f to 0.82f, 0.90f to 0.95f,
    0.70f to 0.95f, 0.55f to 0.88f, 0.45f to 0.78f, 0.40f to 0.62f, 0.30f to 0.50f,
)

/** Brightness steps of the neutral (gray) row; all dark enough for a white icon on top. */
private val GRAYS = listOf(0.12f, 0.20f, 0.28f, 0.36f, 0.44f, 0.52f, 0.58f, 0.64f, 0.68f, 0.72f)

private fun hsvToArgb(hue: Float, saturation: Float, value: Float): Long {
    val c = value * saturation
    val x = c * (1 - abs((hue / 60f) % 2 - 1))
    val m = value - c
    val (r, g, b) = when {
        hue < 60 -> Triple(c, x, 0f)
        hue < 120 -> Triple(x, c, 0f)
        hue < 180 -> Triple(0f, c, x)
        hue < 240 -> Triple(0f, x, c)
        hue < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    fun channel(v: Float) = ((v + m) * 255f).roundToInt().toLong().coerceIn(0, 255)
    return 0xFF000000L or (channel(r) shl 16) or (channel(g) shl 8) or channel(b)
}

/**
 * 160 colors for categories: a row of ten grays followed by 15 hues with ten tones each, so the grid is
 * 16 rows of 10 and every row is one color family.
 */
val ExtendedColorPalette: List<Long> by lazy {
    val grays = GRAYS.map { hsvToArgb(220f, 0.08f, it) }
    val hues = (0 until HUES).flatMap { index ->
        val hue = index * (360f / HUES)
        TONES.map { (s, v) -> hsvToArgb(hue, s, v) }
    }
    grays + hues
}

/** Grid of color dots, [COLUMNS] per row; tapping the selected one keeps it selected. */
@Composable
fun ColorGrid(
    colors: List<Long>,
    selected: Long?,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        colors.chunked(COLUMNS).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { value ->
                    val isSelected = value == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(Color(value))
                            .then(
                                if (isSelected) {
                                    Modifier.border(BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface), CircleShape)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { onSelected(value) },
                    )
                }
                // Keep the last row aligned if it is shorter than the others.
                repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
