package com.vm.coinfold.app.shared.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberFileTransfer(): FileTransfer {
    val context = LocalContext.current
    // The launcher callback has to find the request that is waiting for it.
    val pending = remember { PendingPick() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pending.deferred?.complete(uri)
        pending.deferred = null
    }
    return remember(context, launcher) { AndroidFileTransfer(context, launcher, pending) }
}

private class PendingPick {
    var deferred: CompletableDeferred<Uri?>? = null
}

private class AndroidFileTransfer(
    private val context: Context,
    private val launcher: ActivityResultLauncher<Array<String>>,
    private val pending: PendingPick,
) : FileTransfer {

    override suspend fun share(fileName: String, mimeType: String, bytes: ByteArray) {
        val file = withContext(Dispatchers.IO) {
            File(context.cacheDir, "exports").apply { mkdirs() }.resolve(fileName).also { it.writeBytes(bytes) }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override suspend fun pickFile(): ByteArray? {
        val deferred = CompletableDeferred<Uri?>()
        pending.deferred = deferred
        // Backups are JSON, but some file managers report other types, so do not filter too strictly.
        launcher.launch(arrayOf("application/json", "text/plain", "application/octet-stream", "*/*"))
        val uri = deferred.await() ?: return null
        return withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
    }
}
