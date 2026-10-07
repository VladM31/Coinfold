package com.vm.coinfold.app.feature.transactions.domain.repositories.impls

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.transactions.db.daos.TransactionDao
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionEntity
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionRow
import com.vm.coinfold.app.feature.transactions.domain.models.NoteSuggestion
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionCategory
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionQuery
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {

    override val customIncomeSources: Flow<List<String>> = dao.observeCustomIncomeSources()

    override val noteSuggestions: Flow<List<NoteSuggestion>> = dao.observeNoteStats().map { rows ->
        rows.map { NoteSuggestion(it.note, it.categoryId, it.uses, it.lastUsed) }
    }

    override suspend fun addManual(
        type: TransactionType,
        accountId: Long,
        amount: Money,
        source: IncomeSource?,
        note: String,
        dateTime: Long,
    ) {
        require(amount.minorUnits > 0) { "Amount must be positive" }
        dao.insert(
            TransactionEntity(
                type = type,
                accountId = accountId,
                incomeSource = if (type == TransactionType.INCOME) source?.toStored() else null,
                amountMinor = amount.minorUnits,
                currency = amount.currency,
                accountAmountMinor = amount.minorUnits,
                rate = BigDecimal.ONE.toPlainString(),
                note = note.trim(),
                dateTime = dateTime,
            ),
        )
    }

    override suspend fun addExpense(
        accountId: Long,
        categoryId: Long,
        amount: Money,
        accountAmount: Money,
        rate: BigDecimal,
        note: String,
        dateTime: Long,
    ) {
        require(amount.minorUnits > 0 && accountAmount.minorUnits > 0) { "Amount must be positive" }
        dao.insert(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                accountId = accountId,
                categoryId = categoryId,
                amountMinor = amount.minorUnits,
                currency = amount.currency,
                accountAmountMinor = accountAmount.minorUnits,
                rate = rate.toPlainString(),
                note = note.trim(),
                dateTime = dateTime,
            ),
        )
    }

    override fun observeItems(query: TransactionQuery, limit: Int): Flow<List<TransactionItem>> =
        dao.observeRows(
            from = query.fromMillis,
            to = query.toMillis,
            accountId = query.accountId,
            categoryId = query.categoryId,
            type = query.type?.name,
            limit = limit,
        ).map { rows -> rows.map { it.toItem() } }

    override suspend fun update(
        id: Long,
        type: TransactionType,
        accountId: Long,
        categoryId: Long?,
        source: IncomeSource?,
        amount: Money,
        accountAmount: Money,
        rate: BigDecimal,
        note: String,
        dateTime: Long,
    ) {
        require(amount.minorUnits > 0 && accountAmount.minorUnits > 0) { "Amount must be positive" }
        dao.update(
            TransactionEntity(
                id = id,
                type = type,
                accountId = accountId,
                categoryId = categoryId,
                incomeSource = if (type == TransactionType.INCOME) source?.toStored() else null,
                amountMinor = amount.minorUnits,
                currency = amount.currency,
                accountAmountMinor = accountAmount.minorUnits,
                rate = rate.toPlainString(),
                note = note.trim(),
                dateTime = dateTime,
            ),
        )
    }

    /** The last deleted transaction, kept in memory so the delete can be undone right after. */
    private var lastDeleted: TransactionEntity? = null

    override suspend fun delete(id: Long) {
        lastDeleted = dao.getById(id)
        dao.delete(id)
    }

    override suspend fun undoDelete() {
        val entity = lastDeleted ?: return
        lastDeleted = null
        // The account or category may have been removed in the meantime; then there is nothing to restore into.
        runCatching { dao.insert(entity) }
    }

    override suspend fun duplicate(id: Long, dateTime: Long): Boolean {
        val original = dao.getById(id) ?: return false
        dao.insert(original.copy(id = 0, dateTime = dateTime))
        return true
    }
}

private fun TransactionRow.toItem(): TransactionItem {
    val t = transaction
    return TransactionItem(
        id = t.id,
        type = t.type,
        accountId = t.accountId,
        accountName = accountName,
        accountCurrency = accountCurrency,
        category = if (t.categoryId != null && categoryName != null) {
            TransactionCategory(t.categoryId, categoryName, categoryColor ?: 0xFF78909C, categoryIcon.orEmpty())
        } else {
            null
        },
        source = t.incomeSource?.let { IncomeSource.fromStored(it) },
        note = t.note,
        amount = Money(t.amountMinor, t.currency),
        accountAmount = Money(t.accountAmountMinor, accountCurrency),
        rate = BigDecimal.parseString(t.rate),
        dateTime = t.dateTime,
    )
}
