plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "wulf.compose"
    compileSdk = 35

    defaultConfig {
        // 24 y no más alto: es lo que corre en los teléfonos baratos que son la
        // escena de uso real de este sistema, y nada de lo que hace pide más.
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures { compose = true }

    // Las pruebas de JVM leen el archivo Dart para comprobar la paridad; no
    // necesitan aparato ni recursos de Android.
    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    implementation(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.ui.graphics)
    api(libs.compose.material3)
    implementation(libs.androidx.core.ktx)
    compileOnly(libs.compose.ui.tooling.preview)

    testImplementation(libs.junit)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.graphics)
}
