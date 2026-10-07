package com.vm.coinfold.app.feature.recurring.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringEntity
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringRow
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {
    @Query(
        "SELECT r.*, a.name AS accountName, a.currency AS accountCurrency, " +
            "c.name AS categoryName, c.color AS categoryColor, c.icon AS categoryIcon " +
            "FROM recurring r " +
            "JOIN accounts a ON a.id = r.accountId " +
            "LEFT JOIN categories c ON c.id = r.categoryId " +
            "ORDER BY r.isActive DESC, r.nextDate, r.id",
    )
    fun observeAll(): Flow<List<RecurringRow>>

    /** Active schedules that are due on or before [today] (ISO date; ISO strings sort like dates). */
    @Query(
        "SELECT r.*, a.name AS accountName, a.currency AS accountCurrency, " +
            "c.name AS categoryName, c.color AS categoryColor, c.icon AS categoryIcon " +
            "FROM recurring r " +
            "JOIN accounts a ON a.id = r.accountId " +
            "LEFT JOIN categories c ON c.id = r.categoryId " +
            "WHERE r.isActive = 1 AND a.isArchived = 0 AND r.nextDate <= :today ORDER BY r.nextDate, r.id",
    )
    suspend fun getDue(today: String): List<RecurringRow>

    @Query("SELECT * FROM recurring WHERE id = :id")
    suspend fun getById(id: Long): RecurringEntity?

    @Insert
    suspend fun insert(entity: RecurringEntity): Long

    @Update
    suspend fun update(entity: RecurringEntity)

    @Query("UPDATE recurring SET nextDate = :nextDate WHERE id = :id")
    suspend fun setNextDate(id: Long, nextDate: String)

    @Query("UPDATE recurring SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("DELETE FROM recurring WHERE id = :id")
    suspend fun delete(id: Long)
}
