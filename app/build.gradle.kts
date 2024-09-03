plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cbl_teams_rooms"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cbl_teams_rooms"

//        minSdk = 24
        minSdk = 22
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.play.services.nearby)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.protolite.well.known.types)
    implementation(libs.android.joda)
    implementation(libs.google.material)
    implementation(libs.lottie)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}