plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
}

group = "com.google.android.fhir"
version = "unspecified"

repositories {
    google()
    mavenCentral()
}

dependencies {
}

tasks.test {
    useJUnitPlatform()
}