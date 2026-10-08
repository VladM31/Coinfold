package com.vm.coinfold.app.feature.expenses.domain.services

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.models.Currency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpokenExpenseParserTest {
    private fun dec(s: String) = BigDecimal.parseString(s)

    @Test
    fun amountCurrencyAndNote() {
        val r = parseSpokenExpense("150 грн Сільпо")
        assertEquals(dec("150"), r.amount)
        assertEquals(Currency.UAH, r.currency)
        assertEquals("Сільпо", r.note)
    }

    @Test
    fun noteBeforeTheNumberAndDecimalsWithDotOrComma() {
        assertEquals(dec("45.5"), parseSpokenExpense("silpo 45.5 dollars").amount)
        assertEquals(Currency.USD, parseSpokenExpense("silpo 45.5 dollars").currency)
        assertEquals("Silpo", parseSpokenExpense("silpo 45.5 dollars").note)
        assertEquals(dec("45.5"), parseSpokenExpense("кава 45,5").amount)
    }

    @Test
    fun spacesInsideTheNumberAreThousandSeparators() {
        val r = parseSpokenExpense("1 500 грн оренда")
        assertEquals(dec("1500"), r.amount)
        assertEquals("Оренда", r.note)
    }

    @Test
    fun symbolsAndEuroWords() {
        assertEquals(Currency.EUR, parseSpokenExpense("250 євро кава").currency)
        assertEquals(Currency.EUR, parseSpokenExpense("€ 20 taxi").currency)
        assertEquals(Currency.USD, parseSpokenExpense("$ 12 lunch").currency)
    }

    @Test
    fun currencyIsOptionalAndSoIsTheNote() {
        val onlyNumber = parseSpokenExpense("300")
        assertEquals(dec("300"), onlyNumber.amount)
        assertNull(onlyNumber.currency)
        assertEquals("", onlyNumber.note)
    }

    @Test
    fun noNumberKeepsTheWholePhraseAsTheNote() {
        val r = parseSpokenExpense("купив каву")
        assertNull(r.amount)
        assertEquals("Купив каву", r.note)
    }

    @Test
    fun onlyTheFirstNumberIsTheAmount() {
        val r = parseSpokenExpense("100 грн за 2 кави")
        assertEquals(dec("100"), r.amount)
        assertEquals("За 2 кави", r.note)
    }
}
