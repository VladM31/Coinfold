package com.vm.coinfold.app.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalTime::class)
fun today(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Clock.System.now().toLocalDateTime(timeZone).date

/**
 * Timestamp to store for a transaction on [date]: the current moment if it is today (keeps the
 * order of same-day entries), otherwise noon local time of that day.
 */
@OptIn(ExperimentalTime::class)
fun epochMillisFor(
    date: LocalDate,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: Instant = Clock.System.now(),
): Long =
    if (date == now.toLocalDateTime(timeZone).date) {
        now.toEpochMilliseconds()
    } else {
        date.atTime(12, 0).toInstant(timeZone).toEpochMilliseconds()
    }

/** Material DatePicker reports the selected day as UTC midnight millis. */
@OptIn(ExperimentalTime::class)
fun dateFromPickerMillis(millis: Long): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date

@OptIn(ExperimentalTime::class)
fun pickerMillisFor(date: LocalDate): Long = date.atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()

/** `dd.MM.yyyy` — same in both supported languages. */
fun LocalDate.format(): String =
    "${day.toString().padStart(2, '0')}.${month.number.toString().padStart(2, '0')}.$year"
