package com.vm.coinfold.app.feature.expenses.domain.repositories.impls

import com.vm.coinfold.app.feature.expenses.db.daos.ExpenseStatsDao
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.feature.expenses.domain.models.PeriodTotal
import com.vm.coinfold.app.feature.expenses.domain.repositories.ExpenseStatsRepository
import com.vm.coinfold.app.shared.domain.models.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone

class ExpenseStatsRepositoryImpl(private val dao: ExpenseStatsDao) : ExpenseStatsRepository {
    override fun observeTotals(period: Period): Flow<List<PeriodTotal>> {
        val timeZone = TimeZone.currentSystemDefault()
        return dao.observeTotals(period.startMillis(timeZone), period.endMillis(timeZone)).map { rows ->
            rows.map { PeriodTotal(it.type, it.categoryId, Money(it.total, it.currency)) }
        }
    }
}
