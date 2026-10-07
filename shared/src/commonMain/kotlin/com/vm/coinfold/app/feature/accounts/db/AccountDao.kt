package com.vm.coinfold.app.feature.accounts.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Account plus the net sum of its transactions (income minus expense, in account currency). */
data class AccountRow(
    @Embedded val account: AccountEntity,
    val delta: Long,
    val txCount: Int,
)

@Dao
interface AccountDao {
    @Query(
        """
        SELECT a.*,
               COALESCE(SUM(CASE t.type WHEN 'INCOME' THEN t.accountAmountMinor
                                        WHEN 'EXPENSE' THEN -t.accountAmountMinor
                                        ELSE 0 END), 0) AS delta,
               COUNT(t.id) AS txCount
        FROM accounts a
        LEFT JOIN transactions t ON t.accountId = a.id
        WHERE a.isArchived = 0
        GROUP BY a.id
        ORDER BY a.id
        """,
    )
    fun observeActiveWithBalance(): Flow<List<AccountRow>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :id")
    suspend fun countTransactions(id: Long): Int

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("UPDATE accounts SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)
}
