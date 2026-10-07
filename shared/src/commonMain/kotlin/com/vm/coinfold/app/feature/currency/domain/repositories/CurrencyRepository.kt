package com.vm.coinfold.app.feature.currency.domain.repositories

import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    /** Rates cached in the database; emits again whenever they are refreshed. */
    val rateTable: Flow<RateTable>

    /** Epoch millis of the last successful refresh, null if rates were never loaded. */
    val updatedAt: Flow<Long?>

    /**
     * Fetches fresh rates unless an attempt was made less than 5 minutes ago (API limit).
     * Returns true if new rates were stored; on any error the cache is left untouched.
     */
    suspend fun refresh(): Boolean
}
