package com.vm.coinfold.app.feature.expenses.domain.models

import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.cat_clothes
import coinfold.shared.generated.resources.cat_dining
import coinfold.shared.generated.resources.cat_education
import coinfold.shared.generated.resources.cat_entertainment
import coinfold.shared.generated.resources.cat_gifts
import coinfold.shared.generated.resources.cat_groceries
import coinfold.shared.generated.resources.cat_health
import coinfold.shared.generated.resources.cat_other
import coinfold.shared.generated.resources.cat_pets
import coinfold.shared.generated.resources.cat_sport
import coinfold.shared.generated.resources.cat_subscriptions
import coinfold.shared.generated.resources.cat_taxes
import coinfold.shared.generated.resources.cat_transport
import coinfold.shared.generated.resources.cat_utilities
import org.jetbrains.compose.resources.StringResource

data class DefaultCategory(val name: StringResource, val color: Long, val icon: String)

/**
 * Raise this when [DefaultCategories] changes and the installed categories should be replaced by the new
 * ones (see SeedDefaultCategoriesUseCase). Existing categories with expenses are archived, not lost.
 */
const val DEFAULT_CATEGORIES_VERSION = 2

/** Categories created on the very first launch; the user can rename, reorder or remove them freely. */
val DefaultCategories: List<DefaultCategory> = listOf(
    DefaultCategory(Res.string.cat_groceries, 0xFF8BC34A, "icon:cart"),
    DefaultCategory(Res.string.cat_pets, 0xFFFF7043, "icon:pets"),
    DefaultCategory(Res.string.cat_utilities, 0xFFFFB300, "icon:electricity"),
    DefaultCategory(Res.string.cat_health, 0xFFE91E63, "icon:medication"),
    DefaultCategory(Res.string.cat_gifts, 0xFFAB47BC, "icon:gift"),
    DefaultCategory(Res.string.cat_taxes, 0xFF3F51B5, "icon:receipt"),
    DefaultCategory(Res.string.cat_subscriptions, 0xFF7C4DFF, "icon:subscriptions"),
    DefaultCategory(Res.string.cat_transport, 0xFF29B6F6, "icon:bus"),
    DefaultCategory(Res.string.cat_dining, 0xFF8D6E63, "icon:cafe"),
    DefaultCategory(Res.string.cat_clothes, 0xFF26A69A, "icon:clothes"),
    DefaultCategory(Res.string.cat_entertainment, 0xFF5E35D6, "icon:movie"),
    DefaultCategory(Res.string.cat_education, 0xFF5C6BC0, "icon:school"),
    DefaultCategory(Res.string.cat_sport, 0xFF66BB6A, "icon:fitness"),
    DefaultCategory(Res.string.cat_other, 0xFF78909C, "icon:other"),
)
