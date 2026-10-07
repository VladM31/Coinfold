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

    override suspend fun delete(id: Long): CategoryDeleteResult =
        if (dao.countTransactions(id) > 0) {
            dao.archive(id)
            CategoryDeleteResult.ARCHIVED
        } else {
            dao.delete(id)
            CategoryDeleteResult.DELETED
        }

    override suspend fun reorder(ids: List<Long>) {
        val byId = dao.observeActive().first().associateBy { it.id }
        // Unknown ids are ignored; categories missing from [ids] keep their relative order at the end.
        val ordered = ids.mapNotNull { byId[it] } + byId.values.filter { it.id !in ids }.sortedBy { it.sortOrder }
        dao.updateAll(ordered.mapIndexed { index, entity -> entity.copy(sortOrder = index) })
    }
}
