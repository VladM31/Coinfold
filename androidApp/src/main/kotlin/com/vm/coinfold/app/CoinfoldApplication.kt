package com.vm.coinfold.app

import android.app.Application
import com.vm.coinfold.app.config.initKoinAndroid

class CoinfoldApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
