package com.vm.coinfold.app.feature.appconfig.net.clients.impls

import com.vm.coinfold.app.feature.appconfig.domain.models.AppConfig
import com.vm.coinfold.app.feature.appconfig.net.clients.AppConfigClient
import com.vm.coinfold.app.feature.appconfig.net.dtos.AppConfigDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

class GistAppConfigClient(
    private val httpClient: HttpClient,
    private val url: String = URL,
) : AppConfigClient {

    // A gist is served as text/plain, so it is parsed by hand instead of by content negotiation.
    override suspend fun load(): AppConfig =
        json.decodeFromString<AppConfigDto>(httpClient.get(url).body<String>()).toModel()

    companion object {
        const val URL = "https://gist.githubusercontent.com/VladM31/7f85e60ace628dd5f76393e9f482c34f/raw"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
