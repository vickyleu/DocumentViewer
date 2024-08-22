@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "viewer"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material)
            implementation(compose.material3)
//            implementation(compose.components.resources)
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(project.dependencies.platform(libs.coroutines.bom))
//            implementation(project.dependencies.platform(libs.coil.bom))
            //put your multiplatform dependencies here
            implementation(libs.compose.filepicker)
        }
        androidMain.get().apply {
            kotlin.srcDir("src/androidMain/kotlin")
            resources.srcDir("src/androidMain/res")
        }
        androidMain.dependencies {
            //libs/TbsFileSdk.aar
            // aar
            implementation(files("src/androidMain/libs/TbsFileSdk.aar"))

        }

    }
}

android {
    namespace = "org.uooc.document"
    compileSdk = 34
    defaultConfig {
        minSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publishing{
        singleVariant("release"){
            withSourcesJar()
            withJavadocJar()
        }
    }
}
