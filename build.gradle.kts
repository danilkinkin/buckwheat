buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.3.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.46")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:6.20.0")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }

    pluginManager.apply("com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            // by default the target is every '.kt' and '.kts` file in the java sourcesets
            ktfmt()    // has its own section below
            ktlint()   // has its own section below
            diktat()   // has its own section below
            prettier() // has its own section below
            licenseHeaderFile(rootProject.file("spotless/copyright.kt")) // or licenseHeaderFile
        }
        kotlinGradle {
            target("*.gradle.kts") // default target for kotlinGradle
            ktlint() // or ktfmt() or prettier()
        }
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.47.0"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
