package com.vm.coinfold.app.feature.backup.domain.models

/** A file ready to be shared. */
class ExportFile(val name: String, val mimeType: String, val bytes: ByteArray)

/** Which transactions go into a CSV or PDF export. Periods follow the "period start day" setting. */
enum class ExportRange { ALL, CURRENT_PERIOD, PREVIOUS_PERIOD }

/** Why a file could not be used as a backup. */
enum class BackupProblem {
    /** Not readable JSON of the expected shape. */
    UNREADABLE,

    /** Valid JSON, but not a Coinfold backup. */
    NOT_COINFOLD,

    /** Made by a newer version of the app than this one understands. */
    NEWER_VERSION,

    /** Contains values that cannot be loaded (unknown currency, bad rate or date, ...). */
    INVALID_VALUES,

    /** Contains transactions or schedules that point to accounts or categories that are not in the file. */
    BROKEN_REFERENCES,

    /** The same id appears twice in one list. */
    DUPLICATE_IDS,
}

sealed interface ParseResult {
    data class Ok(val file: BackupFile) : ParseResult
    data class Failed(val problem: BackupProblem) : ParseResult
}

/** A table-like report for the PDF export; the platform draws it. */
class ReportDocument(
    val title: String,
    val subtitle: String,
    val summaryLines: List<String>,
    val columns: List<ReportColumn>,
    val rows: List<List<String>>,
)

/** [weight] is the relative width of the column; [alignRight] is for amounts. */
class ReportColumn(val title: String, val weight: Float, val alignRight: Boolean = false)
