# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B dependency:go-offline
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B package -DskipTests \
    && cp target/*.jar app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
# curl é usado pelo HEALTHCHECK da imagem e do docker-compose.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app app
COPY --from=build /workspace/app.jar app.jar
USER app
EXPOSE 8080
# Prontidão da aplicação (Actuator); o docker-compose usa o mesmo endpoint.
HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
    CMD curl -fsS http://localhost:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
