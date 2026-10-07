package com.vm.coinfold.app.shared.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual object LocalAppLocale {
    private val systemLanguage: String = (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "en"

    private val current = staticCompositionLocalOf { systemLanguage }

    actual val systemTag: String get() = systemLanguage

    @Composable
    actual infix fun provides(tag: String?): ProvidedValue<*> {
        // Compose resources on iOS read the preferred languages from NSUserDefaults.
        val defaults = NSUserDefaults.standardUserDefaults
        if (tag == null) {
            defaults.removeObjectForKey("AppleLanguages")
        } else {
            defaults.setObject(listOf(tag), forKey = "AppleLanguages")
        }
        return current provides (tag ?: systemLanguage)
    }
}
