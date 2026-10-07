package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberFileTransfer(): FileTransfer = remember { IosFileTransfer() }

@OptIn(ExperimentalForeignApi::class)
private class IosFileTransfer : FileTransfer {
    // UIKit keeps only a weak reference to a picker delegate, so it is held here while the picker is open.
    private var pickerDelegate: PickerDelegate? = null

    override suspend fun share(fileName: String, mimeType: String, bytes: ByteArray) {
        val url = NSURL.fileURLWithPath(NSTemporaryDirectory() + fileName)
        bytes.toNSData().writeToURL(url, atomically = true)
        val controller = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
        topViewController()?.presentViewController(controller, animated = true, completion = null)
    }

    override suspend fun pickFile(): ByteArray? {
        val result = CompletableDeferred<ByteArray?>()
        val delegate = PickerDelegate(result)
        pickerDelegate = delegate
        val picker = UIDocumentPickerViewController(
            documentTypes = listOf("public.json", "public.text", "public.data"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
        )
        picker.delegate = delegate
        topViewController()?.presentViewController(picker, animated = true, completion = null)
        return try {
            result.await()
        } finally {
            pickerDelegate = null
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PickerDelegate(private val result: CompletableDeferred<ByteArray?>) :
    NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        // Files outside the app sandbox need explicit access.
        val scoped = url?.startAccessingSecurityScopedResource() ?: false
        val data = url?.let { NSData.dataWithContentsOfURL(it) }
        if (scoped) url?.stopAccessingSecurityScopedResource()
        result.complete(data?.toByteArray())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        result.complete(null)
    }
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData() else usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return bytes
}
