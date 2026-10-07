package com.vm.coinfold.app.feature.accounts.domain.services

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.Account
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TotalBalanceTest {
    private fun account(id: Long, balance: Money) = AccountWithBalance(
        Account(id, "A$id", balance.currency, balance, null, null),
        balance,
        transactionCount = 0,
    )

    private val rates = RateTable(mapOf((Currency.USD to Currency.UAH) to BigDecimal.parseString("40")))

    @Test
    fun sumsAccountsInTargetCurrency() {
        val total = calculateTotalBalance(
            listOf(account(1, Money(10_000, Currency.UAH)), account(2, Money(100, Currency.USD))),
            Currency.UAH,
            rates,
        )
        // 100.00 UAH + 1.00 USD * 40
        assertEquals(Money(14_000, Currency.UAH), total.money)
        assertFalse(total.hasMissingRates)
    }

    @Test
    fun accountsWithoutRateAreSkippedAndFlagged() {
        val total = calculateTotalBalance(
            listOf(account(1, Money(10_000, Currency.UAH)), account(2, Money(100, Currency.EUR))),
            Currency.UAH,
            rates,
        )
        assertEquals(Money(10_000, Currency.UAH), total.money)
        assertTrue(total.hasMissingRates)
    }

    @Test
    fun incomeSourceRoundTrips() {
        assertEquals(IncomeSource.Preset.GIFT, IncomeSource.fromStored(IncomeSource.Preset.GIFT.toStored()))
        assertEquals(IncomeSource.Custom("Cashback"), IncomeSource.fromStored("Cashback"))
        assertEquals(IncomeSource.Preset.OTHER, IncomeSource.fromStored("preset:unknown"))
    }
}
