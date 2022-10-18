@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.danilkinkin.buckwheat"
        minSdk = 26
        targetSdk = 33
        versionCode = 4
        versionName = "alpha-4.0"
        testInstrumentationRunner = "com.danilkinkin.buckwheat.CustomTestRunner"
        manifestPlaceholders["sentryDsn"] = ""
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["dagger.hilt.disableModulesHaveInstallInCheck"] = "true"
            }
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

        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-benchmark-rules.pro",
            )
            isDebuggable = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true

    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0"
    }

    packagingOptions {
        // Multiple dependency bring these files in. Exclude them to enable
        // our test APK to build (has no effect on our AARs)
        resources.excludes += "/META-INF/AL2.0"
        resources.excludes += "/META-INF/LGPL2.1"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.activity:activity-compose:1.6.0")
    implementation("androidx.compose.runtime:runtime:1.2.1")
    implementation("androidx.compose.foundation:foundation:1.2.1")
    implementation("androidx.compose.foundation:foundation-layout:1.2.1")
    implementation("androidx.compose.ui:ui-util:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.0.0-beta03")
    implementation("androidx.compose.animation:animation:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.2.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.2.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.2.1")
    implementation("androidx.navigation:navigation-compose:2.5.2")
    implementation("androidx.compose.material3:material3:1.0.0-beta03")
    implementation("androidx.compose.material:material:1.2.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.25.1")

    implementation("androidx.room:room-runtime:2.5.0-alpha03")
    kapt("androidx.room:room-compiler:2.5.0-alpha03")
    implementation("androidx.room:room-ktx:2.5.0-alpha03")

    implementation("androidx.room:room-paging:2.4.3")
    implementation("androidx.paging:paging-compose:1.0.0-alpha16")

    implementation("com.google.dagger:dagger:2.44")
    kapt("com.google.dagger:dagger-compiler:2.44")


    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation("io.sentry:sentry-android:6.5.0")
    implementation("io.sentry:sentry-compose-android:6.5.0")

    debugImplementation("androidx.compose.ui:ui-test-manifest:1.2.1")

    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation("androidx.compose.ui:ui-test:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.2.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}

secrets {
    defaultPropertiesFileName = "local.defaults.properties"
}