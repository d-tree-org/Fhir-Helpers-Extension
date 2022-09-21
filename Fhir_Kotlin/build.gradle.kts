plugins {
    kotlin("jvm") version "1.7.10"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven ("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}