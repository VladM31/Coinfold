package com.vm.coinfold.app.feature.appconfig.domain.repositories

import com.vm.coinfold.app.feature.appconfig.domain.models.AppConfig
import kotlinx.coroutines.flow.StateFlow

/** Remote settings of the app. Read [config] anywhere; call [refresh] to download a fresh copy. */
interface AppConfigRepository {
    /** The last downloaded settings, or null until the first successful [refresh]. */
    val config: StateFlow<AppConfig?>

    /** Downloads the settings; offline or a broken file keeps the previous value and returns null. */
    suspend fun refresh(): AppConfig?
}
