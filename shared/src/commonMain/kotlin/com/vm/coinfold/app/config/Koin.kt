package com.vm.coinfold.app.config

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Starts Koin once; platform entry points pass extra setup (e.g. the Android context). */
fun initKoin(extra: KoinAppDeclaration? = null) {
    startKoin {
        extra?.invoke(this)
        modules(platformModule, dataModule)
    }
}
