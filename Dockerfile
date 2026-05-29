# syntax=docker/dockerfile:1

# --- Builder ---
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

# --- Runtime ---
FROM eclipse-temurin:17-jre-alpine AS runtime
RUN apk add --no-cache curl

RUN addgroup -S spring && adduser -S spring -G spring
USER spring
WORKDIR /app

COPY --from=builder /build/target/ShoppingSystem-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
