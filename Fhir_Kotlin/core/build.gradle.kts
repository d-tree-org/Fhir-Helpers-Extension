import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyConstraint
import org.gradle.kotlin.dsl.exclude

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

object CoreDependencies {

    object HapiFhir {
        const val fhirBaseModule = "ca.uhn.hapi.fhir:hapi-fhir-base"
        const val fhirClientModule = "ca.uhn.hapi.fhir:hapi-fhir-client"
        const val structuresDstu2Module = "ca.uhn.hapi.fhir:hapi-fhir-structures-dstu2"
        const val structuresDstu3Module = "ca.uhn.hapi.fhir:hapi-fhir-structures-dstu3"
        const val structuresR4Module = "ca.uhn.hapi.fhir:hapi-fhir-structures-r4"
        const val structuresR4bModule = "ca.uhn.hapi.fhir:hapi-fhir-structures-r4b"
        const val structuresR5Module = "ca.uhn.hapi.fhir:hapi-fhir-structures-r5"

        const val validationModule = "ca.uhn.hapi.fhir:hapi-fhir-validation"
        const val validationDstu3Module = "ca.uhn.hapi.fhir:hapi-fhir-validation-resources-dstu3"
        const val validationR4Module = "ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r4"
        const val validationR5Module = "ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r5"

        const val fhirCoreDstu2Module = "ca.uhn.hapi.fhir:org.hl7.fhir.dstu2"
        const val fhirCoreDstu2016Module = "ca.uhn.hapi.fhir:org.hl7.fhir.dstu2016may"
        const val fhirCoreDstu3Module = "ca.uhn.hapi.fhir:org.hl7.fhir.dstu3"
        const val fhirCoreR4Module = "ca.uhn.hapi.fhir:org.hl7.fhir.r4"
        const val fhirCoreR4bModule = "ca.uhn.hapi.fhir:org.hl7.fhir.r4b"
        const val fhirCoreR5Module = "ca.uhn.hapi.fhir:org.hl7.fhir.r5"
        const val fhirCoreUtilsModule = "ca.uhn.hapi.fhir:org.hl7.fhir.utilities"
        const val fhirCoreConvertorsModule = "ca.uhn.hapi.fhir:org.hl7.fhir.convertors"

        const val guavaCachingModule = "ca.uhn.hapi.fhir:hapi-fhir-caching-guava"

        const val fhirBase = "$fhirBaseModule:${Versions.hapiFhir}"
        const val fhirClient = "$fhirClientModule:${Versions.hapiFhir}"
        const val structuresDstu2 = "$structuresDstu2Module:${Versions.hapiFhir}"
        const val structuresDstu3 = "$structuresDstu3Module:${Versions.hapiFhir}"
        const val structuresR4 = "$structuresR4Module:${Versions.hapiFhir}"
        const val structuresR4b = "$structuresR4bModule:${Versions.hapiFhir}"
        const val structuresR5 = "$structuresR5Module:${Versions.hapiFhir}"

        const val validation = "$validationModule:${Versions.hapiFhir}"
        const val validationDstu3 = "$validationDstu3Module:${Versions.hapiFhir}"
        const val validationR4 = "$validationR4Module:${Versions.hapiFhir}"
        const val validationR5 = "$validationR5Module:${Versions.hapiFhir}"

        const val fhirCoreDstu2 = "$fhirCoreDstu2Module:${Versions.hapiFhirCore}"
        const val fhirCoreDstu2016 = "$fhirCoreDstu2016Module:${Versions.hapiFhirCore}"
        const val fhirCoreDstu3 = "$fhirCoreDstu3Module:${Versions.hapiFhirCore}"
        const val fhirCoreR4 = "$fhirCoreR4Module:${Versions.hapiFhirCore}"
        const val fhirCoreR4b = "$fhirCoreR4bModule:${Versions.hapiFhirCore}"
        const val fhirCoreR5 = "$fhirCoreR5Module:${Versions.hapiFhirCore}"
        const val fhirCoreUtils = "$fhirCoreUtilsModule:${Versions.hapiFhirCore}"
        const val fhirCoreConvertors = "$fhirCoreConvertorsModule:${Versions.hapiFhirCore}"

        const val guavaCaching = "$guavaCachingModule:${Versions.hapiFhir}"
    }

