package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler

@Composable
actual fun rememberWebPageOpener(): (url: String) -> Unit {
    val uriHandler = LocalUriHandler.current
    return remember(uriHandler) { { url -> uriHandler.openUri(url) } }
}
