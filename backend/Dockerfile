FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY target/school-1.0.0.jar app.jar

EXPOSE 7000

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
