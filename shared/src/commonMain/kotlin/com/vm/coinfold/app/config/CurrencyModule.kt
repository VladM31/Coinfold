package com.vm.coinfold.app.config

import com.vm.coinfold.app.feature.currency.main.ConvertMoneyUseCase
import com.vm.coinfold.app.feature.currency.main.CurrencyRepository
import com.vm.coinfold.app.feature.currency.main.CurrencyRepositoryImpl
import com.vm.coinfold.app.feature.currency.main.GetRateUseCase
import com.vm.coinfold.app.feature.currency.net.CurrencyClient
import com.vm.coinfold.app.feature.currency.net.MonobankCurrencyClient
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
