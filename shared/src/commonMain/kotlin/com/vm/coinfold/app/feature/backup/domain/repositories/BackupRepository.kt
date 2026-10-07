package com.vm.coinfold.app.feature.backup.domain.repositories

import com.vm.coinfold.app.feature.backup.domain.models.BackupFile

interface BackupRepository {
    /** Everything the user has entered, plus the settings, as a [BackupFile]. */
    suspend fun createBackup(): BackupFile

    /**
     * Replaces all current data with the content of [file] (which must have passed validation) in one database
     * transaction, then applies the settings stored in it.
     */
    suspend fun restore(file: BackupFile)
}
