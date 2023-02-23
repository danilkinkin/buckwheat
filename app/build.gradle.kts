import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("io.sentry.android.gradle")
}

val sentryProperties = Properties()
sentryProperties.load(FileInputStream(rootProject.file("sentry.properties")))

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.danilkinkin.buckwheat"
        minSdk = 26
        targetSdk = 33
        versionCode = 11
        versionName = "1.4"
        testInstrumentationRunner = "com.danilkinkin.buckwheat.CustomTestRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["dagger.hilt.disableModulesHaveInstallInCheck"] = "true"
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")

            buildConfigField("String", "SENTRY_DSN", "\"\"")
        }

        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")

            buildConfigField("String", "SENTRY_DSN", "\"" + sentryProperties["auth.dsn"] + "\"")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    namespace = "com.danilkinkin.buckwheat"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.runtime:runtime:1.3.3")
    implementation("androidx.compose.foundation:foundation:1.3.1")
    implementation("androidx.compose.foundation:foundation-layout:1.3.1")
    implementation("androidx.compose.ui:ui-util:1.3.3")
    implementation("androidx.compose.material3:material3-window-size-class:1.0.1")
    implementation("androidx.compose.animation:animation:1.3.3")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.3")
    implementation("androidx.compose.runtime:runtime-livedata:1.3.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.3.3")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.material:material:1.3.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.25.1")
    implementation("androidx.room:room-runtime:2.5.0")
    kapt("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")
    implementation("com.google.dagger:dagger:2.44")
    kapt("com.google.dagger:dagger-compiler:2.44")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("com.google.dagger:hilt-android:2.44")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("androidx.glance:glance-appwidget:1.0.0-alpha05")
    implementation("androidx.compose.ui:ui:1.3.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("androidx.core:core-splashscreen:1.0.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.3.3")
    debugImplementation("com.google.android.glance.tools:appwidget-viewer:0.2.2")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation("androidx.compose.ui:ui-test:1.3.3")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.3.3")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}

// For more info see: https://docs.sentry.io/platforms/android/gradle/
sentry {
    includeProguardMapping.set(true)
    autoUploadProguardMapping.set(true)
    experimentalGuardsquareSupport.set(true)
    uploadNativeSymbols.set(true)
    includeNativeSources.set(true)
    tracingInstrumentation {
        enabled.set(true)
        features.set(
            setOf(
                io.sentry.android.gradle.extensions.InstrumentationFeature.COMPOSE,
            )
        )
    }
    autoInstallation {
        enabled.set(true)
        sentryVersion.set("6.6.0")
    }
    includeDependenciesReport.set(true)
    ignoredBuildTypes.set(setOf("debug"))
}

secrets {
    defaultPropertiesFileName = "local.defaults.properties"
}
