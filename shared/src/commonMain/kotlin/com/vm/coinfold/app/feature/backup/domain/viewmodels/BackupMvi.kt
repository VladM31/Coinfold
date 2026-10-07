package com.vm.coinfold.app.feature.backup.domain.viewmodels

import com.vm.coinfold.app.feature.backup.domain.models.BackupFile
import com.vm.coinfold.app.feature.backup.domain.models.BackupSummary
import com.vm.coinfold.app.feature.backup.domain.models.ExportFile
import com.vm.coinfold.app.feature.backup.domain.models.ExportRange
import org.jetbrains.compose.resources.StringResource

data class BackupState(
    val range: ExportRange = ExportRange.ALL,
    /** True while a file is being created or restored. */
    val isBusy: Boolean = false,
    val pdfSupported: Boolean = false,
    /** Set after the user picked a valid backup: waits for the confirmation to replace the current data. */
    val restoreCandidate: RestoreCandidate? = null,
)

class RestoreCandidate(val file: BackupFile, val summary: BackupSummary)

sealed interface BackupIntent {
    data class RangeSelected(val range: ExportRange) : BackupIntent
    data object ExportBackupClicked : BackupIntent
    data object ExportCsvClicked : BackupIntent
    data object ExportPdfClicked : BackupIntent
    data object RestoreClicked : BackupIntent

    /** The result of the file picker; null if the user cancelled. */
    class FileChosen(val bytes: ByteArray?) : BackupIntent
    data object ConfirmRestore : BackupIntent
    data object DismissRestore : BackupIntent
}

sealed interface BackupEffect {
    class ShareFile(val file: ExportFile) : BackupEffect
    data object PickFile : BackupEffect
    data class ShowMessage(val message: StringResource) : BackupEffect
}
