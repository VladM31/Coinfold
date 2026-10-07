package com.vm.coinfold.app.feature.accounts.domain.repositories

import com.vm.coinfold.app.feature.accounts.domain.models.AccountDraft
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.accounts.domain.models.DeleteResult
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    val accounts: Flow<List<AccountWithBalance>>
    suspend fun save(draft: AccountDraft)

    /** Deletes the account, or archives it if it already has transactions. */
    suspend fun delete(id: Long): DeleteResult

    /** Brings back the account removed by the last [delete] (restores it, or un-archives it). */
    suspend fun undoDelete()
}
