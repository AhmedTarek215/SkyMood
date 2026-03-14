plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.skymood"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.skymood"
        minSdk = 26
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.androidx.compose.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Retrofit & Gson
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Glide (if needed)
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation ("com.github.bumptech.glide:compose:1.0.0-beta01")
    
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose-android:2.8.7")
    
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-ktx:${room_version}")
    implementation("androidx.room:room-runtime:${room_version}")
    ksp("androidx.room:room-compiler:${room_version}")

    // LiveData & Compose
    val compose_version = "1.7.5"
    implementation ("androidx.compose.runtime:runtime-livedata:$compose_version")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Coil for Image Loading in Compose
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Location Services (GPS)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // OSMDroid (OpenStreetMap)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // Coroutines support for Play Services Tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    
    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")
}
