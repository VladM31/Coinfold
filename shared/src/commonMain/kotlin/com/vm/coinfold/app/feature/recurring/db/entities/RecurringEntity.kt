package com.vm.coinfold.app.feature.recurring.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.shared.domain.models.TransactionType

/**
 * A payment that repeats on a schedule. The amount is always in the currency of the account, so creating
 * the transaction never needs an exchange rate. [nextDate] (ISO `yyyy-MM-dd`) is the next day it is due;
 * [anchorDay] keeps monthly/yearly payments on their original day of the month (31st -> last day of short months).
 */
@Entity(
    tableName = "recurring",
    foreignKeys = [
        // Deleting an account removes its schedules; deleting a category turns them into plain withdrawals.
        ForeignKey(AccountEntity::class, ["id"], ["accountId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(CategoryEntity::class, ["id"], ["categoryId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("accountId"), Index("categoryId"), Index("nextDate")],
)
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val accountId: Long,
    val categoryId: Long? = null,
    val incomeSource: String? = null,
    val amountMinor: Long,
    val note: String = "",
    /** Name of the Frequency enum. */
    val frequency: String,
    val anchorDay: Int,
    val nextDate: String,
    val isActive: Boolean = true,
)
