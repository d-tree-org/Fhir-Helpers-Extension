plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
}

group = "com.sevenreup.fhir"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven ("https://oss.sonatype.org/content/repositories/snapshots/")

}

dependencies {
    implementation("com.jayway.jsonpath:json-path:2.7.0")
    implementation("org.hamcrest:hamcrest-core:2.2")
    implementation("com.google.fhir:r4:0.6.1")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.3.0")
    implementation("org.opencds.cqf:cql-engine-fhir:1.3.14-SNAPSHOT")
    implementation("org.opencds.cqf:cql-engine:1.3.14-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}