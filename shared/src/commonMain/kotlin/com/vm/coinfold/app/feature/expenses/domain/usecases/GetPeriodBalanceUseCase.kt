package com.vm.coinfold.app.feature.expenses.domain.usecases

import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import com.vm.coinfold.app.feature.expenses.domain.repositories.ExpenseStatsRepository
import com.vm.coinfold.app.feature.expenses.domain.services.calculateSummary
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.utils.today
import kotlinx.coroutines.flow.first

/** What is left of this period's income after spending, in the main currency; income - spending can be negative. */
data class PeriodBalance(val remaining: Money, val income: Money, val spent: Money)

/** One-shot version of the main screen's ring numbers, for places that cannot observe (the home screen widget). */
class GetPeriodBalanceUseCase(
    private val stats: ExpenseStatsRepository,
    private val categories: CategoryRepository,
    private val currencies: CurrencyRepository,
    private val settings: SettingsRepository,
) {
    suspend operator fun invoke(): PeriodBalance {
        val current = settings.settings.first()
        val period = Period.containing(today(), current.periodStartDay)
        val summary = calculateSummary(
            totals = stats.observeTotals(period).first(),
            categories = categories.categories.first(),
            rates = currencies.rateTable.first(),
            mainCurrency = current.mainCurrency,
        )
        return PeriodBalance(summary.remaining, summary.income, summary.spent)
    }
}
