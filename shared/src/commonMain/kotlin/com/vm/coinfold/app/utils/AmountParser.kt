package com.vm.coinfold.app.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal

private val POSITIVE = Regex("""\d+([.,]\d{1,2})?""")
private val SIGNED = Regex("""-?\d+([.,]\d{1,2})?""")

/**
 * Parses user input like `1 234,56` or `12.5` (max 2 decimals); returns null for anything invalid.
 * With [allowNegative] a leading minus is accepted (e.g. initial balance of a credit card).
 */
fun parseAmount(text: String, allowNegative: Boolean = false): BigDecimal? {
    val cleaned = text.trim().filterNot { it.isWhitespace() }
    val regex = if (allowNegative) SIGNED else POSITIVE
    if (!regex.matches(cleaned)) return null
    return BigDecimal.parseString(cleaned.replace(',', '.'))
}
