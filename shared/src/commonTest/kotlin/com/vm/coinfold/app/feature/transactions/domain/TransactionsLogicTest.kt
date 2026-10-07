package com.vm.coinfold.app.feature.transactions.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.currency.domain.usecases.ConvertMoneyUseCase
import com.vm.coinfold.app.feature.transactions.domain.models.NoteSuggestion
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionQuery
import com.vm.coinfold.app.feature.transactions.domain.models.UpdateTransactionResult
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.feature.transactions.domain.services.groupByDay
import com.vm.coinfold.app.feature.transactions.domain.usecases.UpdateTransactionUseCase
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalTime::class)
class TransactionsLogicTest {
    private fun dec(s: String) = BigDecimal.parseString(s)
    private val rates = RateTable(mapOf((Currency.USD to Currency.UAH) to dec("40")))

    private fun item(
        id: Long,
        type: TransactionType,
        amount: Money,
        at: String,
        accountCurrency: Currency = Currency.UAH,
        rate: BigDecimal = BigDecimal.ONE,
    ) = TransactionItem(
        id = id,
        type = type,
        accountId = 1,
        accountName = "Main",
        accountCurrency = accountCurrency,
        category = null,
        source = null,
        note = "",
        amount = amount,
        accountAmount = amount.convertTo(accountCurrency, rate),
        rate = rate,
        dateTime = Instant.parse(at).toEpochMilliseconds(),
    )

    @Test
    fun groupsByDayWithSubtotalInMainCurrency() {
        val items = listOf(
            item(3, TransactionType.EXPENSE, Money(100, Currency.USD), "2026-03-02T10:00:00Z"), // -40.00 UAH
            item(2, TransactionType.INCOME, Money(10_000, Currency.UAH), "2026-03-02T09:00:00Z"),
            item(1, TransactionType.EXPENSE, Money(500, Currency.UAH), "2026-03-01T09:00:00Z"),
        )
        val groups = groupByDay(items, rates, Currency.UAH, TimeZone.UTC)

        assertEquals(listOf(LocalDate(2026, 3, 2), LocalDate(2026, 3, 1)), groups.map { it.date })
        assertEquals(Money(6_000, Currency.UAH), groups[0].total)
        assertEquals(Money(-500, Currency.UAH), groups[1].total)
    }

    @Test
    fun missingRateIsFlaggedPerDay() {
        val items = listOf(item(1, TransactionType.EXPENSE, Money(100, Currency.EUR), "2026-03-02T10:00:00Z"))
        val group = groupByDay(items, rates, Currency.UAH, TimeZone.UTC).single()
        assertTrue(group.hasMissingRates)
    }

    private class FakeRepository : TransactionRepository {
        var updated: Triple<Money, Money, BigDecimal>? = null
        override val customIncomeSources: Flow<List<String>> = MutableStateFlow(emptyList())
        override val noteSuggestions: Flow<List<NoteSuggestion>> = flowOf(emptyList())
        override suspend fun addManual(
            type: TransactionType, accountId: Long, amount: Money, source: IncomeSource?, note: String, dateTime: Long,
        ) = Unit
        override suspend fun addExpense(
            accountId: Long, categoryId: Long, amount: Money, accountAmount: Money, rate: BigDecimal,
            note: String, dateTime: Long,
        ) = Unit
        override fun observeItems(query: TransactionQuery, limit: Int): Flow<List<TransactionItem>> = flowOf(emptyList())
        override suspend fun update(
            id: Long, type: TransactionType, accountId: Long, categoryId: Long?, source: IncomeSource?,
            amount: Money, accountAmount: Money, rate: BigDecimal, note: String, dateTime: Long,
        ) {
            updated = Triple(amount, accountAmount, rate)
        }
        override suspend fun delete(id: Long) = Unit
        override suspend fun undoDelete() = Unit
        override suspend fun duplicate(id: Long, dateTime: Long) = true
    }

    private class FakeCurrencies(table: RateTable) : CurrencyRepository {
        override val rateTable: Flow<RateTable> = flowOf(table)
        override val updatedAt: Flow<Long?> = flowOf(null)
        override suspend fun refresh() = false
    }

    @Test
    fun editKeepsStoredRateWhenCurrencyPairIsUnchanged() = runTest {
        val repo = FakeRepository()
        // Today's rate is 40, but this expense was made at 38 and must stay at 38.
        val useCase = UpdateTransactionUseCase(repo, ConvertMoneyUseCase(FakeCurrencies(rates)))
        val original = item(
            1, TransactionType.EXPENSE, Money(100, Currency.USD), "2026-03-02T10:00:00Z",
            accountCurrency = Currency.UAH, rate = dec("38"),
        )

        val result = useCase(original, 1, Currency.UAH, null, null, Money(200, Currency.USD), "", original.dateTime)

        assertEquals(UpdateTransactionResult.SUCCESS, result)
        assertEquals(Money(7_600, Currency.UAH), repo.updated?.second)
        assertEquals(dec("38"), repo.updated?.third)
    }

    @Test
    fun editWithNewCurrencyPairUsesCurrentRateOrFails() = runTest {
        val repo = FakeRepository()
        val original = item(1, TransactionType.EXPENSE, Money(1_000, Currency.UAH), "2026-03-02T10:00:00Z")

        val withRate = UpdateTransactionUseCase(repo, ConvertMoneyUseCase(FakeCurrencies(rates)))
        assertEquals(
            UpdateTransactionResult.SUCCESS,
            withRate(original, 1, Currency.UAH, null, null, Money(100, Currency.USD), "", original.dateTime),
        )
        assertEquals(Money(4_000, Currency.UAH), repo.updated?.second)

        repo.updated = null
        val withoutRate = UpdateTransactionUseCase(repo, ConvertMoneyUseCase(FakeCurrencies(RateTable.Empty)))
        assertEquals(
            UpdateTransactionResult.NO_RATE,
            withoutRate(original, 1, Currency.UAH, null, null, Money(100, Currency.USD), "", original.dateTime),
        )
        assertEquals(null, repo.updated)
    }
}
