package com.vm.coinfold.app.feature.transactions.domain.services

import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.transactions.domain.models.DayGroup
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Groups items (already sorted newest first) by local day and computes each day subtotal. */
@OptIn(ExperimentalTime::class)
fun groupByDay(
    items: List<TransactionItem>,
    rates: RateTable,
    mainCurrency: Currency,
    timeZone: TimeZone,
): List<DayGroup> {
    val groups = LinkedHashMap<kotlinx.datetime.LocalDate, MutableList<TransactionItem>>()
    for (item in items) {
        val date = Instant.fromEpochMilliseconds(item.dateTime).toLocalDateTime(timeZone).date
        groups.getOrPut(date) { mutableListOf() }.add(item)
    }
    return groups.map { (date, dayItems) ->
        var total = Money.zero(mainCurrency)
        var missing = false
        for (item in dayItems) {
            val converted = rates.convert(item.signedAmount, mainCurrency)
            if (converted == null) missing = true else total += converted.money
        }
        DayGroup(date, dayItems, total, missing)
    }
}
