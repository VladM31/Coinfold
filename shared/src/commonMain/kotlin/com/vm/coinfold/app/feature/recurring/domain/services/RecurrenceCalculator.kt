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
    Frequency.EVERY_N_DAYS -> date.plus(anchorDay.coerceAtLeast(1), DateTimeUnit.DAY)
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

/** The first date on or after [from] that falls on [day] of a month (the last day of months that are shorter). */
fun firstMonthlyDate(from: LocalDate, day: Int): LocalDate {
    val thisMonth = LocalDate(from.year, from.month, minOf(day, lengthOfMonth(from.year, from.month.number)))
    if (thisMonth >= from) return thisMonth
    val next = from.plus(1, DateTimeUnit.MONTH)
    return LocalDate(next.year, next.month, minOf(day, lengthOfMonth(next.year, next.month.number)))
}

private fun lengthOfMonth(year: Int, month: Int): Int {
    val first = LocalDate(year, Month(month), 1)
    return first.daysUntil(first.plus(1, DateTimeUnit.MONTH))
}
