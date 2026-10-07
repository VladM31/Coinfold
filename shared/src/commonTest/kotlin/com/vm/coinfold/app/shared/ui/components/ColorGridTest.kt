package com.vm.coinfold.app.shared.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorGridTest {
    @Test
    fun paletteHas160OpaqueColors() {
        assertEquals(160, ExtendedColorPalette.size)
        assertTrue(ExtendedColorPalette.all { it ushr 24 == 0xFFL })
    }

    @Test
    fun colorsAreMostlyDistinct() {
        // Rounding may merge a couple of near-identical tones, but the palette must stay rich.
        assertTrue(ExtendedColorPalette.toSet().size >= 150)
    }

    @Test
    fun firstRowIsNeutralAndPureRedFamilyFollows() {
        // Neutral row: all channels are close to each other.
        ExtendedColorPalette.take(10).forEach { c ->
            val r = (c shr 16) and 0xFF
            val b = c and 0xFF
            assertTrue(kotlin.math.abs(r - b) < 40, "gray row color $c is not neutral")
        }
        // First chromatic tone of hue 0 is a dark red: red channel dominates.
        val c = ExtendedColorPalette[10]
        assertTrue(((c shr 16) and 0xFF) > ((c shr 8) and 0xFF))
    }
}
