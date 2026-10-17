package com.vm.coinfold.app.feature.appconfig.net.dtos

import com.vm.coinfold.app.feature.appconfig.domain.models.AppConfig
import com.vm.coinfold.app.feature.appconfig.domain.models.PolicyLink
import kotlinx.serialization.Serializable

/** The JSON file as published; every field has a default so a partial file still loads. */
@Serializable
data class AppConfigDto(
    val currentVersion: String = "",
    val supportedVersions: List<String> = emptyList(),
    val updateUrl: String? = null,
    val policies: List<PolicyDto> = emptyList(),
) {
    @Serializable
    data class PolicyDto(val url: String, val language: String, val version: String = "")

    fun toModel() = AppConfig(
        currentVersion = currentVersion,
        supportedVersions = supportedVersions,
        updateUrl = updateUrl?.takeIf { it.isNotBlank() },
        policies = policies.map { PolicyLink(it.url, it.language, it.version) },
    )
}
