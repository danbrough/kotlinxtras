/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.6.21"))
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain {
        check(this is JavaToolchainSpec)
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
