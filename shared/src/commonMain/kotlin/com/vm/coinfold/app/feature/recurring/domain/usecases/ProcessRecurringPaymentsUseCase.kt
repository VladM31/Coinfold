package com.vm.coinfold.app.feature.recurring.domain.usecases

import com.vm.coinfold.app.feature.recurring.domain.models.RecurringPayment
import com.vm.coinfold.app.feature.recurring.domain.repositories.RecurringRepository
import com.vm.coinfold.app.feature.recurring.domain.services.nextOccurrence
import com.vm.coinfold.app.feature.transactions.domain.models.AddExpenseResult
import com.vm.coinfold.app.feature.transactions.domain.usecases.AddExpenseUseCase
import com.vm.coinfold.app.feature.transactions.domain.usecases.AddManualTransactionUseCase
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.utils.epochMillisFor
import com.vm.coinfold.app.utils.today
import kotlinx.datetime.LocalDate

/**
 * Creates the transactions of every schedule that is due. Run when the app starts: if it was not opened for a
 * while, each missed occurrence is created with its own date (up to [MAX_CATCH_UP] per schedule), then the
 * schedule moves on to its next future date. Returns how many transactions were created.
 */
class ProcessRecurringPaymentsUseCase(
    private val repository: RecurringRepository,
    private val addManual: AddManualTransactionUseCase,
    private val addExpense: AddExpenseUseCase,
) {
    suspend operator fun invoke(today: LocalDate = today()): Int {
        var created = 0
        for (payment in repository.due(today)) {
            var next = payment.nextDate
            var count = 0
            while (next <= today && count < MAX_CATCH_UP) {
                create(payment, next)
                created++
                count++
                next = nextOccurrence(next, payment.frequency, payment.anchorDay)
            }
            // Past the catch-up limit the remaining old occurrences are skipped instead of flooding the history.
            while (next <= today) next = nextOccurrence(next, payment.frequency, payment.anchorDay)
            repository.setNextDate(payment.id, next)
        }
        return created
    }

    private suspend fun create(payment: RecurringPayment, date: LocalDate) {
        val dateTime = epochMillisFor(date)
        val category = payment.category
        when {
            payment.type == TransactionType.EXPENSE && category != null -> {
                // The amount is in the account currency, so no exchange rate is involved.
                val result = addExpense(
                    accountId = payment.accountId,
                    accountCurrency = payment.amount.currency,
                    categoryId = category.id,
                    amount = payment.amount,
                    note = payment.note,
                    dateTime = dateTime,
                )
                check(result == AddExpenseResult.SUCCESS) { "Same-currency expense cannot need a rate" }
            }
            else -> addManual(
                type = payment.type,
                accountId = payment.accountId,
                amount = payment.amount,
                source = payment.source ?: IncomeSource.Preset.OTHER.takeIf { payment.type == TransactionType.INCOME },
                note = payment.note,
                dateTime = dateTime,
            )
        }
    }

    private companion object {
        const val MAX_CATCH_UP = 100
    }
}
