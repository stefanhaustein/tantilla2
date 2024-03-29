plugins {
    id("com.android.application")
    kotlin("android")
}

repositories {
    mavenCentral()
    mavenLocal()
}


android {
    compileSdk = 33
    defaultConfig {
        applicationId = "org.kobjects.tantilla2.android"
        minSdk = 21
        targetSdk = 33
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
    namespace = "org.kobjects.tantilla2.android"
}

dependencies {
    implementation(project(":core"))
    implementation("org.kobjects.konsole:core:0.3.0")
    implementation("org.kobjects.parserlib:core:0.6.0")
    implementation("org.kobjects.konsole:compose:0.3.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.ui:ui:1.3.2")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:1.3.2")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:1.3.1")
    // Material Design
    implementation("androidx.compose.material:material:1.3.1")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
}