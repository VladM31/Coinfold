package com.vm.coinfold.app.config

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import com.vm.coinfold.app.shared.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = get<Context>()
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = context.getDatabasePath(DATABASE_FILE_NAME).absolutePath,
        )
    }
    single<String>(DataStorePathQualifier) {
        get<Context>().filesDir.resolve(DATASTORE_FILE_NAME).absolutePath
    }
}

/** Called from the Application class of the Android app. */
fun initKoinAndroid(context: Context) = initKoin { androidContext(context) }
