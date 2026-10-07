package com.vm.coinfold.app.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vm.coinfold.app.feature.settings.db.storages.SettingsStorage
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.feature.settings.domain.repositories.impls.SettingsRepositoryImpl
import com.vm.coinfold.app.shared.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Platform bindings: `RoomDatabase.Builder<AppDatabase>` and the DataStore file path. */
expect val platformModule: Module

const val DATASTORE_FILE_NAME = "coinfold.preferences_pb"
const val DATABASE_FILE_NAME = "coinfold.db"
val DataStorePathQualifier = named("datastore_path")

val dataModule = module {
    single<AppDatabase> {
        get<RoomDatabase.Builder<AppDatabase>>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }
    single { get<AppDatabase>().accountDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().expenseStatsDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().rateDao() }

    single<DataStore<Preferences>> {
        val path = get<String>(DataStorePathQualifier)
        PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })
    }
    single { SettingsStorage(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}
