package com.vm.coinfold.app.feature.overview.domain.repositories

import com.vm.coinfold.app.feature.overview.domain.models.OverviewTransaction
import com.vm.coinfold.app.shared.domain.models.Period
import kotlinx.coroutines.flow.Flow

interface OverviewRepository {
    /** All transactions inside [period], oldest first. */
    fun observeTransactions(period: Period): Flow<List<OverviewTransaction>>

    /** Every expense ever recorded. */
    fun observeAllExpenses(): Flow<List<OverviewTransaction>>
}
