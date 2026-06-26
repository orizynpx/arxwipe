plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.crashlytics)
    id("com.google.gms.google-services")
}

android {
    namespace = "io.github.orizynpx.arxwipe"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.orizynpx.arxwipe"
        minSdk = 26
        //noinspection OldTargetApi
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.material)
    
    testImplementation(libs.junit)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.androidx.recyclerview)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    ksp(libs.kotlin.metadata.jvm)
    implementation(libs.serialization)

    // Firebase things
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)

    implementation(libs.timber)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.retrofit)
    implementation(libs.okhttp)

    implementation(libs.kotlinx.serialization.json)
}
