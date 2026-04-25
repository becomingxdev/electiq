# ── Build stage ────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
# Cache dependency downloads separately from source compilation
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Run stage ──────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Google Cloud Run injects PORT automatically; default 8080
ENV PORT=8080
EXPOSE 8080

# REQUIRED environment variables (set in Cloud Run service config — NOT here):
#   GEMINI_API_KEY   — your Google Gemini API key
#   ALLOWED_ORIGINS  — comma-separated Firebase Hosting URLs (optional, has defaults)
#
# JVM flags tuned for Cloud Run's shared CPU / memory-constrained environment
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
