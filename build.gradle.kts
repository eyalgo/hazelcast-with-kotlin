plugins {
    // val kotlinVersion="1.9.21"

    java
    kotlin("jvm").version("1.9.21")

    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "11.5.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "0.1"
group = "eyalgo"

val kotlinVersion = project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    implementation("com.hazelcast:hazelcast:5.3.5")
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

tasks {
    addKtlintFormatGitPreCommitHook {
        mustRunAfter(
            "processResources",
            "processTestResources",
            "loadKtlintReporters",
            "runKtlintCheckOverKotlinScripts",
            "runKtlintCheckOverMainSourceSet",
            "runKtlintCheckOverTestSourceSet",
            "ktlintKotlinScriptCheck",
            "ktlintMainSourceSetCheck",
            "ktlintTestSourceSetCheck"
        )
    }
}
