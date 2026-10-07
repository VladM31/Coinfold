package com.vm.coinfold.app.feature.overview.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.vm.coinfold.app.feature.overview.db.entities.OverviewRow
import kotlinx.coroutines.flow.Flow

@Dao
interface OverviewDao {
    /** [from] inclusive, [to] exclusive, both UTC epoch millis. */
    @Query(
        "SELECT type, categoryId, currency, amountMinor, dateTime FROM transactions " +
            "WHERE dateTime >= :from AND dateTime < :to ORDER BY dateTime",
    )
    fun observeRows(from: Long, to: Long): Flow<List<OverviewRow>>

    /** Everything ever recorded, for averages and the trend over several periods. */
    @Query("SELECT type, categoryId, currency, amountMinor, dateTime FROM transactions")
    fun observeAll(): Flow<List<OverviewRow>>
}
