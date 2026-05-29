# syntax=docker/dockerfile:1

# --- Build stage ---
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/

RUN ./mvnw clean package -DskipTests -B

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine AS runtime

RUN apk add --no-cache curl \
    && addgroup -S spring \
    && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /app/target/ShoppingSystem-*.jar /app/app.jar

RUN chown spring:spring /app/app.jar

USER spring

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
