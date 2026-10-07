package com.vm.coinfold.app.feature.recurring.domain.models

import com.vm.coinfold.app.feature.transactions.domain.models.TransactionCategory
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlinx.datetime.LocalDate

enum class Frequency { DAILY, WEEKLY, MONTHLY, YEARLY }

/**
 * A scheduled income or expense. [amount] is in the currency of the account. An expense without a
 * [category] is a manual withdrawal.
 */
data class RecurringPayment(
    val id: Long,
    val type: TransactionType,
    val accountId: Long,
    val accountName: String,
    val category: TransactionCategory?,
    val source: IncomeSource?,
    val amount: Money,
    val note: String,
    val frequency: Frequency,
    /** Day of month a monthly/yearly payment is meant for, even if short months push it earlier. */
    val anchorDay: Int,
    val nextDate: LocalDate,
    val isActive: Boolean,
)

/** Data needed to create ([id] == null) or update a schedule; [firstDate] becomes the next due date. */
data class RecurringDraft(
    val id: Long?,
    val type: TransactionType,
    val accountId: Long,
    val categoryId: Long?,
    val source: IncomeSource?,
    val amount: Money,
    val note: String,
    val frequency: Frequency,
    val firstDate: LocalDate,
    val isActive: Boolean,
)
