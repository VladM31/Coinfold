import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import Shared

/// Sends the events and errors of the shared code to Firebase. Only release builds report,
/// so testing does not pollute the numbers. Never pass amounts, notes or names.
final class FirebaseTracker: Shared.AnalyticsTracker {

    /// Collection is off in Info.plist and is turned on by the shared code only after the user
    /// accepted the privacy policy.
    static func install() {
        FirebaseApp.configure()
        Shared.Analytics.shared.install(tracker: FirebaseTracker())
    }

    func setCollectionEnabled(enabled: Bool) {
        #if DEBUG
        let allowed = false
        #else
        let allowed = enabled
        #endif
        FirebaseAnalytics.Analytics.setAnalyticsCollectionEnabled(allowed)
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(allowed)
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
