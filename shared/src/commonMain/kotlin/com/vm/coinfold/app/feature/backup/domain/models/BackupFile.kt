package com.vm.coinfold.app.feature.backup.domain.models

import kotlinx.serialization.Serializable

/**
 * The backup file: plain JSON that does not depend on the database engine, so a backup made on Android
 * can be restored on iOS and the other way round. Enums are stored as their names and money as minor units.
 * Bump [CURRENT_VERSION] when the format changes in an incompatible way.
 */
@Serializable
data class BackupFile(
    val app: String = APP_ID,
    val version: Int = CURRENT_VERSION,
    /** UTC epoch millis when the backup was made. */
    val createdAt: Long,
    val settings: BackupSettings? = null,
    val accounts: List<BackupAccount> = emptyList(),
    val categories: List<BackupCategory> = emptyList(),
    val transactions: List<BackupTransaction> = emptyList(),
    val recurring: List<BackupRecurring> = emptyList(),
) {
    companion object {
        const val APP_ID = "coinfold"
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class BackupSettings(
    val theme: String,
    val language: String,
    val mainCurrency: String,
    val periodStartDay: Int,
    val lastUsedCurrency: String,
)

@Serializable
data class BackupAccount(
    val id: Long,
    val name: String,
    val currency: String,
    val initialBalanceMinor: Long,
    val icon: String? = null,
    val color: Long? = null,
    val isArchived: Boolean = false,
)

@Serializable
data class BackupCategory(
    val id: Long,
    val name: String,
    val color: Long,
    val icon: String,
    val sortOrder: Int,
    val isArchived: Boolean = false,
)

@Serializable
data class BackupTransaction(
    val id: Long,
    val type: String,
    val accountId: Long,
    val categoryId: Long? = null,
    val incomeSource: String? = null,
    val amountMinor: Long,
    val currency: String,
    val accountAmountMinor: Long,
    val rate: String,
    val note: String = "",
    val dateTime: Long,
)

@Serializable
data class BackupRecurring(
    val id: Long,
    val type: String,
    val accountId: Long,
    val categoryId: Long? = null,
    val incomeSource: String? = null,
    val amountMinor: Long,
    val note: String = "",
    val frequency: String,
    val anchorDay: Int,
    val nextDate: String,
    val isActive: Boolean = true,
)

/** What a backup contains, for the confirmation before restoring it. */
data class BackupSummary(
    val createdAt: Long,
    val accounts: Int,
    val categories: Int,
    val transactions: Int,
    val recurring: Int,
)

fun BackupFile.summary() = BackupSummary(createdAt, accounts.size, categories.size, transactions.size, recurring.size)
