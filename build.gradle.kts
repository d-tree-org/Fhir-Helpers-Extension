import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.sevenreup"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven ("https://oss.sonatype.org/content/repositories/snapshots/")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

                implementation("org.jetbrains.compose.material:material-icons-extended:1.2.0-alpha01-dev620")
                implementation("org.jetbrains.compose.components:components-splitpane-desktop:1.2.0-alpha01-dev774")
                implementation("com.google.fhir:r4:0.6.1")
                implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.3.0")

                implementation("org.opencds.cqf:cql-engine-fhir:1.3.14-SNAPSHOT")
                implementation("org.opencds.cqf:cql-engine:1.3.14-SNAPSHOT")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "FhirDebug"
            packageVersion = "1.0.0"
            includeAllModules = true
        }
    }
}