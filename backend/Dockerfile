# ---- build stage: full JDK, builds the jar, then is thrown away ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
# the cache mount keeps downloaded Maven dependencies between builds
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests

# ---- runtime stage: JRE only, no compiler or build tools ----
FROM eclipse-temurin:21-jre
# curl is only here for the container healthcheck
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER app
EXPOSE 8080
# size the heap from the container's memory limit instead of the whole host
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
