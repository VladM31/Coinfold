package com.vm.coinfold.app.feature.expenses.domain.repositories

import com.vm.coinfold.app.feature.expenses.db.daos.CategoryDao
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.feature.expenses.domain.models.CategoryDeleteResult
import com.vm.coinfold.app.feature.expenses.domain.repositories.impls.CategoryRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest

class CategoryRepositoryTest {

    private class FakeDao(initial: List<CategoryEntity>, var txCount: Int = 0) : CategoryDao {
        val all = MutableStateFlow(initial)
        override fun observeActive(): Flow<List<CategoryEntity>> =
            all.map { list -> list.filter { !it.isArchived }.sortedBy { it.sortOrder } }
        override suspend fun getById(id: Long) = all.value.firstOrNull { it.id == id }
        override suspend fun maxOrder() = all.value.maxOfOrNull { it.sortOrder } ?: -1
        override suspend fun countTransactions(id: Long) = txCount
        override suspend fun archive(id: Long) {
            all.value = all.value.map { if (it.id == id) it.copy(isArchived = true) else it }
        }
        override suspend fun insert(category: CategoryEntity): Long {
            val id = (all.value.maxOfOrNull { it.id } ?: 0) + 1
            all.value = all.value + category.copy(id = id)
            return id
        }
        override suspend fun update(category: CategoryEntity) {
            all.value = all.value.map { if (it.id == category.id) category else it }
        }
        override suspend fun updateAll(categories: List<CategoryEntity>) {
            categories.forEach { update(it) }
        }
        override suspend fun delete(id: Long) {
            all.value = all.value.filter { it.id != id }
        }
    }

    private fun category(id: Long, order: Int) = CategoryEntity(id, "C$id", 0xFF000000, "icon:cart", order)

    private suspend fun CategoryRepository.ids() = categories.first().map { it.id }

    @Test
    fun reorderStoresTheDroppedOrder() = runTest {
        val dao = FakeDao(listOf(category(1, 0), category(2, 1), category(3, 2), category(4, 3)))
        val repo = CategoryRepositoryImpl(dao)

        repo.reorder(listOf(3, 1, 4, 2))

        assertEquals(listOf(3L, 1L, 4L, 2L), repo.ids())
        // order values are contiguous again
        assertEquals(listOf(0, 1, 2, 3), dao.observeActive().first().map { it.sortOrder })
    }

    @Test
    fun reorderKeepsCategoriesMissingFromTheListAtTheEnd() = runTest {
        val dao = FakeDao(listOf(category(1, 0), category(2, 1), category(3, 2)))
        val repo = CategoryRepositoryImpl(dao)

        repo.reorder(listOf(3, 99)) // 99 does not exist, 1 and 2 were not mentioned

        assertEquals(listOf(3L, 1L, 2L), repo.ids())
    }

    @Test
    fun deleteArchivesWhenCategoryHasExpenses() = runTest {
        val withExpenses = FakeDao(listOf(category(1, 0)), txCount = 3)
        assertEquals(CategoryDeleteResult.ARCHIVED, CategoryRepositoryImpl(withExpenses).delete(1))
        assertEquals(true, withExpenses.all.value.single().isArchived)

        val empty = FakeDao(listOf(category(1, 0)))
        assertEquals(CategoryDeleteResult.DELETED, CategoryRepositoryImpl(empty).delete(1))
        assertEquals(emptyList(), empty.all.value)
    }
}
