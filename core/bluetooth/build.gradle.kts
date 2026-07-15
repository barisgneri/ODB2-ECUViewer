import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.plugin)
}

android {
    namespace = "com.baris.core.bluetooth"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:obd"))
    implementation(project(":core:model"))

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.hilt.android)
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.hilt.compiler)
}