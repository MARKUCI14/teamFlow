FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew :user-service:bootJar --no-daemon \
    && find user-service/build/libs -maxdepth 1 -name 'user-service-*.jar' ! -name '*-plain.jar' \
       -exec cp {} /tmp/user-service.jar \;

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --uid 10001 --create-home app
COPY --from=build --chown=app:app /tmp/user-service.jar /app/user-service.jar
USER app

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]
