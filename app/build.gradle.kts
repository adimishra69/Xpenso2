plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    id("com.google.devtools.ksp") version "2.0.21-1.0.27" // Add this line (match your Kotlin version)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services) // ✅ ADD THIS
}

android {
    namespace = "com.example.xpenso"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.xpenso"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

//            isMinifyEnabled = true
////            isShrinkResources = true
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
//    implementation("androidx.compose.material:material-icons-extended")
//
//    implementation("androidx.room:room-runtime:2.6.1")
//    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.compose.foundation.layout)
//    ksp("androidx.room:room-compiler:2.6.1")

//    implementation("androidx.navigation:navigation-compose:2.7.7")
//    implementation("androidx.datastore:datastore-preferences:1.1.1")
//    implementation("androidx.biometric:biometric:1.2.0-alpha05")
//
//    implementation("androidx.work:work-runtime-ktx:2.9.0")
//
//    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
//    implementation("com.google.firebase:firebase-analytics")
//
//    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation(libs.gson)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.compose.ui.tooling)
}