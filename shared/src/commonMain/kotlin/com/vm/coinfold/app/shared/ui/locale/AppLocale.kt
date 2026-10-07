package com.vm.coinfold.app.shared.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Lets the app override the language of Compose Multiplatform resources at runtime,
 * without restarting. Use as `CompositionLocalProvider(LocalAppLocale provides tag) { ... }`
 * and re-create the content (for example with `key(tag)`) when the tag changes.
 */
expect object LocalAppLocale {
    /** Language tag of the device captured before any override, e.g. "uk-UA" or "en". */
    val systemTag: String

    /** [tag] is a BCP 47 tag such as "uk"; null restores the device language. */
    @Composable
    infix fun provides(tag: String?): ProvidedValue<*>
}
