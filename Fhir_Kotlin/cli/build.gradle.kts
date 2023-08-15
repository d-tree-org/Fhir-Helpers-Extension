import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.7.10"
    kotlin("kapt")
    application
}

group = "com.sevenreup.fhir.compiler"
version = "0.0.2-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven ("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("info.picocli:picocli:4.7.4")
    kapt("info.picocli:picocli-codegen:4.7.4")

    testImplementation(kotlin("test"))
    implementation("com.github.ajalt:mordant:1.2.1")
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