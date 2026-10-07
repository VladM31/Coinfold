package com.vm.coinfold.app.feature.backup.domain.services

import com.vm.coinfold.app.feature.backup.domain.models.ReportDocument

actual object PdfRenderer {
    // PDF export is implemented on Android only for now; the UI hides the button where this is false.
    actual val isSupported: Boolean = false

    actual fun render(document: ReportDocument): ByteArray =
        throw UnsupportedOperationException("PDF export is not available on iOS yet")
}
