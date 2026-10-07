package com.vm.coinfold.app.feature.backup.domain.services

import com.vm.coinfold.app.feature.backup.domain.models.ExportRange
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionQuery
import com.vm.coinfold.app.shared.domain.models.Period
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/** The time window of an export as a query: the start is inclusive, the end exclusive. */
fun exportQuery(range: ExportRange, periodStartDay: Int, today: LocalDate, timeZone: TimeZone): TransactionQuery {
    val (from, to) = when (range) {
        ExportRange.ALL -> 0L to Long.MAX_VALUE
        ExportRange.CURRENT_PERIOD -> Period.containing(today, periodStartDay).let { it.startMillis(timeZone) to it.endMillis(timeZone) }
        ExportRange.PREVIOUS_PERIOD ->
            Period.containing(today, periodStartDay).shiftMonths(-1).let { it.startMillis(timeZone) to it.endMillis(timeZone) }
    }
    return TransactionQuery(from, to, accountId = null, categoryId = null, type = null)
}
