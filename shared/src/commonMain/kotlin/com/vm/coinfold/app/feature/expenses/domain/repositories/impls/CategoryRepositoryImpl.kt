package com.vm.coinfold.app.feature.expenses.domain.repositories.impls

import com.vm.coinfold.app.feature.expenses.db.daos.CategoryDao
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.CategoryDeleteResult
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {

    override val categories: Flow<List<Category>> =
        dao.observeActive().map { list -> list.map { Category(it.id, it.name, it.color, it.icon) } }

    override suspend fun save(id: Long?, name: String, color: Long, icon: String) {
        val trimmed = name.trim()
        if (id == null) {
            dao.insert(CategoryEntity(name = trimmed, color = color, icon = icon, sortOrder = dao.maxOrder() + 1))
        } else {
            val existing = dao.getById(id) ?: return
            dao.update(existing.copy(name = trimmed, color = color, icon = icon))
        }
    }

    /** What the last [delete] did, kept in memory so it can be undone right after. */
    private var lastDelete: Pair<CategoryEntity, Boolean>? = null

    override suspend fun delete(id: Long): CategoryDeleteResult {
        val entity = dao.getById(id)
        val archive = dao.countTransactions(id) > 0
        if (archive) dao.archive(id) else dao.delete(id)
        if (entity != null) lastDelete = entity to archive
        return if (archive) CategoryDeleteResult.ARCHIVED else CategoryDeleteResult.DELETED
    }

    override suspend fun undoDelete() {
        val (entity, archived) = lastDelete ?: return
        lastDelete = null
        if (archived) dao.unarchive(entity.id) else dao.insert(entity)
    }

    override suspend fun reorder(ids: List<Long>) {
        val byId = dao.observeActive().first().associateBy { it.id }
        // Unknown ids are ignored; categories missing from [ids] keep their relative order at the end.
        val ordered = ids.mapNotNull { byId[it] } + byId.values.filter { it.id !in ids }.sortedBy { it.sortOrder }
        dao.updateAll(ordered.mapIndexed { index, entity -> entity.copy(sortOrder = index) })
    }
}
