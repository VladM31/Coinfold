package com.vm.coinfold.app.feature.backup.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.backup.db.entities.BackupSnapshot
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringEntity
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionEntity

/**
 * Reads and replaces all user data at once. This DAO deliberately touches the tables of several features:
 * a backup is a whole-database operation, and [replaceAll] must be one transaction so a failed restore
 * leaves the existing data untouched.
 */
@Dao
abstract class BackupDao {
    @Query("SELECT * FROM accounts")
    abstract suspend fun accounts(): List<AccountEntity>

    @Query("SELECT * FROM categories")
    abstract suspend fun categories(): List<CategoryEntity>

    @Query("SELECT * FROM transactions")
    abstract suspend fun transactions(): List<TransactionEntity>

    @Query("SELECT * FROM recurring")
    abstract suspend fun recurring(): List<RecurringEntity>

    // Children first, because of the foreign keys.
    @Query("DELETE FROM recurring")
    abstract suspend fun clearRecurring()

    @Query("DELETE FROM transactions")
    abstract suspend fun clearTransactions()

    @Query("DELETE FROM categories")
    abstract suspend fun clearCategories()

    @Query("DELETE FROM accounts")
    abstract suspend fun clearAccounts()

    @Insert
    abstract suspend fun insertAccounts(items: List<AccountEntity>)

    @Insert
    abstract suspend fun insertCategories(items: List<CategoryEntity>)

    @Insert
    abstract suspend fun insertTransactions(items: List<TransactionEntity>)

    @Insert
    abstract suspend fun insertRecurring(items: List<RecurringEntity>)

    @Transaction
    open suspend fun snapshot(): BackupSnapshot =
        BackupSnapshot(accounts(), categories(), transactions(), recurring())

    @Transaction
    open suspend fun replaceAll(snapshot: BackupSnapshot) {
        clearRecurring()
        clearTransactions()
        clearCategories()
        clearAccounts()
        insertAccounts(snapshot.accounts)
        insertCategories(snapshot.categories)
        insertTransactions(snapshot.transactions)
        insertRecurring(snapshot.recurring)
    }
}
