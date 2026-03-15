
FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
VOLUME ["/app/data"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

