package com.vm.coinfold.app.feature.transactions.domain.services

import com.vm.coinfold.app.feature.transactions.domain.models.NoteSuggestion

/**
 * Notes to offer while the user types. Matches contain the typed text (case-insensitive); notes that start
 * with it come first, then notes used with [categoryId], then the most used and most recent. An empty query
 * offers the notes most used with the category. The exact text already typed is never suggested.
 */
fun suggestNotes(all: List<NoteSuggestion>, query: String, categoryId: Long?, limit: Int = 4): List<String> {
    val q = query.trim().lowercase()
    return all
        .asSequence()
        .filter { it.note.lowercase() != q && (q.isEmpty() || it.note.lowercase().contains(q)) }
        // An empty query only makes sense within the category being filled in.
        .filter { q.isNotEmpty() || categoryId == null || it.categoryId == categoryId }
        .sortedWith(
            compareByDescending<NoteSuggestion> { it.note.lowercase().startsWith(q) }
                .thenByDescending { it.categoryId == categoryId }
                .thenByDescending { it.uses }
                .thenByDescending { it.lastUsed },
        )
        .map { it.note }
        .distinctBy { it.lowercase() }
        .take(limit)
        .toList()
}

/**
 * The category that usually goes with the typed text: the category most often used with a note equal to it,
 * or (from 3 characters) starting with it. Null when nothing matches well enough.
 */
fun suggestCategory(all: List<NoteSuggestion>, query: String): Long? {
    val q = query.trim().lowercase()
    if (q.length < 2) return null
    return all
        .filter { it.categoryId != null }
        .filter { val n = it.note.lowercase(); n == q || (q.length >= 3 && n.startsWith(q)) }
        .groupBy { it.categoryId }
        .maxByOrNull { (_, rows) -> rows.sumOf { it.uses } }
        ?.key
}
