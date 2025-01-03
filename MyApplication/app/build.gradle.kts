plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.lifecycle.runtime.ktx.v261)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.v150)
    implementation(libs.androidx.material3.v110)
    implementation(libs.androidx.navigation.compose.v253)
    implementation(libs.okhttp.v4110)
    implementation(libs.androidx.core.splashscreen)

    //firebase
    implementation(libs.google.firebase.analytics)
    implementation(libs.com.google.firebase.firebase.database.ktx)
    implementation(libs.kotlinx.coroutines.play.services.v173)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.foundation)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    debugImplementation(libs.androidx.ui.tooling.v150)
    debugImplementation(libs.androidx.ui.test.manifest.v150)
    implementation(libs.androidx.material3.v110)

    implementation (libs.accompanist.pager)
    implementation (libs.accompanist.pager.indicators)

}
