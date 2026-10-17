import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    // Reads google-services.json and uploads the mapping files that make crash traces readable.
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

// The only place to change the version. Keep it equal to MARKETING_VERSION in iosApp/Configuration/Config.xcconfig
// and to the entries of "supportedVersions" / "currentVersion" in the remote config (the gist).
val appVersion = "1.0.0"

// 1.2.3 -> 10203: grows with every release, as Google Play requires (each part must stay below 100).
val appVersionCode = appVersion.split(".").map { it.toInt() }.let { (major, minor, patch) ->
    major * 10_000 + minor * 100 + patch
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    // BiometricPrompt needs a FragmentActivity, which comes with this library
    implementation(libs.androidx.biometric)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.vm.coinfold.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.vm.coinfold.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}