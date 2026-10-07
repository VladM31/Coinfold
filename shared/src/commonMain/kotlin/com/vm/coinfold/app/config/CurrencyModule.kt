package com.vm.coinfold.app.config

import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.currency.domain.repositories.impls.CurrencyRepositoryImpl
import com.vm.coinfold.app.feature.currency.domain.usecases.ConvertMoneyUseCase
import com.vm.coinfold.app.feature.currency.domain.usecases.GetRateUseCase
import com.vm.coinfold.app.feature.currency.net.clients.CurrencyClient
import com.vm.coinfold.app.feature.currency.net.clients.impls.MonobankCurrencyClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val currencyModule = module {
    single {
        HttpClient(get<HttpClientEngine>()) {
            expectSuccess = true
            install(HttpTimeout) { requestTimeoutMillis = 15_000 }
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }
    // To change the rate source, bind another CurrencyClient here.
    single<CurrencyClient> { MonobankCurrencyClient(get()) }
    single<CurrencyRepository> { CurrencyRepositoryImpl(get(), get()) }
    factory { GetRateUseCase(get()) }
    factory { ConvertMoneyUseCase(get()) }
}
