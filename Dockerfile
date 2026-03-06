FROM gradle:8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/ktor-app-all.jar /ktor-app.jar
WORKDIR /
ENTRYPOINT ["java", "-jar", "ktor-app.jar"]