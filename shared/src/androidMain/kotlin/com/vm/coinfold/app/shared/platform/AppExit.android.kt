package com.vm.coinfold.app.shared.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberAppExit(): (() -> Unit)? {
    val context = LocalContext.current
    return remember(context) {
        val activity = generateSequence(context) { (it as? ContextWrapper)?.baseContext }.filterIsInstance<Activity>().firstOrNull()
        activity?.let { { it.finishAffinity() } }
    }
}
