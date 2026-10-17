package com.vm.coinfold.app.shared.platform

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

private const val CHROME_PACKAGE = "com.android.chrome"

@Composable
actual fun rememberWebPageOpener(): (url: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { url ->
            val view = Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(Intent(view).setPackage(CHROME_PACKAGE))
            } catch (_: ActivityNotFoundException) {
                // no Chrome on this device: the default browser
                runCatching { context.startActivity(view) }
            }
        }
    }
}
