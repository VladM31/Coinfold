package com.vm.coinfold.app.feature.accounts.domain.repositories

import com.vm.coinfold.app.feature.accounts.db.daos.AccountDao
import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.accounts.db.entities.AccountRow
import com.vm.coinfold.app.feature.accounts.domain.models.AccountDraft
import com.vm.coinfold.app.feature.accounts.domain.models.DeleteResult
import com.vm.coinfold.app.feature.accounts.domain.repositories.impls.AccountRepositoryImpl
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class AccountRepositoryTest {

    private class FakeDao(var txCount: Int = 0) : AccountDao {
        val rows = MutableStateFlow<List<AccountRow>>(emptyList())
        val stored = mutableMapOf<Long, AccountEntity>()
        var archived = mutableListOf<Long>()
        var deleted = mutableListOf<Long>()
        private var nextId = 1L

        override fun observeActiveWithBalance(): Flow<List<AccountRow>> = rows
        override suspend fun getById(id: Long) = stored[id]
        override suspend fun countTransactions(id: Long) = txCount
        override suspend fun insert(account: AccountEntity): Long {
            val id = nextId++
            stored[id] = account.copy(id = id)
            return id
        }
        override suspend fun update(account: AccountEntity) { stored[account.id] = account }
        override suspend fun archive(id: Long) { archived += id }
        override suspend fun unarchive(id: Long) { archived -= id }
        override suspend fun delete(id: Long) { deleted += id }
    }

    @Test
    fun balanceIsInitialPlusNetOfTransactions() = runTest {
        val dao = FakeDao()
        // initial 100.00, income minus expenses = +25.50, 3 transactions
        dao.rows.value = listOf(
            AccountRow(AccountEntity(1, "Card", Currency.USD, 10_000), delta = 2_550, txCount = 3),
        )
        val item = AccountRepositoryImpl(dao).accounts.first().single()

        assertEquals(Money(12_550, Currency.USD), item.balance)
        assertEquals(Money(10_000, Currency.USD), item.account.initialBalance)
        assertEquals(3, item.transactionCount)
    }

    @Test
    fun deleteArchivesAccountsWithTransactions() = runTest {
        val withHistory = FakeDao(txCount = 2)
        assertEquals(DeleteResult.ARCHIVED, AccountRepositoryImpl(withHistory).delete(1))
        assertEquals(listOf(1L), withHistory.archived)
        assertEquals(emptyList(), withHistory.deleted)

        val empty = FakeDao(txCount = 0)
        assertEquals(DeleteResult.DELETED, AccountRepositoryImpl(empty).delete(1))
        assertEquals(listOf(1L), empty.deleted)
    }

    @Test
    fun currencyCannotChangeOnceAccountHasTransactions() = runTest {
        val dao = FakeDao(txCount = 1)
        dao.stored[1] = AccountEntity(1, "Card", Currency.UAH, 0)

        AccountRepositoryImpl(dao).save(
            AccountDraft(1, " Renamed ", Currency.USD, Money(500, Currency.USD), color = null),
        )

        val saved = dao.stored.getValue(1)
        assertEquals(Currency.UAH, saved.currency)
        assertEquals("Renamed", saved.name)
        assertEquals(500, saved.initialBalanceMinor)
        assertNull(saved.color)
    }

    @Test
    fun undoDeleteUnarchivesAnArchivedAccount() = runTest {
        val dao = FakeDao(txCount = 2)
        dao.stored[1] = AccountEntity(1, "Card", Currency.UAH, 0)
        val repo = AccountRepositoryImpl(dao)

        repo.delete(1)
        assertEquals(listOf(1L), dao.archived)
        repo.undoDelete()

        assertEquals(emptyList(), dao.archived)
    }

    @Test
    fun undoDeleteReinsertsADeletedAccount() = runTest {
        val dao = FakeDao(txCount = 0)
        dao.stored[1] = AccountEntity(1, "Card", Currency.UAH, 500)
        val repo = AccountRepositoryImpl(dao)

        repo.delete(1)
        repo.undoDelete()

        assertEquals("Card", dao.stored.values.single { it.initialBalanceMinor == 500L }.name)
    }
}
