FROM eclipse-temurin:21-jdk AS local
WORKDIR /app
COPY /app /app
ENV TZ=Asia/Tokyo
RUN chmod +x gradlew && ./gradlew clean getDependencies -x test --stacktrace

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*
COPY /app/gradlew /app/
COPY /app/gradle /app/gradle
COPY /app/*.gradle.kts /app/
RUN chmod +x gradlew && ./gradlew dependencies --daemon --parallel --build-cache
COPY /app/src /app/src
RUN chmod +x gradlew && ./gradlew build -x test --daemon --parallel --build-cache

FROM eclipse-temurin:21-jre AS production
ENV TZ=Asia/Tokyo
RUN useradd -m -u 1001 spring
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R spring:spring /app
USER spring
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
