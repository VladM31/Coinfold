package com.vm.coinfold.app.feature.recurring.domain.repositories

import com.vm.coinfold.app.feature.recurring.domain.models.RecurringDraft
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface RecurringRepository {
    /** Active schedules first, soonest due date first. */
    val payments: Flow<List<RecurringPayment>>

    suspend fun save(draft: RecurringDraft)
    suspend fun setActive(id: Long, active: Boolean)

    /** Removes the schedule; [undoDelete] brings back the last removed one. */
    suspend fun delete(id: Long)
    suspend fun undoDelete()

    /** Active schedules due on or before [today]. */
    suspend fun due(today: LocalDate): List<RecurringPayment>

    suspend fun setNextDate(id: Long, nextDate: LocalDate)
}
