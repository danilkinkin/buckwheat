plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.luna.dollargrain"
        minSdk = 26
        targetSdk = 34
        versionCode = 28
        versionName = "4.7.0"
        testInstrumentationRunner = "com.luna.dollargrain.CustomTestRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["dagger.hilt.disableModulesHaveInstallInCheck"] = "true"
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
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

    packaging {
        // Multiple dependency bring these files in. Exclude them to enable
        // our test APK to build (has no effect on our AARs)
        resources.excludes += "/META-INF/AL2.0"
        resources.excludes += "/META-INF/LGPL2.1"
    }
    namespace = "com.danilkinkin.dollargrain"
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.runtime:runtime:1.7.6")
    implementation("androidx.compose.foundation:foundation:1.7.6")
    implementation("androidx.compose.foundation:foundation-layout:1.7.6")
    implementation("androidx.compose.ui:ui-util:1.7.6")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
    implementation("androidx.compose.animation:animation:1.7.6")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.6")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.6")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material:1.7.6")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.25.1")
    implementation("com.google.dagger:dagger:2.49")
    implementation("com.google.dagger:hilt-android:2.49")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("io.coil-kt:coil-compose:2.3.0")
    ksp("androidx.room:room-compiler:2.6.1")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    kapt("com.google.dagger:dagger-compiler:2.46.1")
    kapt("com.google.dagger:hilt-android-compiler:2.46.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.6")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.6")
    debugImplementation("com.google.android.glance.tools:appwidget-viewer:0.2.2")

    // Testing
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test:1.7.6")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.6")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.46.1")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.46.1")
}