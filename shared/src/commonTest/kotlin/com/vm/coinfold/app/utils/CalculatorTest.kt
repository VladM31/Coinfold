package com.vm.coinfold.app.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalculatorTest {
    private fun eval(s: String) = Calculator.evaluate(s)
    private fun dec(s: String) = BigDecimal.parseString(s)

    @Test
    fun respectsPrecedence() {
        assertEquals(dec("14"), eval("2+3×4"))
        assertEquals(dec("5"), eval("20÷4+0"))
        assertEquals(dec("-3"), eval("2−5"))
    }

    @Test
    fun decimalsAreExact() {
        assertEquals(dec("0.3"), eval("0.1+0.2"))
        assertEquals(dec("1.5"), eval("3÷2"))
    }

    @Test
    fun divisionByZeroAndGarbageAreNull() {
        assertNull(eval("5÷0"))
        assertNull(eval("5+"))
        assertNull(eval(""))
        assertNull(eval("1.2.3"))
        assertNull(eval("×5"))
    }

    @Test
    fun leadingMinusAndTrailingDot() {
        assertEquals(dec("-2"), eval("-5+3"))
        assertEquals(dec("5"), eval("5."))
    }

    @Test
    fun inputRules() {
        assertEquals("0.", CalculatorInput.append("", '.'))
        assertEquals("1.5", CalculatorInput.append("1.", '5'))
        assertEquals("1.5", CalculatorInput.append("1.5", '.'))
        assertEquals("", CalculatorInput.append("", OP_PLUS))
        assertEquals("−", CalculatorInput.append("", OP_MINUS))
        assertEquals("5×", CalculatorInput.append("5+", OP_TIMES))
        assertEquals("5+0.", CalculatorInput.append("5+", '.'))
        assertEquals("5", CalculatorInput.backspace("5+"))
    }
}
