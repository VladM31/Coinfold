package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable

// iOS draws the status bar on its own and the app follows the device appearance there.
@Composable
actual fun SystemBarsEffect(darkTheme: Boolean) = Unit
