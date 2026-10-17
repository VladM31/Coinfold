package com.vm.coinfold.app.config

import com.vm.coinfold.app.feature.appconfig.domain.repositories.AppConfigRepository
import com.vm.coinfold.app.feature.appconfig.domain.repositories.impls.AppConfigRepositoryImpl
import com.vm.coinfold.app.feature.appconfig.net.clients.AppConfigClient
import com.vm.coinfold.app.feature.appconfig.net.clients.impls.GistAppConfigClient
import com.vm.coinfold.app.feature.policy.db.storages.PolicyStorage
import com.vm.coinfold.app.feature.policy.domain.viewmodels.PolicyViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appConfigModule = module {
    single<AppConfigClient> { GistAppConfigClient(get()) }
    single<AppConfigRepository> { AppConfigRepositoryImpl(get()) }
    single { PolicyStorage(get()) }
    viewModelOf(::PolicyViewModel)
}
