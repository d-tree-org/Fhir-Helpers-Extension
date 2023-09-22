import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.7.10"
    kotlin("kapt")
    application
}

group = "com.sevenreup.fhir.cli"
version = "0.0.5-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven ("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
    implementation("com.github.ajalt.mordant:mordant:2.1.0")
    implementation("info.picocli:picocli:4.7.4")
    kapt("info.picocli:picocli-codegen:4.7.4")

    testImplementation(kotlin("test"))
    implementation(project(":core"))
}

application {
    mainClass.set("com.sevenreup.fhir.cli.MainKt")
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    withType<ShadowJar>() {
        isZip64 = true
        manifest {
            attributes["Main-Class"] = "com.sevenreup.fhir.cli.MainKt"
        }
    }

    withType<CreateStartScripts>() {
        applicationName = "fhir-tools"
    }
}