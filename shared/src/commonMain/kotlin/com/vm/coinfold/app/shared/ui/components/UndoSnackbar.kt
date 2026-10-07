package com.vm.coinfold.app.shared.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_undo
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/** Shows [message] with an "Undo" button; returns true if the user pressed it. */
suspend fun SnackbarHostState.showUndo(message: StringResource): Boolean =
    showSnackbar(
        message = getString(message),
        actionLabel = getString(Res.string.action_undo),
        duration = SnackbarDuration.Long,
    ) == SnackbarResult.ActionPerformed
