package com.vm.coinfold.app.feature.expenses.domain.models

data class Category(
    val id: Long,
    val name: String,
    /** ARGB color. */
    val color: Long,
    /** Built-in icon: one of [CategoryIcons]. */
    val icon: String,
)

/** Icon defaults; the pickable icons live in the shared UI (vector catalog and emoji set). */
object CategoryIcons {
    /** Default for new categories: the shopping cart from the vector catalog (stored as `icon:<key>`). */
    const val default: String = "icon:cart"
}

enum class CategoryDeleteResult { DELETED, ARCHIVED }
