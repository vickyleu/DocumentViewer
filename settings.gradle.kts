@file:Suppress("UnstableApiUsage")

/*
* Copyright 2023-2024 onseok
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// WorkQueue error throw in Iguana
//gradle.startParameter.excludedTaskNames.addAll(listOf(
//    ":buildSrc:testClasses",
//    ":rust_plugin:testClasses",
//))

pluginManagement {
    repositories.apply {
        removeAll(this)
    }
    dependencyResolutionManagement.repositories.apply {
        removeAll(this)
    }
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            mavenCentral{
                content{
                    includeGroupByRegex("io.github.*")
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.android.tools.*")
                    excludeGroupByRegex("androidx.compose.*")
                    excludeGroupByRegex("com.github.(?!johnrengelman|oshi).*")
                }
            }
            gradlePluginPortal {
                content{
                    excludeGroupByRegex("media.kamel.*")
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("androidx.databinding.*")
                    // 避免无效请求,加快gradle 同步依赖的速度
                    excludeGroupByRegex("com.github.(?!johnrengelman).*")
                }
            }
            google {
                content {
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("org.jogamp.*")
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven(url = "https://androidx.dev/storage/compose-compiler/repository") {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev") {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven {
                setUrl("https://jogamp.org/deployment/maven")
                content {
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    includeGroupByRegex("org.jogamp.*")
                    includeGroupByRegex("dev.datlag.*")
                }
            }
            maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        }
    }
    resolutionStrategy {
        val properties = java.util.Properties()
        rootDir.resolve("gradle/libs.versions.toml").inputStream().use(properties::load)
        val kotlinVersion = properties.getProperty("kotlin").removeSurrounding("\"")
        eachPlugin {
            if (requested.id.id == "dev.icerock.mobile.multiplatform-resources") {
                useModule("dev.icerock.moko:resources-generator:${requested.version}")
            }
            else if(requested.id.id.startsWith("org.jetbrains.kotlin")){
                useVersion(kotlinVersion)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}


dependencyResolutionManagement {
    //FAIL_ON_PROJECT_REPOS
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral{
            content {
                includeGroupByRegex("io.github.*")
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
        }
        google {
            content {
                excludeGroupByRegex("org.jogamp.*")
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech).*")
            }
        }

        // workaround for https://youtrack.jetbrains.com/issue/KT-51379
        maven { setUrl("https://repo.maven.apache.org/maven2")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
        }
        ivy {
            name = "Node.js"
            setUrl("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                excludeGroupByRegex("org.jogamp.*")
                includeModule("org.nodejs", "node")
            }
            isAllowInsecureProtocol = false
        }
        maven {
            setUrl("https://jitpack.io")
            content {
                excludeGroupByRegex("org.jogamp.*")
                includeGroupByRegex("com.github.*")
                includeGroupByRegex("io.github.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
            }
        }
        maven {
            setUrl("https://repo1.maven.org/maven2/")
            content {
                excludeGroupByRegex("org.jetbrains.compose.*")
                includeGroupByRegex("org.jogamp.gluegen.*")
            }
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/public/")
            content {
                excludeGroupByRegex("org.jogamp.*")
                includeGroupByRegex("com.aliyun.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.github.vickyleu.*")
                excludeGroupByRegex("io.github.vickyleu.*")
            }
        }
        maven {
            setUrl("https://maven.aliyun.com/nexus/content/repositories/jcenter/")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
        }
        maven { setUrl("https://maven.pkg.jetbrains.space/public/p/compose/dev")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven {
            setUrl("https://jogamp.org/deployment/maven")
            content {
                includeGroupByRegex("dev.datlag.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
            }
        }
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental"){
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }


        maven {
            url = uri("https://maven.pkg.github.com/vickyleu/compose_image_picker")
            val properties = java.util.Properties().apply {
                runCatching { rootProject.projectDir.resolve("local.properties") }
                    .getOrNull()
                    .takeIf { it?.exists() ?: false }
                    ?.reader()
                    ?.use(::load)
            }
            val environment: Map<String, String?> = System.getenv()
            extra["githubToken"] = properties["github.token"] as? String
                ?: environment["GITHUB_TOKEN"] ?: ""
            credentials {
                username = "vickyleu"
                password = extra["githubToken"]?.toString()
            }
            content {
                excludeGroupByRegex("com.finogeeks.*")
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("(?!com|cn).github.(?!vickyleu).*")
            }
        }

    }
}

rootProject.name = "DocumentViewer"
include(
    ":viewer",
)