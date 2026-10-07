package com.vm.coinfold.app.feature.expenses.domain.repositories

import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.CategoryDeleteResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    /** Active categories in display order. */
    val categories: Flow<List<Category>>
    suspend fun save(id: Long?, name: String, color: Long, icon: String)

    /** Deletes the category, or archives it if it already has transactions. */
    suspend fun delete(id: Long): CategoryDeleteResult

    /** Brings back the category removed by the last [delete] (restores it, or un-archives it). */
    suspend fun undoDelete()

    /** Stores the given order (ids of active categories, first = leftmost). */
    suspend fun reorder(ids: List<Long>)
}
