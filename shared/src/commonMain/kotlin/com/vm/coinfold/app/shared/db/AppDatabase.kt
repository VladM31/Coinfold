package com.vm.coinfold.app.shared.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.vm.coinfold.app.feature.accounts.db.daos.AccountDao
import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.currency.db.daos.RateDao
import com.vm.coinfold.app.feature.currency.db.entities.RateEntity
import com.vm.coinfold.app.feature.expenses.db.daos.CategoryDao
import com.vm.coinfold.app.feature.expenses.db.daos.ExpenseStatsDao
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.feature.transactions.db.daos.TransactionDao
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionEntity
import com.vm.coinfold.app.shared.db.converters.Converters

/** The single database of the app; every feature registers its DAO here. */
@Database(
    entities = [AccountEntity::class, CategoryEntity::class, TransactionEntity::class, RateEntity::class],
    version = 1,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseStatsDao(): ExpenseStatsDao
    abstract fun transactionDao(): TransactionDao
    abstract fun rateDao(): RateDao
}

// Room generates the actual implementations for each target.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
