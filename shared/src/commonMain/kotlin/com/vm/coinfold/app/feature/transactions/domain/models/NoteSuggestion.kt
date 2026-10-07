package com.vm.coinfold.app.feature.transactions.domain.models

/** A note used before, with the category it was used with ([categoryId] null = none) and how often. */
data class NoteSuggestion(val note: String, val categoryId: Long?, val uses: Int, val lastUsed: Long)
