package com.vm.coinfold.app.feature.expenses.main

import com.vm.coinfold.app.feature.expenses.db.CategoryDao
import com.vm.coinfold.app.feature.expenses.db.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface CategoryRepository {
    /** Active categories in display order. */
    val categories: Flow<List<Category>>
    suspend fun save(id: Long?, name: String, color: Long, icon: String)

    /** Deletes the category, or archives it if it already has transactions. */
    suspend fun delete(id: Long): CategoryDeleteResult

    /** Swaps the category with its neighbour; no-op at the ends of the list. */
    suspend fun move(id: Long, up: Boolean)
}

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

    override suspend fun move(id: Long, up: Boolean) {
        val list = dao.observeActive().first().toMutableList()
        val index = list.indexOfFirst { it.id == id }
        val target = if (up) index - 1 else index + 1
        if (index < 0 || target !in list.indices) return
        val moved = list.removeAt(index)
        list.add(target, moved)
        // Rewrite contiguous order values so they stay unique.
        dao.updateAll(list.mapIndexed { i, entity -> entity.copy(sortOrder = i) })
    }
}
