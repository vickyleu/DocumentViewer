@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.dokka.DokkaDefaults.outputDir
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @Suppress("OPT_IN_USAGE")
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xexpect-actual-classes", // remove warnings for expect classes
            "-Xskip-prerelease-check",
            "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
            "-opt-in=org.jetbrains.compose.resources.InternalResourceApi",
        )
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
    applyDefaultHierarchyTemplate()

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
            implementation(libs.coil.core)
            implementation(libs.coil.compose)
        }
        androidMain.get().apply {
            kotlin.srcDir("src/androidMain/kotlin")
            resources.srcDir("src/androidMain/res")
            // 添加解压后的资源和类文件
//            resources.srcDir("$outputDir/res")
//            kotlin.srcDir("$outputDir/classes.jar")
        }
        androidMain.dependencies {
            //libs/TbsFileSdk.aar
            // aar
            implementation( fileTree("${project.layout.buildDirectory.get().asFile}/expanded-aar/TbsFileSdk"){
                include("classes.jar")
            } )
//            implementation(files("src/androidMain/libs/TbsFileSdk.aar"))

        }

    }
}

val aarFile = file("$projectDir/src/androidMain/libs/TbsFileSdk.aar")
val outputDir = file("${project.layout.buildDirectory.get().asFile}/expanded-aar/TbsFileSdk")
tasks.register<Copy>("extractAar") {
    from(zipTree(aarFile))
    into(outputDir)
}
// 在 preBuild 之前执行解压任务
tasks.named("preBuild").configure {
    dependsOn("extractAar")
}
android {
    namespace = "org.uooc.document"
    compileSdk = 34
    defaultConfig {
        minSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }

    sourceSets {
        getByName("main").apply {
            res.srcDirs("src/androidMain/res","${project.layout.buildDirectory.get().asFile}/expanded-aar/TbsFileSdk/res")
            assets.srcDir("${project.layout.buildDirectory.get().asFile}/expanded-aar/TbsFileSdk/assets")
        }
    }
    publishing{
        singleVariant("release"){
            withSourcesJar()
            withJavadocJar()
        }
    }
}
