package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable

/** Speech to text through the system recognizer. */
interface VoiceInput {
    /** True if the device can recognize speech and the app may use it. */
    val isAvailable: Boolean

    /**
     * Opens the system speech dialog and returns what the user said, or null if they cancelled or nothing was
     * recognized. [languageTag] (for example "uk-UA") is a hint for the recognizer.
     */
    suspend fun listen(prompt: String, languageTag: String?): String?
}

@Composable
expect fun rememberVoiceInput(): VoiceInput
