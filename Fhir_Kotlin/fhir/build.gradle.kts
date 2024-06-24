plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
}

group = "com.google.android.fhir"
version = "unspecified"

repositories {
    google()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

object Versions {
    // Maximum Jackson libraries (excluding core) version that supports Android API Level 24:
    // https://github.com/FasterXML/jackson-databind/issues/3658
    const val jackson = "2.13.5"

    // Maximum Jackson Core library version that supports Android API Level 24:
    const val jacksonCore = "2.15.2"
    const val jsonToolsPatch = "1.13"
    const val jsonAssert = "1.5.1"

}

object Dependencies {
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

    const val jsonToolsPatch = "com.github.java-json-tools:json-patch:${Versions.jsonToolsPatch}"
}


fun hapiFhirConstraints(): Map<String, String> {
    return mutableMapOf(
        Dependencies.Jackson.annotationsBase to Versions.jackson,
        Dependencies.Jackson.bomBase to Versions.jackson,
        Dependencies.Jackson.coreBase to Versions.jacksonCore,
        Dependencies.Jackson.databindBase to Versions.jackson,
        Dependencies.Jackson.jaxbAnnotationsBase to Versions.jackson,
        Dependencies.Jackson.jsr310Base to Versions.jackson,
        Dependencies.Jackson.dataformatXmlBase to Versions.jackson,
        Dependencies.jsonToolsPatch to Versions.jsonToolsPatch
    )
}

dependencies {
    hapiFhirConstraints().forEach { (libName, constraints) ->
        implementation(libName)
    }
}

tasks.test {
    useJUnitPlatform()
}