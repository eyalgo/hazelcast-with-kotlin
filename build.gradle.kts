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

    implementation("com.hazelcast:hazelcast:5.3.6")
    /**
     * TODO
     * See [javax.cache:cache-api](https://github.com/hazelcast/hazelcast-code-samples/tree/master)
     * and [jcache](https://docs.hazelcast.com/hazelcast/5.3/jcache/jcache)
     */
    implementation("javax.cache:cache-api:1.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.4.0")

    testImplementation("com.github.blindpirate:junit5-capture-system-output-extension:0.1.2")
}

tasks.withType<Test> {
    useJUnitPlatform()

    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/test")
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
