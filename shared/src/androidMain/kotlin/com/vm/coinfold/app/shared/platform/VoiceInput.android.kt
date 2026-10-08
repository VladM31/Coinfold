package com.vm.coinfold.app.shared.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred

@Composable
actual fun rememberVoiceInput(): VoiceInput {
    val context = LocalContext.current
    val pending = remember { PendingSpeech() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        } else {
            null
        }
        pending.deferred?.complete(text)
        pending.deferred = null
    }
    return remember(context, launcher) { AndroidVoiceInput(context, launcher, pending) }
}

private class PendingSpeech {
    var deferred: CompletableDeferred<String?>? = null
}

private class AndroidVoiceInput(
    private val context: Context,
    private val launcher: ActivityResultLauncher<Intent>,
    private val pending: PendingSpeech,
) : VoiceInput {

    private fun intent(prompt: String, languageTag: String?) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        if (languageTag != null) putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
    }

    // Needs the RECOGNIZE_SPEECH <queries> entry in the manifest, otherwise Android 11+ hides the recognizer.
    override val isAvailable: Boolean
        get() = intent("", null).resolveActivity(context.packageManager) != null

    override suspend fun listen(prompt: String, languageTag: String?): String? {
        if (!isAvailable) return null
        val deferred = CompletableDeferred<String?>()
        pending.deferred = deferred
        launcher.launch(intent(prompt, languageTag))
        return deferred.await()
    }
}
