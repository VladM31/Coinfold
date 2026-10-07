package com.vm.coinfold.app.feature.transactions.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vm.coinfold.app.feature.transactions.db.entities.NoteStatRow
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionEntity
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateTime DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateTime >= :from AND dateTime < :to ORDER BY dateTime DESC, id DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<TransactionEntity>>

    /** Filtered list for the Transactions screen; null filter arguments mean "any". [type] is the enum name. */
    @Query(
        "SELECT t.*, c.name AS categoryName, c.color AS categoryColor, c.icon AS categoryIcon, " +
            "a.name AS accountName, a.currency AS accountCurrency " +
            "FROM transactions t " +
            "JOIN accounts a ON a.id = t.accountId " +
            "LEFT JOIN categories c ON c.id = t.categoryId " +
            "WHERE t.dateTime >= :from AND t.dateTime < :to " +
            "AND (:accountId IS NULL OR t.accountId = :accountId) " +
            "AND (:categoryId IS NULL OR t.categoryId = :categoryId) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "ORDER BY t.dateTime DESC, t.id DESC LIMIT :limit",
    )
    fun observeRows(
        from: Long,
        to: Long,
        accountId: Long?,
        categoryId: Long?,
        type: String?,
        limit: Int,
    ): Flow<List<TransactionRow>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun countForAccount(accountId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countForCategory(categoryId: Long): Int

    /** Custom (user-defined) income sources used so far; presets are stored with the "preset:" prefix. */
    @Query(
        "SELECT DISTINCT incomeSource FROM transactions " +
            "WHERE type = 'INCOME' AND incomeSource IS NOT NULL AND incomeSource NOT LIKE 'preset:%' " +
            "ORDER BY incomeSource",
    )
    fun observeCustomIncomeSources(): Flow<List<String>>

    /** Past notes with their category and usage count, most used first; the source of the suggestions. */
    @Query(
        "SELECT note, categoryId, COUNT(*) AS uses, MAX(dateTime) AS lastUsed FROM transactions " +
            "WHERE note != '' GROUP BY note, categoryId ORDER BY uses DESC, lastUsed DESC LIMIT 500",
    )
    fun observeNoteStats(): Flow<List<NoteStatRow>>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)
}
