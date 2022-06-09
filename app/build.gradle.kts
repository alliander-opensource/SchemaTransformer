/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.3.3/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.1.1-jre")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // https://mvnrepository.com/artifact/org.eclipse.rdf4j/rdf4j-model
    implementation("org.eclipse.rdf4j:rdf4j-model:4.0.0")

    // https://mvnrepository.com/artifact/org.eclipse.rdf4j/rdf4j-rio-turtle
    implementation("org.eclipse.rdf4j:rdf4j-rio-turtle:4.0.0")
    implementation("org.eclipse.rdf4j:rdf4j-repository-sparql:4.0.0")

    implementation("org.eclipse.rdf4j:rdf4j-sail:4.0.0")
    implementation("org.apache.avro:avro:1.11.0")

    // https://mvnrepository.com/artifact/org.eclipse.rdf4j/rdf4j-rio-rdfxml
    implementation("org.eclipse.rdf4j:rdf4j-rio-rdfxml:4.0.0")
    implementation("org.eclipse.rdf4j:rdf4j-repository-sail:4.0.0")
    implementation("org.eclipse.rdf4j:rdf4j-sail-memory:4.0.0")
    implementation("org.eclipse.rdf4j:rdf4j-sparqlbuilder:4.0.0")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-nop
    implementation("org.slf4j:slf4j-nop:1.7.30")

    // Kotest.
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.1.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.1.0")
    // testImplementation("io.kotest:kotest-property-jvm:5.1.0")
    implementation("info.picocli:picocli:4.6.3")


    // Arrow.
//    implementation(platform("io.arrow-kt:arrow-stack:1.0.1"))

//    implementation("io.arrow-kt:arrow-core")
//    implementation("io.arrow-kt:arrow-optics")
    // implementation("io.arrow-kt:arrow-fx-coroutines")
}

application {
    // Define the main class for the application.
    mainClass.set("schematransformer.AppKt")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
