FROM eclipse-temurin:21-jre

WORKDIR /app

ENV APP_NAME=discovery-portal-flow
ENV TZ=UTC

COPY ./build/libs/portal.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
