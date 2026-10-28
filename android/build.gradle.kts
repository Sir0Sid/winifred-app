/* Winifred — the phone app.
 *
 * The versions are Hattie's, on purpose: that build already works in
 * GitHub Actions, and there is no reason for two appliances to be built
 * by two different toolchains.
 */
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
