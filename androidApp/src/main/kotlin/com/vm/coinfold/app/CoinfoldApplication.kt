package com.vm.coinfold.app

import android.app.Application
import com.vm.coinfold.app.config.initKoinAndroid
import com.vm.coinfold.app.shared.analytics.Analytics

class CoinfoldApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Analytics.install(FirebaseTracker(this))
        initKoinAndroid(this)
    }
}
