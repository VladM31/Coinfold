package com.vm.coinfold.app.feature.appconfig

import com.vm.coinfold.app.feature.appconfig.domain.repositories.impls.AppConfigRepositoryImpl
import com.vm.coinfold.app.feature.appconfig.net.clients.impls.GistAppConfigClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class AppConfigTest {
    private val body = """
        {"currentVersion":"1.1.0","supportedVersions":["1.0.0","1.1.0"],"updateUrl":null,"extra":1,
         "policies":[{"url":"https://x/en","language":"en","version":"1.0"},
                     {"url":"https://x/uk","language":"uk","version":"1.0"}]}
    """.trimIndent()

    private fun repo(status: HttpStatusCode = HttpStatusCode.OK, text: String = body): AppConfigRepositoryImpl {
        // text/plain, exactly like a gist
        val engine = MockEngine { respond(text, status, headersOf(HttpHeaders.ContentType, "text/plain")) }
        return AppConfigRepositoryImpl(GistAppConfigClient(HttpClient(engine) { expectSuccess = true }))
    }

    @Test
    fun loadsConfigServedAsPlainText() = runTest {
        val repository = repo()
        assertNull(repository.config.value)

        val config = repository.refresh()!!

        assertEquals(config, repository.config.value)
        assertEquals("1.1.0", config.currentVersion)
        assertNull(config.updateUrl)
        assertTrue(config.isSupported("1.0.0"))
        assertFalse(config.isSupported("0.9.0"))
        assertTrue(config.hasUpdate("1.0.0"))
        assertEquals("https://x/uk", config.policyFor("uk")?.url)
        assertEquals("https://x/en", config.policyFor("de")?.url)
    }

    @Test
    fun failureReturnsNull() = runTest {
        assertNull(repo(HttpStatusCode.NotFound).refresh())
        assertNull(repo(text = "not json").refresh())
    }
}
