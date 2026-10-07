package com.vm.coinfold.app.feature.backup.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.db.entities.AccountEntity
import com.vm.coinfold.app.feature.backup.db.daos.BackupDao
import com.vm.coinfold.app.feature.backup.domain.models.BackupAccount
import com.vm.coinfold.app.feature.backup.domain.models.BackupCategory
import com.vm.coinfold.app.feature.backup.domain.models.BackupFile
import com.vm.coinfold.app.feature.backup.domain.models.BackupProblem
import com.vm.coinfold.app.feature.backup.domain.models.BackupTransaction
import com.vm.coinfold.app.feature.backup.domain.models.ExportRange
import com.vm.coinfold.app.feature.backup.domain.models.ParseResult
import com.vm.coinfold.app.feature.backup.domain.repositories.impls.BackupRepositoryImpl
import com.vm.coinfold.app.feature.backup.domain.services.csvField
import com.vm.coinfold.app.feature.backup.domain.services.decodeBackup
import com.vm.coinfold.app.feature.backup.domain.services.encodeBackup
import com.vm.coinfold.app.feature.backup.domain.services.exportCsv
import com.vm.coinfold.app.feature.backup.domain.services.exportQuery
import com.vm.coinfold.app.feature.backup.domain.services.plainAmount
import com.vm.coinfold.app.feature.expenses.db.entities.CategoryEntity
import com.vm.coinfold.app.feature.recurring.db.entities.RecurringEntity
import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.models.Settings
import com.vm.coinfold.app.feature.settings.domain.models.ThemeMode
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.feature.transactions.db.entities.TransactionEntity
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionCategory
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalTime::class)
class BackupLogicTest {

    // ---------- file format and validation ----------

    private val account = BackupAccount(1, "Card", "UAH", 10_000, "icon:card", 0xFF7C4DFF, false)
    private val category = BackupCategory(5, "Food", 0xFF00FF00, "icon:cart", 0, false)
    private fun tx(id: Long, accountId: Long = 1, categoryId: Long? = 5) = BackupTransaction(
        id, "EXPENSE", accountId, categoryId, null, 2_550, "UAH", 2_550, "1", "Silpo, \"big\"", 1_000L,
    )

    private fun file(
        accounts: List<BackupAccount> = listOf(account),
        categories: List<BackupCategory> = listOf(category),
        transactions: List<BackupTransaction> = listOf(tx(1)),
    ) = BackupFile(createdAt = 5L, accounts = accounts, categories = categories, transactions = transactions)

    private fun problem(f: BackupFile) = (decodeBackup(encodeBackup(f)) as? ParseResult.Failed)?.problem

    @Test
    fun aBackupSurvivesEncodingAndDecoding() {
        val original = file()
        val decoded = assertIs<ParseResult.Ok>(decodeBackup(encodeBackup(original)))
        assertEquals(original, decoded.file)
    }

    @Test
    fun garbageAndForeignFilesAreRejected() {
        assertEquals(BackupProblem.UNREADABLE, (decodeBackup("not json".encodeToByteArray()) as ParseResult.Failed).problem)
        assertEquals(BackupProblem.UNREADABLE, (decodeBackup(ByteArray(0)) as ParseResult.Failed).problem)
        assertEquals(BackupProblem.NOT_COINFOLD, problem(file().copy(app = "other")))
        assertEquals(BackupProblem.NEWER_VERSION, problem(file().copy(version = BackupFile.CURRENT_VERSION + 1)))
    }

    @Test
    fun brokenReferencesAreCaughtBeforeAnythingIsChanged() {
        assertEquals(BackupProblem.BROKEN_REFERENCES, problem(file(transactions = listOf(tx(1, accountId = 99)))))
        assertEquals(BackupProblem.BROKEN_REFERENCES, problem(file(transactions = listOf(tx(1, categoryId = 42)))))
        // a transaction without a category is fine
        assertNull(problem(file(transactions = listOf(tx(1, categoryId = null)))))
    }

