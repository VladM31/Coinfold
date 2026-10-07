package com.vm.coinfold.app.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

const val OP_PLUS = '+'
const val OP_MINUS = '−'
const val OP_TIMES = '×'
const val OP_DIVIDE = '÷'

private val OPERATORS = setOf(OP_PLUS, OP_MINUS, OP_TIMES, OP_DIVIDE)
private const val MAX_LENGTH = 32

/** Rules for typing into the mini-calculator, so the expression is always well-formed. */
object CalculatorInput {

    fun append(expression: String, key: Char): String = when {
        expression.length >= MAX_LENGTH -> expression
        key.isDigit() -> expression + key
        key == '.' -> appendDot(expression)
        key in OPERATORS -> appendOperator(expression, key)
        else -> expression
    }

    fun backspace(expression: String): String = expression.dropLast(1)

    private fun appendDot(expression: String): String {
        val currentNumber = expression.takeLastWhile { it.isDigit() || it == '.' }
        return when {
            '.' in currentNumber -> expression
            currentNumber.isEmpty() -> "${expression}0."
            else -> "$expression."
        }
    }

    private fun appendOperator(expression: String, op: Char): String {
        if (expression.isEmpty()) return if (op == OP_MINUS) "$op" else expression
        val last = expression.last()
        if (last in OPERATORS) {
            // Replace the previous operator; a lone leading minus cannot be replaced.
            val without = expression.dropLast(1)
            return if (without.isEmpty()) expression else without + op
        }
        return expression + op
    }
}

/** Evaluates `+ − × ÷` expressions (no parentheses) on BigDecimal; null if invalid or dividing by zero. */
object Calculator {
    private val DIVISION = DecimalMode(24, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO, 10)

    fun evaluate(expression: String): BigDecimal? = Parser(expression, DIVISION).parse()

    private class Parser(private val s: String, private val mode: DecimalMode) {
        private var i = 0

        fun parse(): BigDecimal? {
            val value = expression() ?: return null
            return if (i == s.length) value else null
        }

        private fun expression(): BigDecimal? {
            var left = term() ?: return null
            while (i < s.length && (s[i] == OP_PLUS || s[i] == OP_MINUS || s[i] == '-')) {
                val op = s[i++]
                val right = term() ?: return null
                left = if (op == OP_PLUS) left + right else left - right
            }
            return left
        }

        private fun term(): BigDecimal? {
            var left = factor() ?: return null
            while (i < s.length && (s[i] == OP_TIMES || s[i] == OP_DIVIDE || s[i] == '*' || s[i] == '/')) {
                val op = s[i++]
                val right = factor() ?: return null
                left = if (op == OP_TIMES || op == '*') {
                    left.multiply(right)
                } else {
                    if (right.isZero()) return null
                    left.divide(right, mode)
                }
            }
            return left
        }

        private fun factor(): BigDecimal? {
            if (i < s.length && (s[i] == OP_MINUS || s[i] == '-')) {
                i++
                return factor()?.negate()
            }
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            if (start == i) return null
            var text = s.substring(start, i)
            if (text.startsWith('.')) text = "0$text"
            if (text.endsWith('.')) text += "0"
            return runCatching { BigDecimal.parseString(text) }.getOrNull()
        }
    }
}
