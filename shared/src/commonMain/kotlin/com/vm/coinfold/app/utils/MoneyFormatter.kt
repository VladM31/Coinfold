package com.vm.coinfold.app.utils

import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.ResolvedLanguage

private const val NBSP = ' '

/**
 * UK: `1 234,56 ₴` (symbol after, comma decimal, non-breaking space grouping).
 * EN: `₴1,234.56` (symbol before). A minus sign always comes first.
 */
fun Money.format(language: ResolvedLanguage, showPlus: Boolean = false): String {
    var divisor = 1L
    repeat(currency.fractionDigits) { divisor *= 10 }
    // Work on the unsigned value; Long.MIN_VALUE is not a realistic balance.
    val abs = if (minorUnits < 0) -minorUnits else minorUnits
    val whole = (abs / divisor).toString()
    val fraction = (abs % divisor).toString().padStart(currency.fractionDigits, '0')

    val groupSeparator = if (language == ResolvedLanguage.UK) NBSP else ','
    val decimalSeparator = if (language == ResolvedLanguage.UK) ',' else '.'
    val grouped = whole.reversed().chunked(3).joinToString(groupSeparator.toString()).reversed()
    val number = if (currency.fractionDigits > 0) "$grouped$decimalSeparator$fraction" else grouped

    val sign = when {
        minorUnits < 0 -> "-"
        showPlus && minorUnits > 0 -> "+"
        else -> ""
    }
    return when (language) {
        ResolvedLanguage.UK -> "$sign$number$NBSP${currency.symbol}"
        ResolvedLanguage.EN -> "$sign${currency.symbol}$number"
    }
}
