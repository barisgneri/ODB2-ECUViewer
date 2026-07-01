plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.baris.core.bluetooth"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }
}

dependencies {
    implementation(project(":core:obd"))
    implementation(project(":core:model"))

    // Coroutines, Flow vb. kütüphaneler
}