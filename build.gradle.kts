plugins {
    // Android Gradle Plugin
    id("com.android.application") version "8.2.0" apply false

    // Kotlin
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false

    // Navigation SafeArgs (for type-safe navigation)
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false

    // Firebase Google Services plugin (required for Firebase)
    id("com.google.gms.google-services") version "4.4.1" apply false
}

