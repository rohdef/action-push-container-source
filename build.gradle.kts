@file:OptIn(ExperimentalDistributionDsl::class)

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    kotlin("multiplatform") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "kotlin.test.actions"
version = "0.0.1"
description = ""

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    js(IR) {
        nodejs {
            distribution {
                outputDirectory.set(projectDir.resolve("output"))
            }
        }

        binaries.executable()
        generateTypeScriptDefinitions()
        useCommonJs()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation  ("org.jetbrains.kotlin-wrappers:kotlin-node:20.14.10-pre.804")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
                implementation("com.charleskorn.kaml:kaml:0.72.0")

                implementation(npm("@actions/core", "^1.11.1"))
                implementation(npm("@docker/actions-toolkit", "^0.47.0"))
            }
        }
    }
}