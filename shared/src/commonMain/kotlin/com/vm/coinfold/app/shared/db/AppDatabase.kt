package com.vm.coinfold.app.shared.db

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.vm.coinfold.app.feature.accounts.db.daos.AccountDao
import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.backup.db.daos.BackupDao
import com.vm.coinfold.app.feature.currency.db.daos.RateDao
import com.vm.coinfold.app.feature.currency.db.entities.RateEntity
import com.vm.coinfold.app.feature.expenses.db.daos.CategoryDao
import com.vm.coinfold.app.feature.expenses.db.daos.ExpenseStatsDao
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.feature.overview.db.daos.OverviewDao
import com.vm.coinfold.app.feature.recurring.db.daos.RecurringDao
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringEntity
import com.vm.coinfold.app.feature.transactions.db.daos.TransactionDao
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionEntity
import com.vm.coinfold.app.shared.db.converters.Converters

/** The single database of the app; every feature registers its DAO here. */
@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        RateEntity::class,
        RecurringEntity::class,
    ],
    version = 2,
    // v1 -> v2 only adds the "recurring" table, so Room can migrate existing data automatically.
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseStatsDao(): ExpenseStatsDao
    abstract fun overviewDao(): OverviewDao
    abstract fun transactionDao(): TransactionDao
    abstract fun rateDao(): RateDao
    abstract fun recurringDao(): RecurringDao
    abstract fun backupDao(): BackupDao
}

// Room generates the actual implementations for each target.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
