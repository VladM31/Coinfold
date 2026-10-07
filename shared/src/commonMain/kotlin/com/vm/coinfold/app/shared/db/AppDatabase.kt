package com.vm.coinfold.app.shared.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.vm.coinfold.app.feature.accounts.db.AccountDao
import com.vm.coinfold.app.feature.accounts.db.AccountEntity
import com.vm.coinfold.app.feature.currency.db.RateDao
import com.vm.coinfold.app.feature.currency.db.RateEntity
import com.vm.coinfold.app.feature.expenses.db.CategoryDao
import com.vm.coinfold.app.feature.expenses.db.CategoryEntity
import com.vm.coinfold.app.feature.transactions.db.TransactionDao
import com.vm.coinfold.app.feature.transactions.db.TransactionEntity

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
    abstract fun transactionDao(): TransactionDao
    abstract fun rateDao(): RateDao
}

// Room generates the actual implementations for each target.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
