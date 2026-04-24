# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Google Cloud Run uses the PORT environment variable
ENV PORT 8080
EXPOSE 8080

# Production-ready entry point
ENTRYPOINT ["java", "-jar", "app.jar"]
