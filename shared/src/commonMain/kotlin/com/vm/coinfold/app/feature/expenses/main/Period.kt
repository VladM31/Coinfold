package com.vm.coinfold.app.feature.expenses.main

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Calculation period: [start] inclusive to [endExclusive] exclusive, always exactly one month long. */
data class Period(val start: LocalDate, val endExclusive: LocalDate) {

    val endInclusive: LocalDate get() = endExclusive.minus(1, DateTimeUnit.DAY)

    fun shiftMonths(months: Int): Period = startingAt(start.plus(months, DateTimeUnit.MONTH))

    fun startMillis(timeZone: TimeZone): Long = start.atStartOfDayIn(timeZone).toEpochMilliseconds()

    fun endMillis(timeZone: TimeZone): Long = endExclusive.atStartOfDayIn(timeZone).toEpochMilliseconds()

    companion object {
        /** The period that contains [date] when periods start on day [startDay] (1..28) of each month. */
        fun containing(date: LocalDate, startDay: Int): Period {
            val day = startDay.coerceIn(1, 28)
            val startThisMonth = LocalDate(date.year, date.month, day)
            val start = if (date >= startThisMonth) startThisMonth else startThisMonth.minus(1, DateTimeUnit.MONTH)
            return startingAt(start)
        }

        private fun startingAt(start: LocalDate) = Period(start, start.plus(1, DateTimeUnit.MONTH))
    }
}
