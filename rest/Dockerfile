FROM openjdk:8-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
ARG JAR_DEPS=target/thin/root/

COPY ${JAR_FILE} app.jar
COPY ${JAR_DEPS} deps/

ENTRYPOINT ["java", "-jar", "/app.jar", "--thin.root=deps/"]
