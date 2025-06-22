# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin/Spring Boot application called "koog-demo" that integrates with the Koog AI agents library. The project uses a Docker-based development environment with Gradle as the build system.

## Architecture

- **Application Layer**: Single Spring Boot application entry point at `com.koog.demo.Application`
- **Build System**: Gradle with Kotlin DSL, Java 21 toolchain
- **Dependencies**: Spring Boot 3.5.3, Kotlin 2.1.21, Koog Agents 0.2.1
- **Container**: Multi-stage Docker build with local/build/production targets
- **Development**: Docker Compose with hot reload and volume mounting

## Development Commands

### Using Task (Recommended)
- `task` - Show all available commands
- `task build` - Build Docker container with no cache
- `task up` - Start the application in Docker
- `task stop` - Stop the container
- `task down` - Stop and remove containers
- `task logs` - View container logs
- `task watch-logs` - Follow container logs in real-time

### Using Gradle Directly (Inside Container)
- `./gradlew bootRun` - Run the application
- `./gradlew build` - Build the application
- `./gradlew test` - Run tests
- `./gradlew clean` - Clean build artifacts
- `./gradlew getDependencies` - Copy runtime dependencies to runtime/ folder

### Development Setup
1. Ensure Docker and Docker Compose are installed
2. Create `.env` file if needed (defaults to dev environment)
3. Run `task up` to start the development environment
4. Application runs on port 8080

## Configuration

- **Environment**: Controlled via `APP_ENV` (defaults to "dev")
- **Logging**: Structured JSON logging with ECS format, configurable via `LOG_LEVEL`
- **Hot Reload**: Enabled via Spring DevTools in development
- **Docker Watch**: Automatically rebuilds on build.gradle.kts or libs.versions.toml changes

## Key Files

- `app/build.gradle.kts` - Main build configuration
- `app/gradle/libs.versions.toml` - Version catalog for dependencies
- `app/src/main/resources/application.yaml` - Spring configuration
- `compose.yaml` - Docker Compose setup
- `Taskfile.yaml` - Task runner commands
- `infra/docker/app/Dockerfile` - Multi-stage Docker build