import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization") version "2.2.0-Beta2"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ch4019.jdaassist"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ch4019.jdaassist"
        minSdk = 29
        targetSdk = 36
        versionCode = 33
        versionName = "1.2.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
//        vectorDrawables {
//            useSupportLibrary = true
//        }
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")    //只保留 arm 架构
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.create("releaseConfig") {
                enableV3Signing = true
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

//    kotlinOptions {
//        jvmTarget = "21"
//    }
    buildFeatures {
        compose = true
    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.15"
//    }
//    buildToolsVersion = "36.0.0"
    packaging {
        resources {
            excludes += listOf(
                "META-INF/*.readme",
                "/META-INF/README.md",
                "/META-INF/{AL2.0,LGPL2.1}"
            )
            // ... 其他要排除的资源文件
        }
        // ... 其他排除规则
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
//    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
}

//kapt {
//    correctErrorTypes = true
//}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.okhttp)
    implementation(libs.jsoup)
    implementation(libs.converter.serialization)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.preferences.core)
    implementation(libs.konfetti.compose)
    implementation(libs.splash.screen)
}