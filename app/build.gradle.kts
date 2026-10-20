plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ir.arzprice.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "ir.arzprice.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "2.0"
    }

    // امضای ثابت: از این به بعد هر نسخه‌ی جدید مستقیم روی نسخه‌ی قبلی نصب می‌شود
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.work:work-runtime:2.9.1")
}
