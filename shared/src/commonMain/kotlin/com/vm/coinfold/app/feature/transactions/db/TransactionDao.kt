package com.vm.coinfold.app.feature.transactions.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateTime DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateTime >= :from AND dateTime < :to ORDER BY dateTime DESC, id DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<TransactionEntity>>

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

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)
}
