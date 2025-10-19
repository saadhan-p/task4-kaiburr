# Stage 1: Build the application using a full Maven and JDK image
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight image
# Copy only the built .jar file from the 'build' stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/task1-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]