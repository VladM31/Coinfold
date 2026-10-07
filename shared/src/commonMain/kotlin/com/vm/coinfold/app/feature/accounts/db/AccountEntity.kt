package com.vm.coinfold.app.feature.accounts.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vm.coinfold.app.shared.domain.Currency

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currency: Currency,
    val initialBalanceMinor: Long,
    val icon: String? = null,
    val color: Long? = null,
    val isArchived: Boolean = false,
)
