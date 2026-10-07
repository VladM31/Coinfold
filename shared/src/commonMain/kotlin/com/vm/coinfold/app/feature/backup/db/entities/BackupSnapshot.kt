package com.vm.coinfold.app.feature.backup.db.entities

import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringEntity
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionEntity

/** Every user table of the database at one moment. Exchange rates are not included: they are re-downloaded. */
data class BackupSnapshot(
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val recurring: List<RecurringEntity>,
)
