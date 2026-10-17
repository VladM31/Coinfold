package com.vm.coinfold.app.feature.appconfig.net.clients

import com.vm.coinfold.app.feature.appconfig.domain.models.AppConfig

/** Downloads the remote settings; swap the implementation (and the Koin binding) to change the source. */
interface AppConfigClient {
    suspend fun load(): AppConfig
}
