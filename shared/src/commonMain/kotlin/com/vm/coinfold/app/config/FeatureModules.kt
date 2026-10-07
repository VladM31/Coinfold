package com.vm.coinfold.app.config

import com.vm.coinfold.app.feature.accounts.domain.repositories.AccountRepository
import com.vm.coinfold.app.feature.accounts.domain.repositories.impls.AccountRepositoryImpl
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsViewModel
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import com.vm.coinfold.app.feature.expenses.domain.repositories.ExpenseStatsRepository
import com.vm.coinfold.app.feature.expenses.domain.repositories.impls.CategoryRepositoryImpl
import com.vm.coinfold.app.feature.expenses.domain.repositories.impls.ExpenseStatsRepositoryImpl
import com.vm.coinfold.app.feature.expenses.domain.usecases.SeedDefaultCategoriesUseCase
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesViewModel
import com.vm.coinfold.app.feature.overview.domain.repositories.OverviewRepository
import com.vm.coinfold.app.feature.overview.domain.repositories.impls.OverviewRepositoryImpl
import com.vm.coinfold.app.feature.overview.domain.viewmodels.OverviewViewModel
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
    viewModelOf(::ExpensesViewModel)
}

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
}

val overviewModule = module {
    single<OverviewRepository> { OverviewRepositoryImpl(get()) }
    viewModelOf(::OverviewViewModel)
}

