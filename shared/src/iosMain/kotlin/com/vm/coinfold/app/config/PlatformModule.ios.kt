package com.vm.coinfold.app.config

import androidx.room.Room
import androidx.room.RoomDatabase
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import com.vm.coinfold.app.shared.db.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private fun documentsPath(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(url?.path) { "Documents directory not available" }
}

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<RoomDatabase.Builder<AppDatabase>> {
        Room.databaseBuilder<AppDatabase>(name = documentsPath() + "/" + DATABASE_FILE_NAME)
    }
    single<String>(DataStorePathQualifier) { documentsPath() + "/" + DATASTORE_FILE_NAME }
}
