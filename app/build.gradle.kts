plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config = files("${rootDir}/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    parallel = true
}

android {
    namespace = "com.periodic.pro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.periodic.pro"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "0.2.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // CI/CD: 通过 GitHub Secrets 注入环境变量
            storeFile = rootProject.file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

configurations.all {
    resolutionStrategy {
        // Force specific transitive dependency versions to prevent overrides
        force(
            // Compose core — pin at 1.7.0
            "androidx.compose.ui:ui:${libs.versions.compose.core.get()}",
            "androidx.compose.ui:ui-graphics:${libs.versions.compose.core.get()}",
            "androidx.compose.ui:ui-text:${libs.versions.compose.core.get()}",
            "androidx.compose.ui:ui-unit:${libs.versions.compose.core.get()}",
            "androidx.compose.ui:ui-geometry:${libs.versions.compose.core.get()}",
            "androidx.compose.foundation:foundation:${libs.versions.compose.core.get()}",
            "androidx.compose.foundation:foundation-layout:${libs.versions.compose.core.get()}",
            "androidx.compose.animation:animation:${libs.versions.compose.core.get()}",
            "androidx.compose.animation:animation-core:${libs.versions.compose.core.get()}",
            "androidx.compose.runtime:runtime:${libs.versions.compose.core.get()}",
            "androidx.compose.runtime:runtime-saveable:${libs.versions.compose.core.get()}",
            // Activity & Core
            "androidx.activity:activity-compose:${libs.versions.activity.compose.get()}",
            "androidx.activity:activity:${libs.versions.activity.compose.get()}",
            "androidx.core:core-ktx:${libs.versions.core.ktx.get()}",
            "androidx.core:core:${libs.versions.core.ktx.get()}",
            // Lifecycle
            "androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.lifecycle.get()}",
            "androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.lifecycle.get()}",
            "androidx.lifecycle:lifecycle-runtime:${libs.versions.lifecycle.get()}",
            "androidx.lifecycle:lifecycle-runtime-ktx:${libs.versions.lifecycle.get()}",
            "androidx.lifecycle:lifecycle-common:${libs.versions.lifecycle.get()}",
        )
    }
}

dependencies {
    // Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // Navigation
    implementation(libs.compose.navigation)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Activity + Core
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.androidx.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Telephoto
    implementation(libs.telephoto.zoomable)

    // Haze
    implementation(libs.haze)

    // Adaptive Navigation Suite
    implementation(libs.compose.adaptive.navigation.suite)

    // Detekt formatting plugin
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.test.manifest)
}
