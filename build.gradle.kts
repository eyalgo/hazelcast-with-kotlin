import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.9.20"

    java
    jacoco
    kotlin("jvm").version(kotlinVersion)
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "11.5.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "eyalgo"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("com.hazelcast:hazelcast:5.3.5")

    val jacksonVersion = "2.15.3"
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson:jackson-bom:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    implementation("io.arrow-kt:arrow-core:1.2.1")

    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.4.0")
    testImplementation("org.awaitility:awaitility:4.2.0")
}

tasks.withType<Test> {
    useJUnitPlatform()

    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    testLogging {
        events = mutableSetOf(FAILED, PASSED, SKIPPED)
        showExceptions = true
        exceptionFormat = FULL
        showCauses = true
        showStackTraces = true
        showStandardStreams = true

        info.events = lifecycle.events
        info.exceptionFormat = lifecycle.exceptionFormat

        addTestListener(EyalgoGradleLoggingTestListener())
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
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

    compileKotlin {
        dependsOn(addKtlintFormatGitPreCommitHook)
    }

    test {
        filter {
            includeTestsMatching("*Test")
            includeTestsMatching("*Test*.*")
            excludeTestsMatching("*Architecture")
        }
    }
}

class EyalgoGradleLoggingTestListener : TestListener {
    private val successTests = mutableListOf<TestDescriptor>()
    private val failedTests = mutableListOf<TestDescriptor>()
    private val skippedTests = mutableListOf<TestDescriptor>()

    override fun beforeSuite(suite: TestDescriptor) {}
    override fun beforeTest(testDescriptor: TestDescriptor) {}
    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        when (result.resultType) {
            TestResult.ResultType.SUCCESS -> successTests.add(testDescriptor)
            TestResult.ResultType.FAILURE -> failedTests.add(testDescriptor)
            TestResult.ResultType.SKIPPED -> skippedTests.add(testDescriptor)
            else -> Unit
        }
    }

    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
        if (suite.parent == null) { // root suite
            printTestResults(successTests, failedTests, skippedTests, result)
        }
    }

    private fun printTestResults(
        successTests: List<TestDescriptor>,
        failedTests: List<TestDescriptor>,
        skippedTests: List<TestDescriptor>,
        result: TestResult
    ) {
        println("---- Test results: ${result.resultType} ----")
        println(
            "Test summary: ${result.testCount} tests, " +
                "${result.successfulTestCount} succeeded, " +
                "${result.failedTestCount} failed, " +
                "${result.skippedTestCount} skipped" +
                "in ${(result.endTime - result.startTime).toFloat() / 1000}s"
        )

        println()
        skippedTests.prefixedSummary("--- Skipped Tests: ---")
        println()
        successTests.prefixedSummary("--- Success Tests: ---")
        println()
        failedTests.prefixedSummary("--- Failed Tests: ---")
    }

    private fun List<TestDescriptor>.prefixedSummary(subject: String) {
        print(subject + System.lineSeparator())
        forEach { test -> println("${test.className} [${test.displayName}]") }
    }

    private fun TestDescriptor.displayName() = parent?.let { "${it.name} - $name" } ?: name
}
