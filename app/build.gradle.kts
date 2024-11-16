plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.anvil)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.build.secrets)
}

android {
    namespace = "dev.hossain.timeline"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.hossain.timeline"
        // Android 11 (API level 30) https://developer.android.com/tools/releases/platforms#11
        minSdk = 30
        // Android 15 (API level 35) https://developer.android.com/tools/releases/platforms#15
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
        buildConfig = true
    }
}

// https://developers.google.com/maps/documentation/android-sdk/secrets-gradle-plugin
secrets {
    // A properties file containing secret values.
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored
    ignoreList.add("sdk.*") // Ignore all keys matching the regexp "sdk.*"
}

dependencies {
    // App dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.circuit.codegen.annotations)
    implementation(libs.circuit.foundation)
    implementation(libs.circuit.overlay)
    implementation(libs.circuitx.android)
    implementation(libs.circuitx.effects)
    implementation(libs.circuitx.gestureNav)
    implementation(libs.circuitx.overlays)
    ksp(libs.circuit.codegen)

    implementation(libs.dagger)
    // ksp(libs.dagger.compiler) // Not working for some reason. Kapt worked.
    kapt(libs.dagger.compiler)

    implementation(libs.anvil.annotations)
    implementation(libs.anvil.annotations.optional)

    implementation(libs.timber)

    implementation(libs.okio)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    implementation(libs.google.maps.compose)
    implementation(libs.google.maps.compose.utils)
    implementation(libs.google.maps.compose.widgets)

    implementation(files("libs/device-timeline-lib-v1.3.jar"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

ksp {
    // `@CircuitInject` - https://slackhq.github.io/circuit/code-gen/#code-generation
    // Anvil-KSP
    arg("anvil-ksp-extraContributingAnnotations", "com.slack.circuit.codegen.annotations.CircuitInject")
    // kotlin-inject-anvil (requires 0.0.3+)
    arg("kotlin-inject-anvil-contributing-annotations", "com.slack.circuit.codegen.annotations.CircuitInject")
}
