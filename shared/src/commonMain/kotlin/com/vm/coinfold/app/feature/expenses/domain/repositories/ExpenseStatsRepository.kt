package com.vm.coinfold.app.feature.expenses.domain.repositories

import com.vm.coinfold.app.feature.expenses.domain.models.PeriodTotal
import com.vm.coinfold.app.shared.domain.models.Period
import kotlinx.coroutines.flow.Flow

interface ExpenseStatsRepository {
    fun observeTotals(period: Period): Flow<List<PeriodTotal>>
}
