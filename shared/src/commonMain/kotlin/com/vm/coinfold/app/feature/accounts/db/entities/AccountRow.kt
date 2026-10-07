package com.vm.coinfold.app.feature.accounts.db.entities

import androidx.room.Embedded
import com.vm.coinfold.app.feature.accounts.domain.models.Account

/** Account plus the net sum of its transactions (income minus expense, in account currency). */
data class AccountRow(
    @Embedded val account: AccountEntity,
    val delta: Long,
    val txCount: Int,
)
