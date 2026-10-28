/* Winifred — the phone app.
 *
 * ONE MODULE, NO LIBRARIES, exactly like Hattie's: the app depends on
 * nothing but the Android platform and Kotlin, so it stays buildable
 * for years without a dependency that has moved. She is a WebView
 * around https://winifred-ai.net/app/ — the conversation, the sign-in
 * and the settings are all the web app's, and this is only the shell
 * that makes it installable and gives it a place on the home screen.
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Winifred"
include(":app")
