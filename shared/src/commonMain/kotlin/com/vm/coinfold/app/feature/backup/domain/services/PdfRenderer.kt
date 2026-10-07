package com.vm.coinfold.app.feature.backup.domain.services

import com.vm.coinfold.app.feature.backup.domain.models.ReportDocument

/**
 * Draws a [ReportDocument] as a multi-page A4 PDF using the platform's text rendering (so Cyrillic works).
 * Not available on every platform yet; check [isSupported] before offering the export.
 */
expect object PdfRenderer {
    val isSupported: Boolean
    fun render(document: ReportDocument): ByteArray
}
