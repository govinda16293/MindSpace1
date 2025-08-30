pluginManagement {
    repositories {
        google()                // ✅ Android & ML Kit plugins
        gradlePluginPortal()    // ✅ Kotlin & Navigation plugins
        mavenCentral()          // ✅ Backup repo
        maven("https://jitpack.io") // ✅ Added: for MPAndroidChart and other GitHub-hosted libs
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()                // ✅ AndroidX, Firebase, ML Kit
        mavenCentral()          // ✅ Retrofit, Room, etc.
        maven("https://jitpack.io") // ✅ Added: for MPAndroidChart
    }
}

rootProject.name = "MindSpace"
include(":app")
