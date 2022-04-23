plugins {
    id("com.android.application")
    kotlin("android")
}

repositories {
    mavenCentral()
    mavenLocal()
}


android {
    compileSdk = 32
    defaultConfig {
        applicationId = "org.kobjects.tantilla2.android"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-rc02"
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core"))
    implementation("org.kobjects.konsole:core:0.1.1")
    implementation("org.kobjects.konsole:compose:0.1.1")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.ui:ui:1.0.5")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:1.0.5")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:1.0.5")
    // Material Design
    implementation("androidx.compose.material:material:1.0.5")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.0.5")
    implementation("androidx.compose.material:material-icons-extended:1.0.5")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
}