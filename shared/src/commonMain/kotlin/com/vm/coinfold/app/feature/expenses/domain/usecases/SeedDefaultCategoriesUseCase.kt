package com.vm.coinfold.app.feature.expenses.domain.usecases

import com.vm.coinfold.app.feature.expenses.domain.models.DefaultCategories
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString

/**
 * Creates the default categories on the first launch only, and only if there are none yet. A persisted flag (not "table is empty")
 * is used, so categories the user deleted on purpose never come back. Names are localized with the
 * language that is active at that moment and are plain user data afterwards.
 */
class SeedDefaultCategoriesUseCase(
    private val categories: CategoryRepository,
    private val settings: SettingsRepository,
) {
    suspend operator fun invoke() {
        if (!settings.markCategoriesSeeded()) return
        // An existing installation that already has categories keeps exactly what it has.
        if (categories.categories.first().isNotEmpty()) return
        DefaultCategories.forEach { default ->
            categories.save(null, getString(default.name), default.color, default.icon)
        }
    }
}
