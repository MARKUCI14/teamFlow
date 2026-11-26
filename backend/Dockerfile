FROM eclipse-temurin:21-jdk AS build

ARG MODULE
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew \
    && ./gradlew ":${MODULE}:bootJar" --no-daemon \
    && find "${MODULE}/build/libs" -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' \
       -exec cp {} /tmp/service.jar \;

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --uid 10001 --create-home app
COPY --from=build --chown=app:app /tmp/service.jar /app/service.jar
USER app

ENTRYPOINT ["java", "-jar", "/app/service.jar"]
