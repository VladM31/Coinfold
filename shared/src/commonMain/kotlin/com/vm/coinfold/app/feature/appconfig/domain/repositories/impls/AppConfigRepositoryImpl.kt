package com.vm.coinfold.app.feature.appconfig.domain.repositories.impls

import com.vm.coinfold.app.feature.appconfig.domain.models.AppConfig
import com.vm.coinfold.app.feature.appconfig.domain.repositories.AppConfigRepository
import com.vm.coinfold.app.feature.appconfig.net.clients.AppConfigClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppConfigRepositoryImpl(private val client: AppConfigClient) : AppConfigRepository {
    private val _config = MutableStateFlow<AppConfig?>(null)
    override val config: StateFlow<AppConfig?> = _config

    override suspend fun refresh(): AppConfig? =
        try {
            client.load().also { _config.value = it }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
}
