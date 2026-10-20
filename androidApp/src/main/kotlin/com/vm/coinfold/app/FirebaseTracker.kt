package com.vm.coinfold.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vm.coinfold.app.shared.analytics.AnalyticsTracker

/**
 * Sends events and errors to Firebase. Collection is off in the manifest and is turned on only after the user
 * accepted the privacy policy; debug builds never report, so testing does not pollute the numbers.
 */
class FirebaseTracker(context: Context) : AnalyticsTracker {
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val isDebuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    override fun setCollectionEnabled(enabled: Boolean) {
        val allowed = enabled && !isDebuggable
        analytics.setAnalyticsCollectionEnabled(allowed)
        crashlytics.setCrashlyticsCollectionEnabled(allowed)
    }

    override fun logEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle().apply { params.forEach { (key, value) -> putString(key, value) } }
        analytics.logEvent(name, bundle)
    }

    override fun logScreen(name: String) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply { putString(FirebaseAnalytics.Param.SCREEN_NAME, name) },
        )
    }

    override fun recordError(error: Throwable) {
        crashlytics.recordException(error)
    }
}
