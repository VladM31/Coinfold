package com.vm.coinfold.app.feature.transactions.domain.services

import com.vm.coinfold.app.feature.transactions.domain.models.NoteSuggestion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NoteSuggestionEngineTest {
    private fun n(note: String, category: Long?, uses: Int, last: Long = 0) = NoteSuggestion(note, category, uses, last)

    private val history = listOf(
        n("Silpo", 1, 12),
        n("Silpo Kyiv", 1, 3),
        n("Sushi", 2, 5),
        n("Taxi", 3, 8),
        n("silpo", 4, 1), // same word typed in lower case once with another category
    )

    @Test
    fun matchesContainTheTypedTextAndPrefixesComeFirst() {
        // "Sushi" does not contain "ilp"; "Silpo Kyiv" and "Silpo" do
        assertEquals(listOf("Silpo", "Silpo Kyiv"), suggestNotes(history, "ilp", categoryId = 1))
        // a prefix match wins over a mere "contains" match
        val mixed = listOf(n("Big Silpo", 1, 50), n("Silpo", 1, 2))
        assertEquals(listOf("Silpo", "Big Silpo"), suggestNotes(mixed, "sil", categoryId = 1))
    }

    @Test
    fun theExactTextAlreadyTypedIsNotOffered() {
        assertEquals(listOf("Silpo Kyiv"), suggestNotes(history, "Silpo", categoryId = 1))
    }

    @Test
    fun emptyQueryOffersTheMostUsedNotesOfTheCategory() {
        assertEquals(listOf("Silpo", "Silpo Kyiv"), suggestNotes(history, "", categoryId = 1))
        assertEquals(listOf("Taxi"), suggestNotes(history, "", categoryId = 3))
    }

    @Test
    fun sameNoteInDifferentCaseIsListedOnce() {
        assertEquals(1, suggestNotes(history, "silp", categoryId = 4).count { it.equals("silpo", ignoreCase = true) })
    }

    @Test
    fun resultsAreLimited() {
        val many = (1..10).map { n("Shop $it", 1, it) }
        assertEquals(4, suggestNotes(many, "shop", categoryId = 1).size)
    }

    @Test
    fun categoryIsSuggestedFromTheMostUsedMatch() {
        assertEquals(1L, suggestCategory(history, "Silpo")) // 12 + ... uses in category 1 beat 1 in category 4
        assertEquals(1L, suggestCategory(history, "silpo k")) // prefix of "Silpo Kyiv", from 3 characters
        assertEquals(3L, suggestCategory(history, "taxi"))
    }

    @Test
    fun noSuggestionForShortOrUnknownText() {
        assertNull(suggestCategory(history, "s")) // too short
        assertNull(suggestCategory(history, "zzz"))
        // two characters must match a whole note, not just a prefix
        assertNull(suggestCategory(history, "si"))
    }

    @Test
    fun notesWithoutACategoryNeverSuggestOne() {
        assertNull(suggestCategory(listOf(n("Gift", null, 9)), "gift"))
    }
}
