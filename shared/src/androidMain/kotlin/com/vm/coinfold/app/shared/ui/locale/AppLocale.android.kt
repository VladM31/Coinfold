package com.vm.coinfold.app.shared.ui.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    // Captured on first access, which happens before any override is applied.
    private val systemLocale: Locale = Locale.getDefault()

    actual val systemTag: String get() = systemLocale.toLanguageTag()

    @Suppress("DEPRECATION")
    @Composable
    actual infix fun provides(tag: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current
        val resources = LocalContext.current.resources
        val locale = if (tag == null) systemLocale else Locale.forLanguageTag(tag)

        Locale.setDefault(locale)
        val updated = Configuration(configuration).apply { setLocale(locale) }
        resources.updateConfiguration(updated, resources.displayMetrics)
        return LocalConfiguration.provides(updated)
    }
}
