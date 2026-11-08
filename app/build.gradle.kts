import java.util.Properties

plugins {
    // Kotlin itself comes from AGP 9's built-in Kotlin support; the standalone
    // 'org.jetbrains.kotlin.android' plugin is rejected from AGP 9.0 onwards.
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Release signing material, so the keystore is never committed and a release build never falls
 * back to the debug keystore (a debug-signed release cannot be verified, cannot be upgraded
 * over, and is not uploadable).
 *
 * Values are looked up in three places, in order: a Gradle property (-P), an environment
 * variable, then the git-ignored local.properties.
 *
 *   RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD
 *
 * When none of them are present (contributor machines, CI forks, F-Droid) the release variant
 * is left unsigned and the build still succeeds.
 */
val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.isFile) {
            file.inputStream().use { load(it) }
        }
    }

fun signingValue(name: String) =
    providers
        .gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .orElse(providers.provider { localProperties.getProperty(name) })

/**
 * Local smoke testing only: `./gradlew assembleRelease -PsignWithDebugKey` produces an
 * installable, minified APK signed with the debug key. It is off by default because such an
 * artifact must never be published or shipped to users.
 */
val signReleaseWithDebugKey =
    providers
        .gradleProperty("signWithDebugKey")
        // `-PsignWithDebugKey` sets an empty string, not "true".
        .map { value -> value.isEmpty() || value.toBoolean() }
        .orElse(false)

val releaseStoreFile = signingValue("RELEASE_STORE_FILE")
val releaseStorePassword = signingValue("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = signingValue("RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingValue("RELEASE_KEY_PASSWORD")

android {
    compileSdk = 37

    defaultConfig {
        // Personal fork: distinct applicationId so this installs alongside upstream Buckwheat
        // instead of upgrading it. The code namespace below intentionally stays unchanged, so
        // package declarations and generated references (Hilt, Room, tests) need no churn.
        applicationId = "com.varashines.eva"
        minSdk = 29
        targetSdk = 36
        versionCode = 60
        versionName = "5.5.2-dev"
        testInstrumentationRunner = "com.danilkinkin.buckwheat.CustomTestRunner"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
    }

    signingConfigs {
        if (releaseStoreFile.isPresent) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile.get())
                storePassword = releaseStorePassword.orNull
                keyAlias = releaseKeyAlias.orNull
                keyPassword = releaseKeyPassword.orNull
            }
        }
    }

    buildTypes {
        getByName("debug") {
            // Personal fork: sign debug builds with the release key when one is configured.
            // A debug-signed install cannot be upgraded by a release-signed APK — Android
            // rejects the signature mismatch and the only fix is uninstall + reinstall, which
            // loses the ledger. Sharing one key now makes that switch a normal in-place update.
            // Contributor machines without credentials still fall back to the debug keystore.
            signingConfig = signingConfigs.findByName("release")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            // A real keystore wins; -PsignWithDebugKey is an explicit local-testing opt-in;
            // otherwise the release variant stays unsigned rather than silently debug-signed.
            signingConfig = signingConfigs.findByName("release")
                ?: if (signReleaseWithDebugKey.get()) signingConfigs.getByName("debug") else null

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

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hilt.android)
    implementation(libs.dagger)
    implementation(libs.commons.csv)

    ksp(libs.androidx.room.compiler)
    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.android.compiler)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Local unit tests (JVM only, no device): ./gradlew testDebugUnitTest
    testImplementation(libs.junit)

    // Instrumented tests (device/emulator): ./gradlew connectedDebugAndroidTest
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
}
