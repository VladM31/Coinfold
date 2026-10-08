package com.vm.coinfold.app.feature.transactions.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.currency.domain.usecases.ConvertMoneyUseCase
import com.vm.coinfold.app.feature.transactions.domain.models.AddExpenseResult
import com.vm.coinfold.app.feature.transactions.domain.models.NoteSuggestion
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionQuery
import com.vm.coinfold.app.feature.transactions.domain.models.UpdateTransactionResult
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.feature.transactions.domain.usecases.AddExpenseUseCase
import com.vm.coinfold.app.feature.transactions.domain.usecases.UpdateTransactionUseCase
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/** A rate typed by the user for one operation: it is used (and stored) instead of the bank rate. */
class ManualRateTest {
    private fun dec(s: String) = BigDecimal.parseString(s)

    private class Recorder : TransactionRepository {
        var added: Triple<Money, Money, BigDecimal>? = null
        var updated: Triple<Money, Money, BigDecimal>? = null
        override val customIncomeSources: Flow<List<String>> = flowOf(emptyList())
        override val noteSuggestions: Flow<List<NoteSuggestion>> = flowOf(emptyList())
        override suspend fun addManual(
            type: TransactionType, accountId: Long, amount: Money, source: IncomeSource?, note: String, dateTime: Long,
        ) = Unit
        override suspend fun addExpense(
            accountId: Long, categoryId: Long, amount: Money, accountAmount: Money, rate: BigDecimal,
            note: String, dateTime: Long,
        ) { added = Triple(amount, accountAmount, rate) }
        override fun observeItems(query: TransactionQuery, limit: Int): Flow<List<TransactionItem>> = flowOf(emptyList())
        override suspend fun update(
            id: Long, type: TransactionType, accountId: Long, categoryId: Long?, source: IncomeSource?,
            amount: Money, accountAmount: Money, rate: BigDecimal, note: String, dateTime: Long,
        ) { updated = Triple(amount, accountAmount, rate) }
        override suspend fun delete(id: Long) = Unit
        override suspend fun undoDelete() = Unit
        override suspend fun lastExpenseCategoryId(): Long? = null
        override suspend fun duplicate(id: Long, dateTime: Long) = true
    }

    private class Currencies(table: RateTable) : CurrencyRepository {
        override val rateTable: Flow<RateTable> = flowOf(table)
        override val updatedAt: Flow<Long?> = flowOf(null)
        override suspend fun refresh() = false
    }

    private val bankRates = RateTable(mapOf((Currency.USD to Currency.UAH) to dec("40")))

    @Test
    fun manualRateWorksEvenWhenNoBankRateWasEverLoaded() = runTest {
        val repo = Recorder()
        val useCase = AddExpenseUseCase(repo, ConvertMoneyUseCase(Currencies(RateTable.Empty)))

        // without the override the cross-currency expense is refused
        assertEquals(
            AddExpenseResult.NO_RATE,
            useCase(1, Currency.UAH, 5, Money(1_000, Currency.USD), "", 0L),
        )
        val result = useCase(1, Currency.UAH, 5, Money(1_000, Currency.USD), "", 0L, rateOverride = dec("41.5"))

        assertEquals(AddExpenseResult.SUCCESS, result)
        assertEquals(Money(41_500, Currency.UAH), repo.added?.second) // 10.00 USD * 41.5
        assertEquals(dec("41.5"), repo.added?.third)
    }

    @Test
    fun manualRateBeatsTheBankRate() = runTest {
        val repo = Recorder()
        val useCase = AddExpenseUseCase(repo, ConvertMoneyUseCase(Currencies(bankRates)))

        useCase(1, Currency.UAH, 5, Money(1_000, Currency.USD), "", 0L, rateOverride = dec("38"))

        assertEquals(Money(38_000, Currency.UAH), repo.added?.second)
    }

    @Test
    fun manualRateIsIgnoredWhenTheCurrenciesAreTheSame() = runTest {
        val repo = Recorder()
        val useCase = AddExpenseUseCase(repo, ConvertMoneyUseCase(Currencies(bankRates)))

        useCase(1, Currency.UAH, 5, Money(1_000, Currency.UAH), "", 0L, rateOverride = dec("99"))

        assertEquals(Money(1_000, Currency.UAH), repo.added?.second)
        assertEquals(BigDecimal.ONE, repo.added?.third)
    }

    @Test
    fun editingWithAManualRateReplacesTheStoredOne() = runTest {
        val repo = Recorder()
        val useCase = UpdateTransactionUseCase(repo, ConvertMoneyUseCase(Currencies(bankRates)))
        val original = TransactionItem(
            id = 1, type = TransactionType.EXPENSE, accountId = 1, accountName = "Card",
            accountCurrency = Currency.UAH, category = null, source = null, note = "",
            amount = Money(1_000, Currency.USD), accountAmount = Money(38_000, Currency.UAH),
            rate = dec("38"), dateTime = 0L,
        )

        val result = useCase(
            original, 1, Currency.UAH, null, null, Money(1_000, Currency.USD), "", 0L, rateOverride = dec("39.25"),
        )

        assertEquals(UpdateTransactionResult.SUCCESS, result)
        assertEquals(Money(39_250, Currency.UAH), repo.updated?.second)
        assertEquals(dec("39.25"), repo.updated?.third)
    }
}
