FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew :work-management-service:bootJar --no-daemon \
    && find work-management-service/build/libs -maxdepth 1 -name 'work-management-service-*.jar' ! -name '*-plain.jar' \
       -exec cp {} /tmp/work-management-service.jar \;

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --uid 10001 --create-home app
COPY --from=build --chown=app:app /tmp/work-management-service.jar /app/work-management-service.jar
USER app

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/work-management-service.jar"]
