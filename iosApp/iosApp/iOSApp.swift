import SwiftUI

@main
struct iOSApp: App {
    init() {
        FirebaseTracker.install()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
