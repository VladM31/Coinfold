package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable

/**
 * Makes the status and navigation bar icons readable on the app's own theme: dark icons on a light app,
 * light icons on a dark app. Without it they follow the device theme, which can differ from the one chosen in Settings.
 */
@Composable
expect fun SystemBarsEffect(darkTheme: Boolean)
