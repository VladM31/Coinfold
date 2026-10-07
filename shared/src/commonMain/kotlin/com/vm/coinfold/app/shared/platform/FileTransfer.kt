package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable

/** Gets files out of the app (share sheet) and into it (file picker), the way each platform does it. */
interface FileTransfer {
    /** Opens the system share sheet for a file with the given content. */
    suspend fun share(fileName: String, mimeType: String, bytes: ByteArray)

    /** Lets the user pick a file; returns its content, or null if they cancelled. */
    suspend fun pickFile(): ByteArray?
}

/** Needs the UI context on each platform (Activity result launchers on Android, the view controller on iOS). */
@Composable
expect fun rememberFileTransfer(): FileTransfer
