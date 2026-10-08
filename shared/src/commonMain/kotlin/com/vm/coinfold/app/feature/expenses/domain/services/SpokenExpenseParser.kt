package com.vm.coinfold.app.feature.expenses.domain.services

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.models.Currency

/** What was understood from a spoken sentence; any part may be missing. */
data class SpokenExpense(
    val amount: BigDecimal?,
    val currency: Currency?,
    val note: String,
)

private val NUMBER = Regex("""\d[\d ]*(?:[.,]\d{1,2})?""")

private val CURRENCY_WORDS: Map<String, Currency> = buildMap {
    listOf("грн", "гривня", "гривні", "гривень", "гривню", "₴", "uah", "hryvnia", "hryvnias", "hryvnya")
        .forEach { put(it, Currency.UAH) }
    listOf("$", "usd", "долар", "долара", "доларів", "доллар", "доллара", "долларов", "dollar", "dollars", "бакс", "баксів")
        .forEach { put(it, Currency.USD) }
    listOf("€", "eur", "євро", "евро", "euro", "euros")
        .forEach { put(it, Currency.EUR) }
}

/**
 * Turns a phrase like "150 грн Сільпо", "silpo 45.5 dollars" or "250 євро кава" into an amount, a currency and
 * a note. Speech recognizers write numbers as digits, so only digits are understood (not "сто п'ятдесят").
 * The first number is the amount; a currency word right next to it (before or after) sets the currency; all the
 * other words become the note.
 */
fun parseSpokenExpense(text: String): SpokenExpense {
    val cleaned = text.trim()
    val match = NUMBER.find(cleaned) ?: return SpokenExpense(null, findCurrency(cleaned), cleaned.withoutCurrencyWords())

    val amountText = match.value.trim().replace(" ", "").replace(',', '.')
    val amount = runCatching { BigDecimal.parseString(amountText) }.getOrNull()

    val before = cleaned.substring(0, match.range.first)
    val after = cleaned.substring(match.range.last + 1)
    val currency = nearestCurrency(before, after) ?: findCurrency(cleaned)
    val note = (before + " " + after).withoutCurrencyWords()
    return SpokenExpense(amount, currency, note)
}

/** A currency word directly after the number wins, then the word directly before it. */
private fun nearestCurrency(before: String, after: String): Currency? {
    val next = after.trim().split(' ').firstOrNull()?.normalizeWord()
    val previous = before.trim().split(' ').lastOrNull()?.normalizeWord()
    return CURRENCY_WORDS[next] ?: CURRENCY_WORDS[previous]
}

private fun findCurrency(text: String): Currency? =
    text.split(' ').firstNotNullOfOrNull { CURRENCY_WORDS[it.normalizeWord()] }

private fun String.withoutCurrencyWords(): String =
    split(' ')
        .filter { it.isNotBlank() && it.normalizeWord() !in CURRENCY_WORDS }
        .joinToString(" ")
        .trim()
        .replaceFirstChar { it.uppercase() }

private fun String.normalizeWord(): String = lowercase().trim(',', '.', '!', '?', ';', ':')
