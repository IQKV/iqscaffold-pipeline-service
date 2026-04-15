# Multi-stage Dockerfile for IQ  Key Value Pipeline Service

# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom and download dependencies (layer cache)
COPY pom.xml ./
RUN mvn dependency:go-offline -Dcheckstyle.skip=true

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -Dcheckstyle.skip=true

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk add --no-cache curl

# Non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /app/target/iqscaffold-pipeline-service-*.jar app.jar

RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
