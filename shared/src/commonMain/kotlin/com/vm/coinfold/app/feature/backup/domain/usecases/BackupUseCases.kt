package com.vm.coinfold.app.feature.backup.domain.usecases

import com.vm.coinfold.app.feature.backup.domain.models.BackupFile
import com.vm.coinfold.app.feature.backup.domain.models.ExportFile
import com.vm.coinfold.app.feature.backup.domain.repositories.BackupRepository
import com.vm.coinfold.app.feature.backup.domain.services.encodeBackup
import com.vm.coinfold.app.shared.analytics.Analytics
import com.vm.coinfold.app.shared.analytics.AnalyticsEvent
import com.vm.coinfold.app.utils.today

/** Builds the backup file with a date in its name, ready to share. */
class CreateBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(): ExportFile {
        val bytes = encodeBackup(repository.createBackup())
        Analytics.event(AnalyticsEvent.BACKUP_CREATED)
        return ExportFile("coinfold-backup-${today()}.json", "application/json", bytes)
    }
}

/** Replaces all data with the content of a validated backup. */
class RestoreBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(file: BackupFile) {
        repository.restore(file)
        Analytics.event(AnalyticsEvent.BACKUP_RESTORED)
    }
}
