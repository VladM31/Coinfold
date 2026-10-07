package com.vm.coinfold.app.shared.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.placeholder_coming_soon
import org.jetbrains.compose.resources.stringResource

/** Temporary stub for screens that are not built yet. */
@Composable
fun ComingSoon(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = title + "\n" + stringResource(Res.string.placeholder_coming_soon),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
