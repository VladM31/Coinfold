package com.vm.coinfold.app.feature.policy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import com.vm.coinfold.app.feature.policy.ui.components.GateButton
import coinfold.shared.generated.resources.update_button
import coinfold.shared.generated.resources.update_close
import coinfold.shared.generated.resources.update_message
import coinfold.shared.generated.resources.update_title
import org.jetbrains.compose.resources.stringResource

/**
 * Shown instead of the app when this version is no longer supported.
 * [updateUrl] is null until the app is published, then there is only the message.
 * [onClose] is null where the app cannot close itself.
 */
@Composable
fun UpdateRequiredScreen(updateUrl: String?, onClose: (() -> Unit)?) {
    val uriHandler = LocalUriHandler.current
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(Res.string.update_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(Res.string.update_message),
                modifier = Modifier.padding(vertical = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Column(
                Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                updateUrl?.let {
                    GateButton(stringResource(Res.string.update_button), { uriHandler.openUri(it) }, primary = true)
                }
                onClose?.let { GateButton(stringResource(Res.string.update_close), it) }
            }
        }
    }
}
