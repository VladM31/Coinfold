package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable

/** Closes the app, or null where the platform does not let an app close itself (iOS). */
@Composable
expect fun rememberAppExit(): (() -> Unit)?
