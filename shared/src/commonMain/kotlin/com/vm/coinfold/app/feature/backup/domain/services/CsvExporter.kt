package com.vm.coinfold.app.feature.backup.domain.services

import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Same column order in every export, so spreadsheets and scripts can rely on it. */
val CSV_HEADER = listOf(
    "Date", "Time", "Type", "Account", "Category", "Source", "Note",
    "Amount", "Currency", "Account amount", "Account currency", "Rate",
)

/**
 * Transactions as CSV text: a UTF-8 byte order mark first (so Excel reads Cyrillic correctly), comma separated,
 * fields quoted when needed, expenses with a minus sign. Dates are ISO (`2026-03-05`) and times are local.
 */
@OptIn(ExperimentalTime::class)
fun exportCsv(items: List<TransactionItem>, timeZone: TimeZone): String = buildString {
    append('﻿')
    appendLine(CSV_HEADER.joinToString(","))
    for (item in items) {
        val local = Instant.fromEpochMilliseconds(item.dateTime).toLocalDateTime(timeZone)
        val sign = if (item.type == TransactionType.INCOME) 1 else -1
        val row = listOf(
            local.date.toString(),
            "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}",
            item.type.name.lowercase(),
            item.accountName,
            item.category?.name.orEmpty(),
            item.source?.plainName().orEmpty(),
            item.note,
            plainAmount(item.amount, sign),
            item.amount.currency.code,
            plainAmount(item.accountAmount, sign),
            item.accountCurrency.code,
            item.rate.toPlainString(),
        )
        appendLine(row.joinToString(",") { csvField(it) })
    }
}

/** Quotes a field containing a comma, a quote or a line break, and doubles quotes inside it. */
fun csvField(value: String): String =
    if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + value.replace("\"", "\"\"") + "\"" else value

/** `1234.50` / `-0.05`: always the full number of decimals, a dot as separator and no grouping. */
fun plainAmount(money: Money, sign: Int = 1): String {
    var divisor = 1L
    repeat(money.currency.fractionDigits) { divisor *= 10 }
    val value = money.minorUnits * sign
    val abs = if (value < 0) -value else value
    val whole = abs / divisor
    val fraction = (abs % divisor).toString().padStart(money.currency.fractionDigits, '0')
    val number = if (money.currency.fractionDigits > 0) "$whole.$fraction" else "$whole"
    return if (value < 0) "-$number" else number
}

/** English, human readable name of an income source for files (the UI shows localized names instead). */
fun IncomeSource.plainName(): String = when (this) {
    is IncomeSource.Preset -> key.replace('_', ' ').replaceFirstChar { it.uppercase() }
    is IncomeSource.Custom -> name
}
