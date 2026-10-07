package com.vm.coinfold.app.feature.backup.domain.usecases

import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.report_balance
import coinfold.shared.generated.resources.report_col_account
import coinfold.shared.generated.resources.report_col_amount
import coinfold.shared.generated.resources.report_col_category
import coinfold.shared.generated.resources.report_col_date
import coinfold.shared.generated.resources.report_col_in_account
import coinfold.shared.generated.resources.report_col_note
import coinfold.shared.generated.resources.report_count
import coinfold.shared.generated.resources.report_expenses
import coinfold.shared.generated.resources.report_generated
import coinfold.shared.generated.resources.report_income
import coinfold.shared.generated.resources.report_range_all
import coinfold.shared.generated.resources.report_range_current
import coinfold.shared.generated.resources.report_range_previous
import coinfold.shared.generated.resources.report_title
import coinfold.shared.generated.resources.transaction_manual_withdrawal
import com.vm.coinfold.app.feature.backup.domain.models.ExportFile
import com.vm.coinfold.app.feature.backup.domain.models.ExportRange
import com.vm.coinfold.app.feature.backup.domain.models.ReportColumn
import com.vm.coinfold.app.feature.backup.domain.models.ReportDocument
import com.vm.coinfold.app.feature.backup.domain.services.PdfRenderer
import com.vm.coinfold.app.feature.backup.domain.services.exportCsv
import com.vm.coinfold.app.feature.backup.domain.services.exportQuery
import com.vm.coinfold.app.feature.backup.domain.services.plainAmount
import com.vm.coinfold.app.feature.backup.domain.services.plainName
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.utils.format
import com.vm.coinfold.app.utils.today
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString

/** Exports the transactions of a period as CSV (for spreadsheets) or as a printable PDF report. */
class ExportTransactionsUseCase(
    private val transactions: TransactionRepository,
    private val settings: SettingsRepository,
    private val currencies: CurrencyRepository,
) {
    suspend fun csv(range: ExportRange): ExportFile {
        val items = load(range)
        val text = exportCsv(items, TimeZone.currentSystemDefault())
        return ExportFile("coinfold-transactions-${today()}.csv", "text/csv", text.encodeToByteArray())
    }

    suspend fun pdf(range: ExportRange): ExportFile {
        val items = load(range)
        val document = buildReport(items, range)
        return ExportFile("coinfold-report-${today()}.pdf", "application/pdf", PdfRenderer.render(document))
    }

    private suspend fun load(range: ExportRange): List<TransactionItem> {
        val startDay = settings.settings.first().periodStartDay
        val query = exportQuery(range, startDay, today(), TimeZone.currentSystemDefault())
        // Oldest first reads naturally in a spreadsheet or on paper.
        return transactions.observeItems(query, Int.MAX_VALUE).first().asReversed()
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun buildReport(items: List<TransactionItem>, range: ExportRange): ReportDocument {
        val current = settings.settings.first()
        val rates = currencies.rateTable.first()
        val main = current.mainCurrency
        var income = Money.zero(main)
        var expenses = Money.zero(main)
        for (item in items) {
            // Totals use today's rates; amounts without any known rate are left out of the totals.
            val converted = rates.convert(item.amount, main)?.money ?: continue
            if (item.type == TransactionType.INCOME) income += converted else expenses += converted
        }
        val period = Period.containing(today(), current.periodStartDay)
        val rangeLabel = when (range) {
            ExportRange.ALL -> getString(Res.string.report_range_all)
            ExportRange.CURRENT_PERIOD -> "${getString(Res.string.report_range_current)}: ${period.start.format()} – ${period.endInclusive.format()}"
            ExportRange.PREVIOUS_PERIOD -> period.shiftMonths(-1).let {
                "${getString(Res.string.report_range_previous)}: ${it.start.format()} – ${it.endInclusive.format()}"
            }
        }
        val withdrawal = getString(Res.string.transaction_manual_withdrawal)
        return ReportDocument(
            title = getString(Res.string.report_title),
            subtitle = "$rangeLabel · ${getString(Res.string.report_generated, today().format())}",
            summaryLines = listOf(
                "${getString(Res.string.report_income)}: ${plainAmount(income)} ${main.code}",
                "${getString(Res.string.report_expenses)}: ${plainAmount(expenses)} ${main.code}",
                "${getString(Res.string.report_balance)}: ${plainAmount(income - expenses)} ${main.code}",
                getString(Res.string.report_count, items.size.toString()),
            ),
            columns = listOf(
                ReportColumn(getString(Res.string.report_col_date), 1.0f),
                ReportColumn(getString(Res.string.report_col_account), 1.3f),
                ReportColumn(getString(Res.string.report_col_category), 1.6f),
                ReportColumn(getString(Res.string.report_col_note), 2.2f),
                ReportColumn(getString(Res.string.report_col_amount), 1.3f, alignRight = true),
                ReportColumn(getString(Res.string.report_col_in_account), 1.3f, alignRight = true),
            ),
            rows = items.map { item ->
                val sign = if (item.type == TransactionType.INCOME) 1 else -1
                val local = Instant.fromEpochMilliseconds(item.dateTime)
                listOf(
                    localDate(local).format(),
                    item.accountName,
                    item.category?.name ?: item.source?.plainName() ?: withdrawal,
                    item.note,
                    "${plainAmount(item.amount, sign)} ${item.amount.currency.code}",
                    "${plainAmount(item.accountAmount, sign)} ${item.accountCurrency.code}",
                )
            },
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun localDate(instant: Instant) = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
