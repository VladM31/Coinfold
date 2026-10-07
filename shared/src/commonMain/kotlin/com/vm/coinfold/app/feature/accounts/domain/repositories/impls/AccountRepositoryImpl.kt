package com.vm.coinfold.app.feature.accounts.domain.repositories.impls

import com.vm.coinfold.app.feature.accounts.db.daos.AccountDao
import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.accounts.db.entities.AccountRow
import com.vm.coinfold.app.feature.accounts.domain.models.Account
import com.vm.coinfold.app.feature.accounts.domain.models.AccountDraft
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.accounts.domain.models.DeleteResult
import com.vm.coinfold.app.feature.accounts.domain.repositories.AccountRepository
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(private val dao: AccountDao) : AccountRepository {

    override val accounts: Flow<List<AccountWithBalance>> =
        dao.observeActiveWithBalance().map { rows -> rows.map { it.toModel() } }

    override suspend fun save(draft: AccountDraft) {
        if (draft.id == null) {
            dao.insert(draft.toEntity(id = 0))
        } else {
            val existing = dao.getById(draft.id) ?: return
            // Currency is fixed once the account has history, otherwise stored amounts would be wrong.
            val currency = if (dao.countTransactions(draft.id) > 0) existing.currency else draft.currency
            dao.update(
                draft.toEntity(draft.id).copy(
                    currency = currency,
                    initialBalanceMinor = draft.initialBalance.minorUnits,
                ),
            )
        }
    }

    /** What the last [delete] did, kept in memory so it can be undone right after. */
    private var lastDelete: Pair<AccountEntity, Boolean>? = null

    override suspend fun delete(id: Long): DeleteResult {
        val entity = dao.getById(id)
        val archive = dao.countTransactions(id) > 0
        if (archive) dao.archive(id) else dao.delete(id)
        if (entity != null) lastDelete = entity to archive
        return if (archive) DeleteResult.ARCHIVED else DeleteResult.DELETED
    }

    override suspend fun undoDelete() {
        val (entity, archived) = lastDelete ?: return
        lastDelete = null
        if (archived) dao.unarchive(entity.id) else dao.insert(entity)
    }
}

private fun AccountDraft.toEntity(id: Long) = AccountEntity(
    id = id,
    name = name.trim(),
    currency = currency,
    initialBalanceMinor = initialBalance.minorUnits,
    color = color,
    icon = icon,
)

private fun AccountRow.toModel(): AccountWithBalance {
    val currency = account.currency
    return AccountWithBalance(
        account = Account(
            id = account.id,
            name = account.name,
            currency = currency,
            initialBalance = Money(account.initialBalanceMinor, currency),
            color = account.color,
            icon = account.icon,
        ),
        balance = Money(account.initialBalanceMinor + delta, currency),
        transactionCount = txCount,
    )
}