    object Jackson {
        private const val mainGroup = "com.fasterxml.jackson"
        private const val coreGroup = "$mainGroup.core"
        private const val dataformatGroup = "$mainGroup.dataformat"
        private const val datatypeGroup = "$mainGroup.datatype"
        private const val moduleGroup = "$mainGroup.module"

        const val annotationsBase = "$coreGroup:jackson-annotations:${Versions.jackson}"
        const val bomBase = "$mainGroup:jackson-bom:${Versions.jackson}"
        const val coreBase = "$coreGroup:jackson-core:${Versions.jacksonCore}"
        const val databindBase = "$coreGroup:jackson-databind:${Versions.jackson}"
        const val dataformatXmlBase = "$dataformatGroup:jackson-dataformat-xml:${Versions.jackson}"
        const val jaxbAnnotationsBase =
            "$moduleGroup:jackson-module-jaxb-annotations:${Versions.jackson}"
        const val jsr310Base = "$datatypeGroup:jackson-datatype-jsr310:${Versions.jackson}"
    }

    object Versions {
        const val hapiFhir = "6.8.0"
        const val hapiFhirCore = "6.0.22"

        // Maximum Jackson libraries (excluding core) version that supports Android API Level 24:
        // https://github.com/FasterXML/jackson-databind/issues/3658
        const val jackson = "2.13.5"

        // Maximum Jackson Core library version that supports Android API Level 24:
        const val jacksonCore = "2.15.2"
    }

    fun Configuration.removeIncompatibleDependencies() {
        exclude(module = "xpp3")
        exclude(module = "xpp3_min")
        exclude(module = "xmlpull")
        exclude(module = "javax.json")
        exclude(module = "jcl-over-slf4j")
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "org.antlr", module = "antlr4")
        exclude(group = "org.eclipse.persistence", module = "org.eclipse.persistence.moxy")
        exclude(module = "hapi-fhir-caching-caffeine")
        exclude(group = "com.github.ben-manes.caffeine", module = "caffeine")
    }

    fun hapiFhirConstraints(): Map<String, DependencyConstraint.() -> Unit> {
        return mutableMapOf<String, DependencyConstraint.() -> Unit>(
            HapiFhir.fhirBaseModule to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.fhirClientModule to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.fhirCoreConvertorsModule to { version { strictly(Versions.hapiFhirCore) } },
            HapiFhir.fhirCoreDstu2Module to { version { strictly(Versions.hapiFhirCore) } },
            HapiFhir.fhirCoreDstu2016Module to { version { strictly(Versions.hapiFhirCore) } },
            HapiFhir.fhirCoreDstu3Module to { version { strictly(Versions.hapiFhirCore) } },
            HapiFhir.fhirCoreR4Module to { version { strictly(Versions.hapiFhirCore) } },
            HapiFhir.fhirCoreR4bModule to { version { strictly(Versions.hapiFhirCore) } },
            HapiFhir.fhirCoreR5Module to { version { strictly(Versions.hapiFhirCore) } },
            HapiFhir.fhirCoreUtilsModule to { version { strictly(Versions.hapiFhirCore) } },
            HapiFhir.structuresDstu2Module to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.structuresDstu3Module to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.structuresR4Module to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.structuresR5Module to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.validationModule to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.validationDstu3Module to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.validationR4Module to { version { strictly(Versions.hapiFhir) } },
            HapiFhir.validationR5Module to { version { strictly(Versions.hapiFhir) } },
            Jackson.annotationsBase to { version { strictly(Versions.jackson) } },
            Jackson.bomBase to { version { strictly(Versions.jackson) } },
            Jackson.coreBase to { version { strictly(Versions.jacksonCore) } },
            Jackson.databindBase to { version { strictly(Versions.jackson) } },
            Jackson.jaxbAnnotationsBase to { version { strictly(Versions.jackson) } },
            Jackson.jsr310Base to { version { strictly(Versions.jackson) } },
            Jackson.dataformatXmlBase to { version { strictly(Versions.jackson) } },
        )
    }
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

    implementation("org.slf4j:slf4j-simple:2.0.12")

    implementation(CoreDependencies.HapiFhir.structuresR4) { exclude(module = "junit") }
    implementation(CoreDependencies.HapiFhir.guavaCaching)
    implementation(CoreDependencies.HapiFhir.validationR4)
    implementation(CoreDependencies.HapiFhir.validation) {
        exclude(module = "commons-logging")
        exclude(module = "httpclient")
    }

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}