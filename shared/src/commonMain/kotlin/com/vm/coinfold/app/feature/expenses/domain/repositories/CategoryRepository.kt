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

    /** Swaps the category with its neighbour; no-op at the ends of the list. */
    suspend fun move(id: Long, up: Boolean)
}
