package com.vm.coinfold.app.feature.expenses.db

import androidx.room.Dao
import androidx.room.Query
import com.vm.coinfold.app.shared.domain.Currency
import com.vm.coinfold.app.shared.domain.TransactionType
import kotlinx.coroutines.flow.Flow

/** Sum of transaction amounts as entered, grouped so each row has a single currency. */
data class TotalRow(
    val type: TransactionType,
    val categoryId: Long?,
    val currency: Currency,
    val total: Long,
)

@Dao
interface ExpenseStatsDao {
    @Query(
        "SELECT type, categoryId, currency, SUM(amountMinor) AS total FROM transactions " +
            "WHERE dateTime >= :from AND dateTime < :to GROUP BY type, categoryId, currency",
    )
    fun observeTotals(from: Long, to: Long): Flow<List<TotalRow>>
}
