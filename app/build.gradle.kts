plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "cs20a.doublezerotwo.dablist"
    compileSdk = 34

    defaultConfig {
        applicationId = "cs20a.doublezerotwo.dablist"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "002.daibilo"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}