package com.vm.coinfold.app.feature.backup.domain.services

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.backup.domain.models.BackupFile
import com.vm.coinfold.app.feature.backup.domain.models.BackupProblem
import com.vm.coinfold.app.feature.backup.domain.models.ParseResult
import com.vm.coinfold.app.feature.recurring.domain.models.Frequency
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

private val json = Json {
    prettyPrint = true
    // A backup from a slightly newer build may carry extra fields; they are skipped, not an error.
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun encodeBackup(file: BackupFile): ByteArray = json.encodeToString(BackupFile.serializer(), file).encodeToByteArray()

/** Reads and validates a backup file; never throws. */
fun decodeBackup(bytes: ByteArray): ParseResult {
    val file = try {
        // Tolerate a UTF-8 byte order mark that some editors add.
        json.decodeFromString(BackupFile.serializer(), bytes.decodeToString().removePrefix("﻿"))
    } catch (e: Exception) {
        return ParseResult.Failed(BackupProblem.UNREADABLE)
    }
    return validateBackup(file)?.let { ParseResult.Failed(it) } ?: ParseResult.Ok(file)
}

/** Checks that the whole file can be loaded without breaking foreign keys; null means it is fine. */
fun validateBackup(file: BackupFile): BackupProblem? {
    if (file.app != BackupFile.APP_ID) return BackupProblem.NOT_COINFOLD
    if (file.version > BackupFile.CURRENT_VERSION || file.version < 1) return BackupProblem.NEWER_VERSION

    val currencies = Currency.entries.map { it.code }.toSet()
    val types = TransactionType.entries.map { it.name }.toSet()
    val frequencies = Frequency.entries.map { it.name }.toSet()

    if (file.accounts.any { it.currency !in currencies }) return BackupProblem.INVALID_VALUES
    if (file.transactions.any { it.currency !in currencies || it.type !in types || !it.rate.isDecimal() }) {
        return BackupProblem.INVALID_VALUES
    }
    if (file.recurring.any { it.type !in types || it.frequency !in frequencies || !it.nextDate.isDate() }) {
        return BackupProblem.INVALID_VALUES
    }
    file.settings?.let {
        if (it.mainCurrency !in currencies || it.lastUsedCurrency !in currencies) return BackupProblem.INVALID_VALUES
    }

    if (file.accounts.hasDuplicates { it.id } || file.categories.hasDuplicates { it.id } ||
        file.transactions.hasDuplicates { it.id } || file.recurring.hasDuplicates { it.id }
    ) {
        return BackupProblem.DUPLICATE_IDS
    }

    val accountIds = file.accounts.mapTo(HashSet()) { it.id }
    val categoryIds = file.categories.mapTo(HashSet()) { it.id }
    val brokenTransaction = file.transactions.any {
        it.accountId !in accountIds || (it.categoryId != null && it.categoryId !in categoryIds)
    }
    val brokenSchedule = file.recurring.any {
        it.accountId !in accountIds || (it.categoryId != null && it.categoryId !in categoryIds)
    }
    if (brokenTransaction || brokenSchedule) return BackupProblem.BROKEN_REFERENCES
    return null
}

private fun <T> List<T>.hasDuplicates(id: (T) -> Long): Boolean = map(id).toSet().size != size

private fun String.isDecimal(): Boolean = runCatching { BigDecimal.parseString(this) }.isSuccess

private fun String.isDate(): Boolean = runCatching { LocalDate.parse(this) }.isSuccess