    @Test
    fun badValuesAndDuplicatesAreCaught() {
        assertEquals(BackupProblem.INVALID_VALUES, problem(file(accounts = listOf(account.copy(currency = "XXX")))))
        assertEquals(BackupProblem.INVALID_VALUES, problem(file(transactions = listOf(tx(1).copy(rate = "abc")))))
        assertEquals(BackupProblem.DUPLICATE_IDS, problem(file(transactions = listOf(tx(1), tx(1)))))
    }

    @Test
    fun aByteOrderMarkInFrontIsTolerated() {
        val bytes = "﻿".encodeToByteArray() + encodeBackup(file())
        assertIs<ParseResult.Ok>(decodeBackup(bytes))
    }

    // ---------- CSV ----------

    private fun item(note: String, type: TransactionType = TransactionType.EXPENSE) = TransactionItem(
        id = 1, type = type, accountId = 1, accountName = "Card, main", accountCurrency = Currency.UAH,
        category = TransactionCategory(5, "Food", 0xFF00FF00, "icon:cart"),
        source = if (type == TransactionType.INCOME) IncomeSource.Preset.DEBT_RETURN else null,
        note = note, amount = Money(1_205, Currency.USD), accountAmount = Money(50_000, Currency.UAH),
        rate = BigDecimal.parseString("41.5"), dateTime = Instant.parse("2026-03-05T14:07:00Z").toEpochMilliseconds(),
    )

    @Test
    fun csvQuotesFieldsAndSignsAmounts() {
        val csv = exportCsv(listOf(item("Silpo, \"big\" shop"), item("Debt", TransactionType.INCOME)), TimeZone.UTC)
        val lines = csv.removePrefix("﻿").trim().lines()

        assertTrue(csv.startsWith("﻿"))
        assertEquals("Date,Time,Type,Account,Category,Source,Note,Amount,Currency,Account amount,Account currency,Rate", lines[0])
        assertEquals(
            "2026-03-05,14:07,expense,\"Card, main\",Food,,\"Silpo, \"\"big\"\" shop\",-12.05,USD,-500.00,UAH,41.5",
            lines[1],
        )
        assertEquals("2026-03-05,14:07,income,\"Card, main\",Food,Debt return,Debt,12.05,USD,500.00,UAH,41.5", lines[2])
    }

    @Test
    fun csvHelpers() {
        assertEquals("plain", csvField("plain"))
        assertEquals("\"a\nb\"", csvField("a\nb"))
        assertEquals("-0.05", plainAmount(Money(5, Currency.UAH), -1))
        assertEquals("1234.50", plainAmount(Money(123_450, Currency.EUR)))
    }

    // ---------- export range ----------

    @Test
    fun exportRangesFollowThePeriodStartDay() {
        val today = LocalDate(2026, 3, 10)
        val all = exportQuery(ExportRange.ALL, 7, today, TimeZone.UTC)
        assertEquals(0L, all.fromMillis)
        assertEquals(Long.MAX_VALUE, all.toMillis)

        val current = exportQuery(ExportRange.CURRENT_PERIOD, 7, today, TimeZone.UTC)
        assertEquals(Instant.parse("2026-03-07T00:00:00Z").toEpochMilliseconds(), current.fromMillis)
        assertEquals(Instant.parse("2026-04-07T00:00:00Z").toEpochMilliseconds(), current.toMillis)

        val previous = exportQuery(ExportRange.PREVIOUS_PERIOD, 7, today, TimeZone.UTC)
        assertEquals(Instant.parse("2026-02-07T00:00:00Z").toEpochMilliseconds(), previous.fromMillis)
        assertEquals(current.fromMillis, previous.toMillis)
    }

    // ---------- backup and restore through the repository ----------

    private class FakeBackupDao : BackupDao() {
        var accountRows = listOf<AccountEntity>()
        var categoryRows = listOf<CategoryEntity>()
        var transactionRows = listOf<TransactionEntity>()
        var recurringRows = listOf<RecurringEntity>()
        val log = mutableListOf<String>()

