package com.vm.coinfold.app.feature.appconfig.domain.models

/**
 * Remote settings of the app, published as a JSON file (a GitHub gist) so they can change without a release.
 *
 * @property currentVersion the latest released version of the app.
 * @property supportedVersions versions that still work; anything else should ask the user to update.
 * @property updateUrl where to get the update (the store page); null until the app is published.
 * @property policies privacy policy documents, one per language.
 */
data class AppConfig(
    val currentVersion: String,
    val supportedVersions: List<String>,
    val updateUrl: String?,
    val policies: List<PolicyLink>,
) {
    /** True when [appVersion] is listed as supported. */
    fun isSupported(appVersion: String): Boolean = appVersion in supportedVersions

    /** True when [appVersion] is not the latest one. */
    fun hasUpdate(appVersion: String): Boolean = appVersion != currentVersion

    /** The policy for [languageCode] (for example "uk"), or the English one when there is none. */
    fun policyFor(languageCode: String): PolicyLink? =
        policies.firstOrNull { it.language == languageCode } ?: policies.firstOrNull { it.language == "en" }
}

data class PolicyLink(val url: String, val language: String, val version: String)
