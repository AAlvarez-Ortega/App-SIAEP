plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}




android {
    namespace = "com.example.app_sisaep"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app_sisaep"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

}

dependencies {

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Compose
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.6.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Core
    implementation("androidx.core:core-ktx:1.13.1")


    //coneccion con supabase

    implementation(libs.supabase.kt)
    implementation(libs.gotrue.kt)
    implementation(libs.postgrest.kt)
    implementation(libs.storage.kt)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.serialization.json)



    // Debug (opcional pero útil)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
}
