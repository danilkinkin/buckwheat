plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "com.danilkinkin.buckwheat"
        minSdk = 29
        targetSdk = 36
        versionCode = 28
        versionName = "4.7.0"
        testInstrumentationRunner = "com.danilkinkin.buckwheat.CustomTestRunner"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    packaging {
        // Multiple dependency bring these files in. Exclude them to enable
        // our test APK to build (has no effect on our AARs)
        resources.excludes += "/META-INF/AL2.0"
        resources.excludes += "/META-INF/LGPL2.1"
    }
    namespace = "com.danilkinkin.buckwheat"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.runtime:runtime:1.8.3")
    implementation("androidx.compose.foundation:foundation:1.8.3")
    implementation("androidx.compose.foundation:foundation-layout:1.8.3")
    implementation("androidx.compose.ui:ui-util:1.8.3")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.2")
    implementation("androidx.compose.animation:animation:1.8.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.3")
    implementation("androidx.compose.runtime:runtime-livedata:1.8.3")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material:1.8.3")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    implementation("androidx.room:room-paging:2.7.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
    implementation("com.google.dagger:dagger:2.56.2")
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation("org.apache.commons:commons-csv:1.14.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    ksp("androidx.room:room-compiler:2.7.2")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    ksp("com.google.dagger:dagger-compiler:2.56.2")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling:1.8.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.8.3")
    debugImplementation("com.google.android.glance.tools:appwidget-viewer:0.2.2")

    // Testing
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test:1.8.3")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.3")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.56.2")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.56.2")
}
