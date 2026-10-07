package com.vm.coinfold.app.feature.expenses.domain.models

data class Category(
    val id: Long,
    val name: String,
    /** ARGB color. */
    val color: Long,
    /** Built-in icon: one of [CategoryIcons]. */
    val icon: String,
)

/** Built-in icon set (emoji render the same on Android and iOS and need no extra assets). */
object CategoryIcons {
    val all: List<String> = listOf(
        "🛒", "🍔", "☕", "🚌", "🚗", "🏠",
        "💡", "📱", "🎬", "👕", "💊", "🎁",
        "✈️", "🐾", "🎓", "🏋️", "💼", "📦",
    )
    val default: String = all.first()
}

enum class CategoryDeleteResult { DELETED, ARCHIVED }
