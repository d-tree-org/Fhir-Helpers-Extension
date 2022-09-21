import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.7.10"
    application
}

group = "com.sevenreup.fhir"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven ("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":compiler"))

    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.15.0")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.15.0")
    implementation("com.beust:jcommander:1.82")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    withType<ShadowJar>() {
        isZip64 = true
        manifest {
            attributes["Main-Class"] = "MainKt"
        }
    }
}