import java.util.Properties

val localProperties = Properties()
localProperties.load(rootProject.file("local.properties").inputStream())

val mapsApiKey = localProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: ""
val metaAccessToken = localProperties.getProperty("META_ACCESS_TOEKN") ?: ""
val revenuecatApiKey = localProperties.getProperty("REVENUE_CAT_API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.aarav.geowav"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.aarav.geowav"
        minSdk = 26
        targetSdk = 37
        versionCode = 39
        versionName = "0.9.9"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = rootProject.file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {

        debug {
            manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey
            buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$mapsApiKey\"")
            buildConfigField("String", "META_ACCESS_TOEKN", "\"$metaAccessToken\"")
            buildConfigField("String", "REVENUE_CAT_API_KEY", "\"$revenuecatApiKey\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey

            buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$mapsApiKey\"")
            buildConfigField("String", "META_ACCESS_TOEKN", "\"$metaAccessToken\"")
            buildConfigField("String", "REVENUE_CAT_API_KEY", "\"$revenuecatApiKey\"")

            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
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
//    kapt {
//        correctErrorTypes = true
//        arguments {
//            arg("room.schemaLocation", "$projectDir/schemas")
//        }
//    }
}

dependencies {
    implementation("androidx.browser:browser:1.8.0")

    implementation("com.google.firebase:firebase-perf")

    // RevenueCat
    implementation("com.revenuecat.purchases:purchases:8.10.8")
    implementation("com.revenuecat.purchases:purchases-ui:8.10.8")

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.config.ktx)

    // Crashlytics
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    //Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    //Constraint Layout

    implementation(libs.constraintlayout.compose)

    // Firebase Credential Manager
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.kotlinx.metadata.jvm)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.compose.ui.text)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.navigation.compose)

    implementation(libs.accompanist.drawablepainter)

    //Places
    implementation(libs.places)

    implementation(libs.androidx.work.runtime.ktx)
    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(platform(libs.firebase.bom))

    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)

    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
// Location Services
    implementation(libs.play.services.location)
// Lifecycle + ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.accompanist.permissions)
    implementation(libs.android.maps.utils)

    implementation(libs.androidx.concurrent.futures)
    implementation(libs.androidx.concurrent.futures.ktx)
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
