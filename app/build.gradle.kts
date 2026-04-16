plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}




android {
    namespace = "com.example.app_sisaep"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app_sisaep"
        minSdk = 29
        targetSdk = 36
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
    implementation("androidx.navigation:navigation-compose:2.9.7")

    // ✅ Compose BOM (alinea TODAS las versiones)
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))

    // Compose
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Opcionales (si los usas)
    implementation("androidx.compose.material:material-icons-extended") // o deja tu libs.androidx... si ya funciona
    implementation(libs.coil.compose)

    // AppCompat / Material (puedes dejarlos)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Core
    implementation("androidx.core:core-ktx:1.13.1")

    // QR
    implementation(libs.zxing.core)

    // Supabase
    implementation(libs.supabase.kt)
    implementation(libs.androidx.datastore.preferences)
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.0")

    // Ktor (solo 1 engine)
    implementation(libs.ktor.client.okhttp)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // ✅ Debug (SIN versiones, también lo controla el BOM)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

// ML Kit QR/Barcodes
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


}
