package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable

// iOS apps must not close themselves (App Store rule): the user simply leaves without accepting.
@Composable
actual fun rememberAppExit(): (() -> Unit)? = null
