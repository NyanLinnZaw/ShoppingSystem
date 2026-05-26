# syntax=docker/dockerfile:1

# -----------------------------------------------------------------------------
# Stage 1: builder — compile and package the Spring Boot application
# Java version matches pom.xml (<java.version>17</java.version>)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy dependency descriptors first so Docker can cache Maven downloads
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw \
    && ./mvnw dependency:go-offline -B

# Copy source only after dependencies are resolved
COPY src ./src

RUN ./mvnw clean package -DskipTests -B

# -----------------------------------------------------------------------------
# Stage 2: runtime — minimal JRE image with only the packaged JAR
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# curl supports Docker Compose health checks (no actuator in this project)
RUN apk add --no-cache curl \
    && addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/target/ShoppingSystem-*.jar app.jar

USER spring:spring

EXPOSE 8082

# Container-aware JVM settings; port overridden via SERVER_PORT in Compose
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
