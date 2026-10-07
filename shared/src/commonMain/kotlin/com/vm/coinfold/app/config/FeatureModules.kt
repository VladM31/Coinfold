package com.vm.coinfold.app.config

import com.vm.coinfold.app.feature.accounts.main.AccountRepository
import com.vm.coinfold.app.feature.accounts.main.AccountRepositoryImpl
import com.vm.coinfold.app.feature.accounts.main.AccountsViewModel
import com.vm.coinfold.app.feature.transactions.main.AddManualTransactionUseCase
import com.vm.coinfold.app.feature.transactions.main.TransactionRepository
import com.vm.coinfold.app.feature.transactions.main.TransactionRepositoryImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val accountsModule = module {
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    viewModelOf(::AccountsViewModel)
}

val transactionsModule = module {
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    factory { AddManualTransactionUseCase(get()) }
}
