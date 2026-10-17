package com.vm.coinfold.app.feature.policy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.language_en
import coinfold.shared.generated.resources.language_system
import coinfold.shared.generated.resources.language_uk
import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.ui.components.OptionChips
import coinfold.shared.generated.resources.policy_accept
import coinfold.shared.generated.resources.policy_decline
import coinfold.shared.generated.resources.policy_message
import coinfold.shared.generated.resources.policy_offline
import coinfold.shared.generated.resources.policy_read
import coinfold.shared.generated.resources.policy_retry
import coinfold.shared.generated.resources.policy_title
import com.vm.coinfold.app.feature.policy.domain.viewmodels.PolicyState
import org.jetbrains.compose.resources.stringResource

/**
 * Shown instead of the app until the current privacy policy is accepted.
 * [onDecline] is null where the app cannot close itself, then the button is not shown.
 */
@Composable
fun PolicyGateScreen(
    state: PolicyState,
    onAccept: () -> Unit,
    onDecline: (() -> Unit)?,
    onRetry: () -> Unit,
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The policy must be readable before the user can change anything else.
            OptionChips(
                options = listOf(
                    AppLanguage.SYSTEM to stringResource(Res.string.language_system),
                    AppLanguage.EN to stringResource(Res.string.language_en),
                    AppLanguage.UK to stringResource(Res.string.language_uk),
                ),
                selected = language,
                onSelected = onLanguageSelected,
                modifier = Modifier.padding(bottom = 32.dp),
            )
            Text(stringResource(Res.string.policy_title), style = MaterialTheme.typography.headlineSmall)
            val policy = state.policy
            if (policy == null) {
                // The policy comes from the internet, so the very first launch needs a connection.
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.padding(top = 24.dp))
                } else {
                    Text(
                        stringResource(Res.string.policy_offline),
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = onRetry) { Text(stringResource(Res.string.policy_retry)) }
                    onDecline?.let {
                        TextButton(onClick = it) { Text(stringResource(Res.string.policy_decline)) }
                    }
                }
            } else {
                Text(
                    stringResource(Res.string.policy_message),
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = { uriHandler.openUri(policy.url) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.policy_read))
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) { Text(stringResource(Res.string.policy_accept)) }
                onDecline?.let {
                    TextButton(onClick = it, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.policy_decline))
                    }
                }
            }
        }
    }
}
