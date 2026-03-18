import java.util.Properties

val localProperties = Properties()
localProperties.load(rootProject.file("local.properties").inputStream())

val mapsApiKey = localProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.aarav.geowav"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aarav.geowav"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.9.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {

        debug {

            manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey
            buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$mapsApiKey\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey

            buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$mapsApiKey\"")
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
        buildConfig = true
    }

    kapt {
        correctErrorTypes = true
    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = "2.0.0"
//    }
}

dependencies {

    // Play Billing
    val billing_version = "8.3.0"

    implementation("com.android.billingclient:billing-ktx:$billing_version")

    // Firebase
    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.firebase:firebase-config-ktx")

//    implementation("com.google.firebase:firebase-analytics")
////    implementation("com.google.firebase:firebase-inappmessaging-display")
////    implementation("com.google.firebase:firebase-ai")
//    implementation("com.google.firebase:firebase-crashlytics-ndk")

    // Crashlytics
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    //Coil
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    //Constraint Layout

    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Firebase Credential Manager
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.compose.ui.text)
    kapt("com.google.dagger:hilt-android-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // Navigation
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    implementation("androidx.navigation:navigation-compose:2.9.5")

    implementation("com.google.accompanist:accompanist-drawablepainter:0.34.0")

    //Places
    implementation("com.google.android.libraries.places:places:5.0.0")

    implementation(libs.androidx.work.runtime.ktx)
    // Room
    val room_version = "2.8.2"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")


    // Maps Compose
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
// Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")
// Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.firebase:firebase-config-ktx:22.1.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}