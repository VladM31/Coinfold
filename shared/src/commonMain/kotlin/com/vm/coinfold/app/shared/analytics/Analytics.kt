package com.vm.coinfold.app.shared.analytics

/** What the platform reports to (Firebase on Android). Never pass amounts, notes, names or other personal data. */
interface AnalyticsTracker {
    fun logEvent(name: String, params: Map<String, String>)
    fun logScreen(name: String)

    /** A handled error that is worth seeing next to the crashes. */
    fun recordError(error: Throwable)

    /** Turns reporting on or off. It stays off until the user has accepted the privacy policy. */
    fun setCollectionEnabled(enabled: Boolean)
}

/** Does nothing: the default until a platform installs a real tracker (and on platforms without one). */
private object NoopTracker : AnalyticsTracker {
    override fun logEvent(name: String, params: Map<String, String>) = Unit
    override fun logScreen(name: String) = Unit
    override fun recordError(error: Throwable) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

/** Entry point for the shared code; reporting must never be able to break the app. */
object Analytics {
    private var tracker: AnalyticsTracker = NoopTracker

    fun install(tracker: AnalyticsTracker) {
        this.tracker = tracker
    }

    fun event(name: String, vararg params: Pair<String, String>) {
        runCatching { tracker.logEvent(name, params.toMap()) }
    }

    fun screen(name: String) {
        runCatching { tracker.logScreen(name) }
    }

    fun setCollectionEnabled(enabled: Boolean) {
        runCatching { tracker.setCollectionEnabled(enabled) }
    }

    fun error(error: Throwable) {
        runCatching { tracker.recordError(error) }
    }
}

/** Event names, kept in one place so the dashboards stay consistent. */
object AnalyticsEvent {
    const val EXPENSE_ADDED = "expense_added"
    const val MANUAL_TRANSACTION_ADDED = "manual_transaction_added"
    const val TRANSFER_ADDED = "transfer_added"
    const val RECURRING_SAVED = "recurring_saved"
    const val BACKUP_CREATED = "backup_created"
    const val BACKUP_RESTORED = "backup_restored"
    const val EXPORT_CSV = "export_csv"
    const val EXPORT_PDF = "export_pdf"
}
