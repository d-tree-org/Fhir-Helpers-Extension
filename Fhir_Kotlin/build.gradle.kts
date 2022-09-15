import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.7.10"
    application
}

group = "com.sevenreup"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven ("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.fhir:r4:0.6.1")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.3.0")
    implementation("org.opencds.cqf:cql-engine-fhir:1.3.14-SNAPSHOT")
    implementation("org.opencds.cqf:cql-engine:1.3.14-SNAPSHOT")
}

application {
    mainClass.set("MainKt")
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
            attributes["Main-Class"] = "MainKt"
        }
    }
}