package com.vm.coinfold.app.feature.expenses.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.vm.coinfold.app.feature.expenses.db.entities.TotalRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseStatsDao {
    @Query(
        "SELECT type, categoryId, currency, SUM(amountMinor) AS total FROM transactions " +
            "WHERE dateTime >= :from AND dateTime < :to GROUP BY type, categoryId, currency",
    )
    fun observeTotals(from: Long, to: Long): Flow<List<TotalRow>>
}
