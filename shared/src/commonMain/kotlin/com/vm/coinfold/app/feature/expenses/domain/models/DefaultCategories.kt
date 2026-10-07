package com.vm.coinfold.app.feature.expenses.domain.models

import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.cat_clothes
import coinfold.shared.generated.resources.cat_dining
import coinfold.shared.generated.resources.cat_entertainment
import coinfold.shared.generated.resources.cat_groceries
import coinfold.shared.generated.resources.cat_health
import coinfold.shared.generated.resources.cat_home
import coinfold.shared.generated.resources.cat_other
import coinfold.shared.generated.resources.cat_transport
import org.jetbrains.compose.resources.StringResource

data class DefaultCategory(val name: StringResource, val color: Long, val icon: String)

/** Categories created on the very first launch; the user can rename or remove them freely. */
val DefaultCategories: List<DefaultCategory> = listOf(
    DefaultCategory(Res.string.cat_groceries, 0xFF8BC34A, "🛒"),
    DefaultCategory(Res.string.cat_dining, 0xFFFF7043, "🍔"),
    DefaultCategory(Res.string.cat_transport, 0xFF29B6F6, "🚌"),
    DefaultCategory(Res.string.cat_home, 0xFF7C4DFF, "🏠"),
    DefaultCategory(Res.string.cat_health, 0xFFE91E63, "💊"),
    DefaultCategory(Res.string.cat_entertainment, 0xFFAB47BC, "🎬"),
    DefaultCategory(Res.string.cat_clothes, 0xFFFFB300, "👕"),
    DefaultCategory(Res.string.cat_other, 0xFF78909C, "📦"),
)
