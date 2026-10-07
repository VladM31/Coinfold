package com.vm.coinfold.app.feature.transactions.db.entities

/** How often a note was used with a category (null = no category), and when last. */
data class NoteStatRow(
    val note: String,
    val categoryId: Long?,
    val uses: Int,
    val lastUsed: Long,
)
