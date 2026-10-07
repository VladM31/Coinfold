package com.vm.coinfold.app.feature.backup.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.backup_create
import coinfold.shared.generated.resources.backup_restore
import coinfold.shared.generated.resources.backup_restore_confirm
import coinfold.shared.generated.resources.backup_restore_message
import coinfold.shared.generated.resources.backup_restore_title
import coinfold.shared.generated.resources.backup_section_backup
import coinfold.shared.generated.resources.backup_section_backup_hint
import coinfold.shared.generated.resources.backup_section_export
import coinfold.shared.generated.resources.backup_section_export_hint
import coinfold.shared.generated.resources.backup_title
import coinfold.shared.generated.resources.export_csv
import coinfold.shared.generated.resources.export_pdf
import coinfold.shared.generated.resources.export_range_all
import coinfold.shared.generated.resources.export_range_current
import coinfold.shared.generated.resources.export_range_previous
import com.vm.coinfold.app.feature.backup.domain.models.ExportRange
import com.vm.coinfold.app.feature.backup.domain.viewmodels.BackupEffect
import com.vm.coinfold.app.feature.backup.domain.viewmodels.BackupIntent
import com.vm.coinfold.app.feature.backup.domain.viewmodels.BackupState
import com.vm.coinfold.app.feature.backup.domain.viewmodels.BackupViewModel
import com.vm.coinfold.app.feature.backup.ui.components.BackupSection
import com.vm.coinfold.app.shared.platform.FileTransfer
import com.vm.coinfold.app.shared.platform.rememberFileTransfer
import com.vm.coinfold.app.utils.formatDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Reached from Settings; [onBack] returns there. */
@Composable
fun BackupScreen(onBack: () -> Unit, viewModel: BackupViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val transfer = rememberFileTransfer()

    LaunchedEffect(viewModel, transfer) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackupEffect.ShareFile -> transfer.share(effect.file.name, effect.file.mimeType, effect.file.bytes)
                BackupEffect.PickFile -> viewModel.onIntent(BackupIntent.FileChosen(transfer.pickFile()))
                is BackupEffect.ShowMessage -> snackbar.showSnackbar(getString(effect.message))
            }
        }
    }

    BackupContent(state, viewModel::onIntent, onBack, snackbar)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackupContent(
    state: BackupState,
    onIntent: (BackupIntent) -> Unit,
    onBack: () -> Unit,
    snackbar: SnackbarHostState,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineSmall) }
                Text(stringResource(Res.string.backup_title), style = MaterialTheme.typography.titleLarge)
            }
            if (state.isBusy) LinearProgressIndicator(Modifier.fillMaxWidth())

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                BackupSection(
                    title = stringResource(Res.string.backup_section_backup),
                    hint = stringResource(Res.string.backup_section_backup_hint),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onIntent(BackupIntent.ExportBackupClicked) }, enabled = !state.isBusy) {
                            Text(stringResource(Res.string.backup_create))
                        }
                        OutlinedButton(onClick = { onIntent(BackupIntent.RestoreClicked) }, enabled = !state.isBusy) {
                            Text(stringResource(Res.string.backup_restore))
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                BackupSection(
                    title = stringResource(Res.string.backup_section_export),
                    hint = stringResource(Res.string.backup_section_export_hint),
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ExportRange.ALL to Res.string.export_range_all,
                            ExportRange.CURRENT_PERIOD to Res.string.export_range_current,
                            ExportRange.PREVIOUS_PERIOD to Res.string.export_range_previous,
                        ).forEach { (range, label) ->
                            FilterChip(
                                selected = state.range == range,
                                onClick = { onIntent(BackupIntent.RangeSelected(range)) },
                                label = { Text(stringResource(label)) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onIntent(BackupIntent.ExportCsvClicked) }, enabled = !state.isBusy) {
                            Text(stringResource(Res.string.export_csv))
                        }
                        if (state.pdfSupported) {
                            OutlinedButton(onClick = { onIntent(BackupIntent.ExportPdfClicked) }, enabled = !state.isBusy) {
                                Text(stringResource(Res.string.export_pdf))
                            }
                        }
                    }
                }
            }
        }
    }

    state.restoreCandidate?.let { candidate ->
        val s = candidate.summary
        AlertDialog(
            onDismissRequest = { onIntent(BackupIntent.DismissRestore) },
            title = { Text(stringResource(Res.string.backup_restore_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.backup_restore_message,
                        formatDateTime(s.createdAt),
                        s.accounts.toString(),
                        s.categories.toString(),
                        s.transactions.toString(),
                        s.recurring.toString(),
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { onIntent(BackupIntent.ConfirmRestore) }) {
                    Text(stringResource(Res.string.backup_restore_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(BackupIntent.DismissRestore) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}
