FROM gradle:jdk11 AS build

WORKDIR /app

COPY Fhir_Kotlin /app/Fhir_Kotlin

# Build the fat JAR using the Shadow plugin
RUN gradle -p /app/Fhir_Kotlin :cli:shadowJar

RUN mv /app/Fhir_Kotlin/cli/build/libs/cli-*-all.jar /app/cli.jar


# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "cli.jar"]
