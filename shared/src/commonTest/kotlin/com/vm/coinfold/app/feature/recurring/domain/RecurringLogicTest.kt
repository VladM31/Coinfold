package com.vm.coinfold.app.feature.recurring.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.currency.domain.usecases.ConvertMoneyUseCase
import com.vm.coinfold.app.feature.recurring.domain.models.Frequency
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringDraft
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringPayment
import com.vm.coinfold.app.feature.recurring.domain.repositories.RecurringRepository
import com.vm.coinfold.app.feature.recurring.domain.services.nextOccurrence
import com.vm.coinfold.app.feature.recurring.domain.usecases.ProcessRecurringPaymentsUseCase
import com.vm.coinfold.app.feature.transactions.domain.models.NoteSuggestion
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionCategory
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionQuery
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.feature.transactions.domain.usecases.AddExpenseUseCase
import com.vm.coinfold.app.feature.transactions.domain.usecases.AddManualTransactionUseCase
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class RecurringLogicTest {

    // ---------- next date ----------

    @Test
    fun simpleFrequencies() {
        val d = LocalDate(2026, 3, 10)
        assertEquals(LocalDate(2026, 3, 11), nextOccurrence(d, Frequency.DAILY, 10))
        assertEquals(LocalDate(2026, 3, 17), nextOccurrence(d, Frequency.WEEKLY, 10))
        assertEquals(LocalDate(2026, 4, 10), nextOccurrence(d, Frequency.MONTHLY, 10))
        assertEquals(LocalDate(2027, 3, 10), nextOccurrence(d, Frequency.YEARLY, 10))
    }

    @Test
    fun monthlyOnThe31stUsesTheLastDayOfShortMonthsWithoutDrifting() {
        var date = LocalDate(2026, 1, 31)
        val seen = mutableListOf<LocalDate>()
        repeat(4) {
            date = nextOccurrence(date, Frequency.MONTHLY, 31)
            seen += date
        }
        assertEquals(
            listOf(LocalDate(2026, 2, 28), LocalDate(2026, 3, 31), LocalDate(2026, 4, 30), LocalDate(2026, 5, 31)),
            seen,
        )
    }

    @Test
    fun monthlyAcrossTheYearBoundaryAndYearlyOnFeb29() {
        assertEquals(LocalDate(2027, 1, 15), nextOccurrence(LocalDate(2026, 12, 15), Frequency.MONTHLY, 15))
        assertEquals(LocalDate(2025, 2, 28), nextOccurrence(LocalDate(2024, 2, 29), Frequency.YEARLY, 29))
        assertEquals(LocalDate(2028, 2, 29), nextOccurrence(LocalDate(2027, 2, 28), Frequency.YEARLY, 29))
    }

    // ---------- processing ----------

    private class FakeSchedules(val items: MutableList<RecurringPayment>) : RecurringRepository {
        override val payments: Flow<List<RecurringPayment>> = flowOf(items)
        override suspend fun save(draft: RecurringDraft) = Unit
        override suspend fun setActive(id: Long, active: Boolean) = Unit
        override suspend fun delete(id: Long) = Unit
        override suspend fun undoDelete() = Unit
        override suspend fun lastExpenseCategoryId(): Long? = null
        override suspend fun due(today: LocalDate) = items.filter { it.isActive && it.nextDate <= today }
        override suspend fun setNextDate(id: Long, nextDate: LocalDate) {
            val i = items.indexOfFirst { it.id == id }
            items[i] = items[i].copy(nextDate = nextDate)
        }
    }

    private class RecordingTransactions : TransactionRepository {
        data class Created(val type: TransactionType, val categoryId: Long?, val amount: Money, val dateTime: Long)
        val created = mutableListOf<Created>()
        override val customIncomeSources: Flow<List<String>> = flowOf(emptyList())
        override val noteSuggestions: Flow<List<NoteSuggestion>> = flowOf(emptyList())
        override suspend fun addManual(
            type: TransactionType, accountId: Long, amount: Money, source: IncomeSource?, note: String, dateTime: Long,
        ) { created += Created(type, null, amount, dateTime) }
        override suspend fun addExpense(
            accountId: Long, categoryId: Long, amount: Money, accountAmount: Money, rate: BigDecimal,
            note: String, dateTime: Long,
        ) { created += Created(TransactionType.EXPENSE, categoryId, amount, dateTime) }
        override fun observeItems(query: TransactionQuery, limit: Int): Flow<List<TransactionItem>> = flowOf(emptyList())
        override suspend fun update(
            id: Long, type: TransactionType, accountId: Long, categoryId: Long?, source: IncomeSource?,
            amount: Money, accountAmount: Money, rate: BigDecimal, note: String, dateTime: Long,
        ) = Unit
        override suspend fun delete(id: Long) = Unit
        override suspend fun undoDelete() = Unit
        override suspend fun duplicate(id: Long, dateTime: Long) = true
    }

    private object NoRates : CurrencyRepository {
        override val rateTable: Flow<RateTable> = flowOf(RateTable.Empty)
        override val updatedAt: Flow<Long?> = flowOf(null)
        override suspend fun refresh() = false
    }

    private fun payment(
        id: Long,
        type: TransactionType,
        frequency: Frequency,
        next: LocalDate,
        category: TransactionCategory? = null,
        active: Boolean = true,
    ) = RecurringPayment(
        id = id, type = type, accountId = 1, accountName = "Card", category = category, source = null,
        amount = Money(10_000, Currency.UAH), note = "", frequency = frequency, anchorDay = next.day,
        nextDate = next, isActive = active,
    )

    private fun useCase(schedules: FakeSchedules, transactions: RecordingTransactions) =
        ProcessRecurringPaymentsUseCase(
            schedules,
            AddManualTransactionUseCase(transactions),
            AddExpenseUseCase(transactions, ConvertMoneyUseCase(NoRates)),
        )

    private val food = TransactionCategory(7, "Food", 0xFF00FF00, "icon:cart")

    @Test
    fun dueScheduleCreatesATransactionAndMovesOn() = runTest {
        val schedules = FakeSchedules(
            mutableListOf(payment(1, TransactionType.EXPENSE, Frequency.MONTHLY, LocalDate(2026, 3, 10), food)),
        )
        val transactions = RecordingTransactions()

        val created = useCase(schedules, transactions)(today = LocalDate(2026, 3, 10))

        assertEquals(1, created)
        assertEquals(7L, transactions.created.single().categoryId)
        assertEquals(LocalDate(2026, 4, 10), schedules.items.single().nextDate)
    }

    @Test
    fun missedOccurrencesAreCreatedOnePerDueDate() = runTest {
        val schedules = FakeSchedules(
            mutableListOf(payment(1, TransactionType.INCOME, Frequency.WEEKLY, LocalDate(2026, 3, 1))),
        )
        val transactions = RecordingTransactions()

        // 1, 8, 15 and 22 March are due when the app is opened on the 24th
        val created = useCase(schedules, transactions)(today = LocalDate(2026, 3, 24))

        assertEquals(4, created)
        assertEquals(LocalDate(2026, 3, 29), schedules.items.single().nextDate)
        assertEquals(4, transactions.created.size)
    }

    @Test
    fun futureAndPausedSchedulesAreLeftAlone() = runTest {
        val schedules = FakeSchedules(
            mutableListOf(
                payment(1, TransactionType.EXPENSE, Frequency.MONTHLY, LocalDate(2026, 3, 20)),
                payment(2, TransactionType.EXPENSE, Frequency.MONTHLY, LocalDate(2026, 3, 1), active = false),
            ),
        )
        val transactions = RecordingTransactions()

        assertEquals(0, useCase(schedules, transactions)(today = LocalDate(2026, 3, 10)))
        assertEquals(LocalDate(2026, 3, 20), schedules.items[0].nextDate)
        assertEquals(LocalDate(2026, 3, 1), schedules.items[1].nextDate)
    }

    @Test
    fun aVeryOldDailyScheduleIsCappedInsteadOfFloodingTheHistory() = runTest {
        val schedules = FakeSchedules(
            mutableListOf(payment(1, TransactionType.EXPENSE, Frequency.DAILY, LocalDate(2020, 1, 1))),
        )
        val transactions = RecordingTransactions()
        val today = LocalDate(2026, 3, 10)

        val created = useCase(schedules, transactions)(today)

        assertEquals(100, created)
        assertEquals(LocalDate(2026, 3, 11), schedules.items.single().nextDate)
    }
}
