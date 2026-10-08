package com.vm.coinfold.app.config

import com.vm.coinfold.app.feature.accounts.domain.repositories.AccountRepository
import com.vm.coinfold.app.feature.accounts.domain.repositories.impls.AccountRepositoryImpl
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsViewModel
import com.vm.coinfold.app.feature.backup.domain.repositories.BackupRepository
import com.vm.coinfold.app.feature.backup.domain.repositories.impls.BackupRepositoryImpl
import com.vm.coinfold.app.feature.backup.domain.usecases.CreateBackupUseCase
import com.vm.coinfold.app.feature.backup.domain.usecases.ExportTransactionsUseCase
import com.vm.coinfold.app.feature.backup.domain.usecases.RestoreBackupUseCase
import com.vm.coinfold.app.feature.backup.domain.viewmodels.BackupViewModel
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import com.vm.coinfold.app.feature.expenses.domain.repositories.ExpenseStatsRepository
import com.vm.coinfold.app.feature.expenses.domain.repositories.impls.CategoryRepositoryImpl
import com.vm.coinfold.app.feature.expenses.domain.repositories.impls.ExpenseStatsRepositoryImpl
import com.vm.coinfold.app.feature.expenses.domain.usecases.GetPeriodBalanceUseCase
import com.vm.coinfold.app.feature.expenses.domain.usecases.SeedDefaultCategoriesUseCase
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesViewModel
import com.vm.coinfold.app.feature.overview.domain.repositories.OverviewRepository
import com.vm.coinfold.app.feature.overview.domain.repositories.impls.OverviewRepositoryImpl
import com.vm.coinfold.app.feature.overview.domain.viewmodels.OverviewViewModel
import com.vm.coinfold.app.feature.recurring.domain.repositories.RecurringRepository
import com.vm.coinfold.app.feature.recurring.domain.repositories.impls.RecurringRepositoryImpl
import com.vm.coinfold.app.feature.recurring.domain.usecases.ProcessRecurringPaymentsUseCase
import com.vm.coinfold.app.feature.recurring.domain.viewmodels.RecurringViewModel
import com.vm.coinfold.app.feature.security.db.storages.SecurityStorage
import com.vm.coinfold.app.feature.security.domain.repositories.SecurityRepository
import com.vm.coinfold.app.feature.security.domain.repositories.impls.SecurityRepositoryImpl
import com.vm.coinfold.app.feature.security.domain.services.LockController
import com.vm.coinfold.app.feature.security.domain.viewmodels.LockViewModel
import com.vm.coinfold.app.feature.security.domain.viewmodels.SecurityViewModel
import com.vm.coinfold.app.feature.settings.domain.viewmodels.SettingsViewModel
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.feature.transactions.domain.repositories.impls.TransactionRepositoryImpl
import com.vm.coinfold.app.feature.transactions.domain.usecases.AddExpenseUseCase
import com.vm.coinfold.app.feature.transactions.domain.usecases.AddManualTransactionUseCase
import com.vm.coinfold.app.feature.transactions.domain.usecases.UpdateTransactionUseCase
import com.vm.coinfold.app.feature.transactions.domain.viewmodels.TransactionsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val accountsModule = module {
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    viewModelOf(::AccountsViewModel)
}

val transactionsModule = module {
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    factory { AddManualTransactionUseCase(get()) }
    factory { AddExpenseUseCase(get(), get()) }
    factory { UpdateTransactionUseCase(get(), get()) }
    viewModelOf(::TransactionsViewModel)
}

val expensesModule = module {
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<ExpenseStatsRepository> { ExpenseStatsRepositoryImpl(get()) }
    factory { SeedDefaultCategoriesUseCase(get(), get()) }
    factory { GetPeriodBalanceUseCase(get(), get(), get(), get()) }
    viewModelOf(::ExpensesViewModel)
}

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
}

val overviewModule = module {
    single<OverviewRepository> { OverviewRepositoryImpl(get()) }
    viewModelOf(::OverviewViewModel)
}

val recurringModule = module {
    single<RecurringRepository> { RecurringRepositoryImpl(get()) }
    factory { ProcessRecurringPaymentsUseCase(get(), get(), get()) }
    viewModelOf(::RecurringViewModel)
}

val backupModule = module {
    single<BackupRepository> { BackupRepositoryImpl(get(), get()) }
    factory { CreateBackupUseCase(get()) }
    factory { RestoreBackupUseCase(get()) }
    factory { ExportTransactionsUseCase(get(), get(), get()) }
    viewModelOf(::BackupViewModel)
}

val securityModule = module {
    single { SecurityStorage(get()) }
    single<SecurityRepository> { SecurityRepositoryImpl(get()) }
    // One controller for the whole app: both the lock screen and App.kt read the same lock state.
    single { LockController(get()) }
    viewModelOf(::LockViewModel)
    viewModelOf(::SecurityViewModel)
}

