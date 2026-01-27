
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.disastermanager"
    compileSdk = 35

    // 2. We only need ONE consolidated defaultConfig block
    defaultConfig {
        applicationId = "com.example.disastermanager"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 3. SECURE ACCESS TO MAPBOX KEY
        // This looks in your local.properties file for MAPBOX_ACCESS_TOKEN
        val mapboxToken: String = project.findProperty("MAPBOX_ACCESS_TOKEN") as String? ?: ""

        // This makes the key accessible in your code via R.string.mapbox_access_token
        resValue("string", "mapbox_access_token", mapboxToken)

        // This makes the key accessible via BuildConfig.MAPBOX_ACCESS_TOKEN (Optional but helpful)
        buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"$mapboxToken\"")
    }

    buildFeatures {
        buildConfig = true // Required for buildConfigField to work
        compose = true
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
}

// 4. Dependencies remain the same, but I've cleaned up the duplicates for you
dependencies {
    // Icons & UI
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation(libs.androidx.navigation.compose)

    // Mapbox
    implementation("com.mapbox.maps:android-ndk27:11.17.0")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.17.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-turf:6.15.0")

    // Google Services
    implementation("com.google.maps.android:maps-compose:6.4.1")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Networking & Utilities
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging")

    // AndroidX & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.runtime.livedata)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}