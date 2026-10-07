package com.vm.coinfold.app.feature.security.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

/** Solid screen shown while the app is not in the foreground, so the app switcher shows no amounts. */
@Composable
fun PrivacyCover() {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
