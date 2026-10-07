package com.vm.coinfold.app.feature.overview.domain.repositories.impls

import com.vm.coinfold.app.feature.overview.db.daos.OverviewDao
import com.vm.coinfold.app.feature.overview.db.entities.OverviewRow
import com.vm.coinfold.app.feature.overview.domain.models.OverviewTransaction
import com.vm.coinfold.app.feature.overview.domain.repositories.OverviewRepository
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.Period
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone

class OverviewRepositoryImpl(private val dao: OverviewDao) : OverviewRepository {
    override fun observeTransactions(period: Period): Flow<List<OverviewTransaction>> {
        val timeZone = TimeZone.currentSystemDefault()
        return dao.observeRows(period.startMillis(timeZone), period.endMillis(timeZone)).map { rows ->
            rows.map { it.toTransaction() }
        }
    }

    override fun observeAllExpenses(): Flow<List<OverviewTransaction>> =
        dao.observeAllExpenses().map { rows -> rows.map { it.toTransaction() } }
}

private fun OverviewRow.toTransaction() = OverviewTransaction(type, categoryId, Money(amountMinor, currency), dateTime)
