# Dockerfile

# Stage 1: Build the application (Must be named using AS)
# 🎯 FIX 1: Name the first stage 'build'
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# ... (rest of Stage 1)

# Stage 2: Create the final, lightweight image
FROM openjdk:11-jre-slim
WORKDIR /app
# 🎯 FIX 2: Reference the name 'build' from Stage 1
COPY --from=build /app/target/*.jar app.jar 
ENTRYPOINT ["java", "-jar", "app.jar"]