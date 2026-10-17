package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable

/**
 * Opens a web page for reading (the privacy policy). On Android it tries Chrome first, because some
 * vendor browsers show such pages badly, and falls back to the default browser.
 */
@Composable
expect fun rememberWebPageOpener(): (url: String) -> Unit
