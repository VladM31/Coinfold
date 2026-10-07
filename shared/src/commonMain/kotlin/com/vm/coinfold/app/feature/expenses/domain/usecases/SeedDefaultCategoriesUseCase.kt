package com.vm.coinfold.app.feature.expenses.domain.usecases

import com.vm.coinfold.app.feature.expenses.domain.models.DEFAULT_CATEGORIES_VERSION
import com.vm.coinfold.app.feature.expenses.domain.models.DefaultCategories
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString

/**
 * Installs the default categories once per [DEFAULT_CATEGORIES_VERSION]. On first launch that simply
 * creates them; when the version was raised, the current categories are removed first (archived instead
 * if they already have expenses, so history stays intact). Names are localized with the language that is
 * active at that moment and are plain user data afterwards.
 */
class SeedDefaultCategoriesUseCase(
    private val categories: CategoryRepository,
    private val settings: SettingsRepository,
) {
    suspend operator fun invoke() {
        if (!settings.claimDefaultCategories(DEFAULT_CATEGORIES_VERSION)) return
        categories.categories.first().forEach { categories.delete(it.id) }
        DefaultCategories.forEach { default ->
            categories.save(null, getString(default.name), default.color, default.icon)
        }
    }
}
