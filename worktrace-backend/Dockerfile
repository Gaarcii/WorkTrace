# Multi-stage Dockerfile para Spring Boot (Gradle)

FROM gradle:8-jdk21 AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
# Ejecuta gradle wrapper si existe, usa --no-daemon para builds reproducibles
RUN ./gradlew clean bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy
ARG JAR_FILE=build/libs/*.jar
WORKDIR /app
COPY --from=builder /home/gradle/project/${JAR_FILE} app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
RUN apt-get update \
	&& apt-get install -y --no-install-recommends postgresql-client \
	&& rm -rf /var/lib/apt/lists/*

COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
