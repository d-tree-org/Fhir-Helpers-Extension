import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.rnett.krosstalk") version "1.4.0"
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
    implementation("com.github.arteam:simple-json-rpc-server:1.3")

    implementation("io.ktor:ktor-server-core:2.2.2")
    implementation("io.ktor:ktor-server-netty:2.2.2")
    implementation("io.ktor:ktor-server-websockets:2.2.2")
    implementation(project(mapOf("path" to ":core")))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("com.sevenreup.fhir.server.MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    withType<ShadowJar>() {
        isZip64 = true
        manifest {
            attributes["Main-Class"] = "com.sevenreup.fhir.server.MainKt"
        }
    }
}