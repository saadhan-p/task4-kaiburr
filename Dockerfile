# Stage 1: Build the application
FROM maven:3.8-openjdk-17 AS builder_stage
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests


FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder_stage /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
