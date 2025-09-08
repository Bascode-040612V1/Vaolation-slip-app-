plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.aics.violationapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aics.violationapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Secure keystore configuration using gradle.properties
            val keystoreFile = project.findProperty("KEYSTORE_FILE") as String?
            val keystorePassword = project.findProperty("KEYSTORE_PASSWORD") as String?
            val keyAlias = project.findProperty("KEY_ALIAS") as String?
            val keyPassword = project.findProperty("KEY_PASSWORD") as String?
            
            if (keystoreFile != null && 
                keystorePassword != null && 
                keyAlias != null && 
                keyPassword != null &&
                file(keystoreFile).exists()) {
                
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
                
                logger.info("Using release keystore: $keystoreFile")
            } else {
                // For debug/development builds, use the default debug signing
                logger.warn("Release signing configuration not found or keystore file missing. Will use debug signing as fallback.")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Use release signing if properly configured, otherwise fall back to debug
            val releaseSigningConfig = signingConfigs.getByName("release")
            signingConfig = if (releaseSigningConfig.storeFile?.exists() == true) {
                logger.info("Using release signing configuration")
                releaseSigningConfig
            } else {
                logger.warn("Using debug signing for release build due to missing/invalid keystore configuration")
                signingConfigs.getByName("debug")
            }
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    
    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    
    // Coroutines
    implementation(libs.coroutines.android)
    
    // SharedPreferences
    implementation(libs.androidx.preference.ktx)
    
    // Material Icons Extended
    implementation(libs.androidx.material.icons.extended)
    
    // Image loading
    implementation(libs.coil.compose)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}