package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// Speech recognition on iOS (SFSpeechRecognizer with microphone permission) is not implemented yet,
// so the microphone button is simply not offered there.
@Composable
actual fun rememberVoiceInput(): VoiceInput = remember { NoVoiceInput }

private object NoVoiceInput : VoiceInput {
    override val isAvailable: Boolean = false
    override suspend fun listen(prompt: String, languageTag: String?): String? = null
}
