package com.vm.coinfold.app.feature.backup.domain.repositories.impls

import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.backup.db.daos.BackupDao
import com.vm.coinfold.app.feature.backup.db.entities.BackupSnapshot
import com.vm.coinfold.app.feature.backup.domain.models.BackupAccount
import com.vm.coinfold.app.feature.backup.domain.models.BackupCategory
import com.vm.coinfold.app.feature.backup.domain.models.BackupFile
import com.vm.coinfold.app.feature.backup.domain.models.BackupRecurring
import com.vm.coinfold.app.feature.backup.domain.models.BackupSettings
import com.vm.coinfold.app.feature.backup.domain.models.BackupTransaction
import com.vm.coinfold.app.feature.backup.domain.repositories.BackupRepository
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringEntity
import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.models.ThemeMode
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionEntity
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.utils.nowMillis
import kotlinx.coroutines.flow.first

class BackupRepositoryImpl(
    private val dao: BackupDao,
    private val settings: SettingsRepository,
) : BackupRepository {

    override suspend fun createBackup(): BackupFile {
        val data = dao.snapshot()
        val current = settings.settings.first()
        return BackupFile(
            createdAt = nowMillis(),
            settings = BackupSettings(
                theme = current.theme.name,
                language = current.language.name,
                mainCurrency = current.mainCurrency.code,
                periodStartDay = current.periodStartDay,
                lastUsedCurrency = current.lastUsedCurrency.code,
            ),
            accounts = data.accounts.map {
                BackupAccount(it.id, it.name, it.currency.code, it.initialBalanceMinor, it.icon, it.color, it.isArchived)
            },
            categories = data.categories.map {
                BackupCategory(it.id, it.name, it.color, it.icon, it.sortOrder, it.isArchived)
            },
            transactions = data.transactions.map {
                BackupTransaction(
                    it.id, it.type.name, it.accountId, it.categoryId, it.incomeSource, it.amountMinor, it.currency.code,
                    it.accountAmountMinor, it.rate, it.note, it.dateTime,
                )
            },
            recurring = data.recurring.map {
                BackupRecurring(
                    it.id, it.type.name, it.accountId, it.categoryId, it.incomeSource, it.amountMinor, it.note,
                    it.frequency, it.anchorDay, it.nextDate, it.isActive,
                )
            },
        )
    }

    override suspend fun restore(file: BackupFile) {
        dao.replaceAll(
            BackupSnapshot(
                accounts = file.accounts.map {
                    AccountEntity(
                        it.id, it.name, Currency.fromCode(it.currency), it.initialBalanceMinor, it.icon, it.color, it.isArchived,
                    )
                },
                categories = file.categories.map {
                    CategoryEntity(it.id, it.name, it.color, it.icon, it.sortOrder, it.isArchived)
                },
                transactions = file.transactions.map {
                    TransactionEntity(
                        id = it.id,
                        type = TransactionType.valueOf(it.type),
                        accountId = it.accountId,
                        categoryId = it.categoryId,
                        incomeSource = it.incomeSource,
                        amountMinor = it.amountMinor,
                        currency = Currency.fromCode(it.currency),
                        accountAmountMinor = it.accountAmountMinor,
                        rate = it.rate,
                        note = it.note,
                        dateTime = it.dateTime,
                    )
                },
                recurring = file.recurring.map {
                    RecurringEntity(
                        id = it.id,
                        type = TransactionType.valueOf(it.type),
                        accountId = it.accountId,
                        categoryId = it.categoryId,
                        incomeSource = it.incomeSource,
                        amountMinor = it.amountMinor,
                        note = it.note,
                        frequency = it.frequency,
                        anchorDay = it.anchorDay,
                        nextDate = it.nextDate,
                        isActive = it.isActive,
                    )
                },
            ),
        )
        file.settings?.let { applySettings(it) }
    }

    private suspend fun applySettings(saved: BackupSettings) {
        ThemeMode.entries.firstOrNull { it.name == saved.theme }?.let { settings.setTheme(it) }
        AppLanguage.entries.firstOrNull { it.name == saved.language }?.let { settings.setLanguage(it) }
        Currency.entries.firstOrNull { it.code == saved.mainCurrency }?.let { settings.setMainCurrency(it) }
        settings.setPeriodStartDay(saved.periodStartDay)
        Currency.entries.firstOrNull { it.code == saved.lastUsedCurrency }?.let { settings.setLastUsedCurrency(it) }
    }
}