        override suspend fun accounts() = accountRows
        override suspend fun categories() = categoryRows
        override suspend fun transactions() = transactionRows
        override suspend fun recurring() = recurringRows
        override suspend fun clearRecurring() { log += "clearRecurring"; recurringRows = emptyList() }
        override suspend fun clearTransactions() { log += "clearTransactions"; transactionRows = emptyList() }
        override suspend fun clearCategories() { log += "clearCategories"; categoryRows = emptyList() }
        override suspend fun clearAccounts() { log += "clearAccounts"; accountRows = emptyList() }
        override suspend fun insertAccounts(items: List<AccountEntity>) { log += "insertAccounts"; accountRows = items }
        override suspend fun insertCategories(items: List<CategoryEntity>) { log += "insertCategories"; categoryRows = items }
        override suspend fun insertTransactions(items: List<TransactionEntity>) { log += "insertTransactions"; transactionRows = items }
        override suspend fun insertRecurring(items: List<RecurringEntity>) { log += "insertRecurring"; recurringRows = items }
    }

    private class FakeSettings : SettingsRepository {
        val state = MutableStateFlow(Settings(theme = ThemeMode.DARK, periodStartDay = 7, mainCurrency = Currency.EUR))
        override val settings: Flow<Settings> = state
        override suspend fun setTheme(theme: ThemeMode) { state.value = state.value.copy(theme = theme) }
        override suspend fun setLanguage(language: AppLanguage) { state.value = state.value.copy(language = language) }
        override suspend fun setMainCurrency(currency: Currency) { state.value = state.value.copy(mainCurrency = currency) }
        override suspend fun setPeriodStartDay(day: Int) { state.value = state.value.copy(periodStartDay = day) }
        override suspend fun setLastUsedCurrency(currency: Currency) { state.value = state.value.copy(lastUsedCurrency = currency) }
        override suspend fun claimDefaultCategories(version: Int) = false
    }

    @Test
    fun everythingSurvivesABackupAndRestoreIntoAnEmptyDevice() = runTest {
        val source = FakeBackupDao().apply {
            accountRows = listOf(AccountEntity(1, "Card", Currency.USD, 12_345, "icon:card", 0xFF111111, true))
            categoryRows = listOf(CategoryEntity(5, "Food", 0xFF00FF00, "icon:cart", 3, false))
            transactionRows = listOf(
                TransactionEntity(
                    9, TransactionType.EXPENSE, 1, 5, null, 1_000, Currency.EUR, 1_100, "1.1", "lunch", 777L,
                ),
            )
            recurringRows = listOf(
                RecurringEntity(2, TransactionType.INCOME, 1, null, "preset:salary", 5_000, "pay", "MONTHLY", 31, "2026-03-31", false),
            )
        }
        val backup = BackupRepositoryImpl(source, FakeSettings()).createBackup()
        // through the real file format, as it would travel between devices
        val decoded = (decodeBackup(encodeBackup(backup)) as ParseResult.Ok).file

        val targetSettings = FakeSettings().apply { state.value = Settings() }
        val target = FakeBackupDao()
        BackupRepositoryImpl(target, targetSettings).restore(decoded)

        assertEquals(source.accountRows, target.accountRows)
        assertEquals(source.categoryRows, target.categoryRows)
        assertEquals(source.transactionRows, target.transactionRows)
        assertEquals(source.recurringRows, target.recurringRows)
        // settings travel with the backup
        assertEquals(ThemeMode.DARK, targetSettings.state.value.theme)
        assertEquals(7, targetSettings.state.value.periodStartDay)
        assertEquals(Currency.EUR, targetSettings.state.value.mainCurrency)
    }

    @Test
    fun restoreClearsChildrenBeforeParentsAndInsertsParentsFirst() = runTest {
        val dao = FakeBackupDao()
        BackupRepositoryImpl(dao, FakeSettings()).restore(file())

        assertEquals(
            listOf(
                "clearRecurring", "clearTransactions", "clearCategories", "clearAccounts",
                "insertAccounts", "insertCategories", "insertTransactions", "insertRecurring",
            ),
            dao.log,
        )
    }
}
