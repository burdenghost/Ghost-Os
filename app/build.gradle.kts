plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ghostos.mobile"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.ghostos.mobile"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "0.1.0-alpha"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
    composeOptions {
        // Compose Compiler 1.3.2 requires Kotlin 1.7.20.
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    packagingOptions {
        resources { excludes += setOf("/META-INF/AL2.0", "/META-INF/LGPL2.1") }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2022.10.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2022.10.00"))
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
