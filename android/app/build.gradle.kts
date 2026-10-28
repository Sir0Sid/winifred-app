plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "net.winifred.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.winifred.app"
        /* 26 is Android 8, and it is Hattie's floor too. */
        minSdk = 26
        targetSdk = 34
        /* BUMPED ON EVERY BUILD. A version that cannot be read off the
         * installed app has already cost a round trip once. */
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            /* Not minified on purpose: an appliance is not an app where
             * size matters, and a stack trace from a customer's phone is
             * worth more than a smaller APK. */
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

/* No dependencies, on purpose. See settings.gradle.kts. */
