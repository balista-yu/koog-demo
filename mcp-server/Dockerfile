FROM gradle:8.11.1-jdk21 AS builder

WORKDIR /app

# Copy Gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Build the application
RUN gradle build --no-daemon

FROM amazoncorretto:21-alpine

WORKDIR /app

# Install required tools
RUN apk add --no-cache curl bash

# Copy the built JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Create app user
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD echo '{"jsonrpc":"2.0","method":"tools/list","id":1}' | java -jar app.jar || exit 1

EXPOSE 3001

CMD ["java", "-jar", "app.jar"]