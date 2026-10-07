package com.vm.coinfold.app.feature.transactions.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.TransactionType

/**
 * [amountMinor]/[currency] is the amount as entered; [accountAmountMinor] is the amount in the
 * currency of the account. [rate] (decimal as TEXT) is fixed at creation and never recalculated.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        // RESTRICT: accounts/categories with history are archived instead of deleted
        ForeignKey(AccountEntity::class, ["id"], ["accountId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(CategoryEntity::class, ["id"], ["categoryId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("accountId"), Index("categoryId"), Index("dateTime")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val accountId: Long,
    val categoryId: Long? = null,
    val incomeSource: String? = null,
    val amountMinor: Long,
    val currency: Currency,
    val accountAmountMinor: Long,
    val rate: String,
    val note: String = "",
    /** UTC epoch millis. */
    val dateTime: Long,
)
