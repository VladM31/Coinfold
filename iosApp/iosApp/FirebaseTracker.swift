import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import Shared

/// Sends the events and errors of the shared code to Firebase. Only release builds report,
/// so testing does not pollute the numbers. Never pass amounts, notes or names.
final class FirebaseTracker: Shared.AnalyticsTracker {

    static func install() {
        FirebaseApp.configure()
        #if DEBUG
        let enabled = false
        #else
        let enabled = true
        #endif
        FirebaseAnalytics.Analytics.setAnalyticsCollectionEnabled(enabled)
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
        Shared.Analytics.shared.install(tracker: FirebaseTracker())
    }

    func logEvent(name: String, params: [String: String]) {
        FirebaseAnalytics.Analytics.logEvent(name, parameters: params)
    }

    func logScreen(name: String) {
        FirebaseAnalytics.Analytics.logEvent(AnalyticsEventScreenView, parameters: [AnalyticsParameterScreenName: name])
    }

    func recordError(error: KotlinThrowable) {
        Crashlytics.crashlytics().record(error: error.asError())
    }
}
