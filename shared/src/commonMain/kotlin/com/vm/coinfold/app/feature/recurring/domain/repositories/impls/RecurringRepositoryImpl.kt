package com.vm.coinfold.app.feature.recurring.domain.repositories.impls

import com.vm.coinfold.app.feature.recurring.db.daos.RecurringDao
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringEntity
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringRow
import com.vm.coinfold.app.feature.recurring.domain.models.Frequency
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringDraft
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringPayment
import com.vm.coinfold.app.feature.recurring.domain.repositories.RecurringRepository
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionCategory
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class RecurringRepositoryImpl(private val dao: RecurringDao) : RecurringRepository {

    override val payments: Flow<List<RecurringPayment>> = dao.observeAll().map { rows -> rows.map { it.toModel() } }

    override suspend fun save(draft: RecurringDraft) {
        val entity = RecurringEntity(
            id = draft.id ?: 0,
            type = draft.type,
            accountId = draft.accountId,
            categoryId = draft.categoryId.takeIf { draft.type == TransactionType.EXPENSE },
            incomeSource = draft.source?.toStored().takeIf { draft.type == TransactionType.INCOME },
            amountMinor = draft.amount.minorUnits,
            note = draft.note.trim(),
            frequency = draft.frequency.name,
            anchorDay = draft.firstDate.day,
            nextDate = draft.firstDate.toString(),
            isActive = draft.isActive,
        )
        if (draft.id == null) dao.insert(entity) else dao.update(entity)
    }

    override suspend fun setActive(id: Long, active: Boolean) = dao.setActive(id, active)

    /** The last deleted schedule, kept in memory so the delete can be undone right after. */
    private var lastDeleted: RecurringEntity? = null

    override suspend fun delete(id: Long) {
        lastDeleted = dao.getById(id)
        dao.delete(id)
    }

    override suspend fun undoDelete() {
        val entity = lastDeleted ?: return
        lastDeleted = null
        runCatching { dao.insert(entity) }
    }

    override suspend fun due(today: LocalDate): List<RecurringPayment> =
        dao.getDue(today.toString()).map { it.toModel() }

    override suspend fun setNextDate(id: Long, nextDate: LocalDate) = dao.setNextDate(id, nextDate.toString())
}

private fun RecurringRow.toModel(): RecurringPayment {
    val r = recurring
    return RecurringPayment(
        id = r.id,
        type = r.type,
        accountId = r.accountId,
        accountName = accountName,
        category = if (r.categoryId != null && categoryName != null) {
            TransactionCategory(r.categoryId, categoryName, categoryColor ?: 0xFF78909C, categoryIcon.orEmpty())
        } else {
            null
        },
        source = r.incomeSource?.let { IncomeSource.fromStored(it) },
        amount = Money(r.amountMinor, accountCurrency),
        note = r.note,
        frequency = Frequency.entries.firstOrNull { it.name == r.frequency } ?: Frequency.MONTHLY,
        anchorDay = r.anchorDay,
        nextDate = LocalDate.parse(r.nextDate),
        isActive = r.isActive,
    )
}
