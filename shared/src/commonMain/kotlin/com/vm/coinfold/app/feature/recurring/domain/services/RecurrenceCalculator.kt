package com.vm.coinfold.app.feature.recurring.domain.services

import com.vm.coinfold.app.feature.recurring.domain.models.Frequency
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.daysUntil
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * The day after [date] on which a payment recurs. Monthly and yearly payments try to stay on [anchorDay]
 * (for example the 31st) and use the last day of shorter months, without drifting to the 28th forever.
 */
fun nextOccurrence(date: LocalDate, frequency: Frequency, anchorDay: Int): LocalDate = when (frequency) {
    Frequency.DAILY -> date.plus(1, DateTimeUnit.DAY)
    Frequency.WEEKLY -> date.plus(7, DateTimeUnit.DAY)
    Frequency.MONTHLY -> {
        val month = date.plus(1, DateTimeUnit.MONTH)
        LocalDate(month.year, month.month, minOf(anchorDay, lengthOfMonth(month.year, month.month.number)))
    }
    Frequency.YEARLY -> {
        val year = date.year + 1
        LocalDate(year, date.month, minOf(anchorDay, lengthOfMonth(year, date.month.number)))
    }
}

private fun lengthOfMonth(year: Int, month: Int): Int {
    val first = LocalDate(year, Month(month), 1)
    return first.daysUntil(first.plus(1, DateTimeUnit.MONTH))
}
