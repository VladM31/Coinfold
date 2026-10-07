package com.vm.coinfold.app.feature.backup.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.backup_error_broken
import coinfold.shared.generated.resources.backup_error_duplicate
import coinfold.shared.generated.resources.backup_error_failed
import coinfold.shared.generated.resources.backup_error_invalid
import coinfold.shared.generated.resources.backup_error_newer
import coinfold.shared.generated.resources.backup_error_not_coinfold
import coinfold.shared.generated.resources.backup_error_unreadable
import coinfold.shared.generated.resources.backup_restored
import com.vm.coinfold.app.feature.backup.domain.models.BackupProblem
import com.vm.coinfold.app.feature.backup.domain.models.ParseResult
import com.vm.coinfold.app.feature.backup.domain.models.summary
import com.vm.coinfold.app.feature.backup.domain.services.PdfRenderer
import com.vm.coinfold.app.feature.backup.domain.services.decodeBackup
import com.vm.coinfold.app.feature.backup.domain.usecases.CreateBackupUseCase
import com.vm.coinfold.app.feature.backup.domain.usecases.ExportTransactionsUseCase
import com.vm.coinfold.app.feature.backup.domain.usecases.RestoreBackupUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class BackupViewModel(
    private val createBackup: CreateBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase,
    private val exports: ExportTransactionsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState(pdfSupported = PdfRenderer.isSupported))
    val state: StateFlow<BackupState> = _state.asStateFlow()

    private val effects = Channel<BackupEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    fun onIntent(intent: BackupIntent) {
        when (intent) {
            is BackupIntent.RangeSelected -> _state.update { it.copy(range = intent.range) }
            BackupIntent.ExportBackupClicked -> share { createBackup() }
            BackupIntent.ExportCsvClicked -> share { exports.csv(_state.value.range) }
            BackupIntent.ExportPdfClicked -> share { exports.pdf(_state.value.range) }
            BackupIntent.RestoreClicked -> viewModelScope.launch { effects.send(BackupEffect.PickFile) }
            is BackupIntent.FileChosen -> onFileChosen(intent.bytes)
            BackupIntent.ConfirmRestore -> confirmRestore()
            BackupIntent.DismissRestore -> _state.update { it.copy(restoreCandidate = null) }
        }
    }

    /** Creates a file and hands it to the UI to share; any failure becomes a message instead of a crash. */
    private fun share(create: suspend () -> com.vm.coinfold.app.feature.backup.domain.models.ExportFile) {
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            try {
                effects.send(BackupEffect.ShareFile(create()))
            } catch (e: Exception) {
                effects.send(BackupEffect.ShowMessage(Res.string.backup_error_failed))
            } finally {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }

    private fun onFileChosen(bytes: ByteArray?) {
        if (bytes == null) return // cancelled
        viewModelScope.launch {
            when (val result = decodeBackup(bytes)) {
                is ParseResult.Ok ->
                    _state.update { it.copy(restoreCandidate = RestoreCandidate(result.file, result.file.summary())) }
                is ParseResult.Failed -> effects.send(BackupEffect.ShowMessage(result.problem.message()))
            }
        }
    }

    private fun confirmRestore() {
        val candidate = _state.value.restoreCandidate ?: return
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, restoreCandidate = null) }
            try {
                restoreBackup(candidate.file)
                effects.send(BackupEffect.ShowMessage(Res.string.backup_restored))
            } catch (e: Exception) {
                // The restore runs in one transaction, so the previous data is still intact here.
                effects.send(BackupEffect.ShowMessage(Res.string.backup_error_failed))
            } finally {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }
}

private fun BackupProblem.message(): StringResource = when (this) {
    BackupProblem.UNREADABLE -> Res.string.backup_error_unreadable
    BackupProblem.NOT_COINFOLD -> Res.string.backup_error_not_coinfold
    BackupProblem.NEWER_VERSION -> Res.string.backup_error_newer
    BackupProblem.INVALID_VALUES -> Res.string.backup_error_invalid
    BackupProblem.BROKEN_REFERENCES -> Res.string.backup_error_broken
    BackupProblem.DUPLICATE_IDS -> Res.string.backup_error_duplicate
}
