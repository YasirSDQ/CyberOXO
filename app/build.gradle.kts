plugins {
    alias(libs.plugins.android.application)
    // Note: kotlin.android is bundled with AGP 9.1.1+ – applying it again
    // causes a duplicate 'kotlin' extension crash. Remove the alias below.
    // alias(libs.plugins.kotlin.android)
}

android {
    namespace = "cyberoxo.io"
    compileSdk = 36

    defaultConfig {
        applicationId = "cyberoxo.io"
        minSdk = 23
        targetSdk = 36
        versionCode = 5
        versionName = "2.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("cyberoxo.jks")
            storePassword = "cyberoxo2026"
            keyAlias = "cyberoxo"
            keyPassword = "cyberoxo2026"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}