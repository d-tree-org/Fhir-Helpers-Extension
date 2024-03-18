plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
    kotlin("plugin.serialization") version "1.7.10"
}

group = "com.sevenreup.fhir"
version = "0.0.1-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven ("https://oss.sonatype.org/content/repositories/snapshots/")

}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")

    implementation("com.jayway.jsonpath:json-path:2.7.0")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.1")

    implementation("com.charleskorn.kaml:kaml:0.47.0")

    implementation("org.hamcrest:hamcrest-core:2.2")
    implementation("com.google.fhir:r4:0.6.1")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.3.0")
    implementation("org.opencds.cqf:cql-engine-fhir:1.3.14-SNAPSHOT")
    implementation("org.opencds.cqf:cql-engine:1.3.14-SNAPSHOT")
    implementation ("ca.uhn.hapi.fhir:hapi-fhir-validation:6.0.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}