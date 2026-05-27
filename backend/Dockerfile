# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
ARG HTTP_PROXY
ARG HTTPS_PROXY
ENV HTTP_PROXY=$HTTP_PROXY
ENV HTTPS_PROXY=$HTTPS_PROXY
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:resolve-plugins dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -P prod

# Stage 2: Create custom JRE
FROM eclipse-temurin:17-jdk-alpine AS jre-builder
RUN jlink \
    --add-modules java.base,java.logging,java.xml,java.sql,java.naming,java.management,java.instrument,java.security.jgss,jdk.unsupported,java.desktop,java.scripting,jdk.security.auth,java.net.http,jdk.crypto.ec,jdk.localedata \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /opt/custom-jre

# Stage 3: Runtime image
FROM alpine:3.21
RUN apk add --no-cache fontconfig ttf-dejavu tzdata
COPY --from=jre-builder /opt/custom-jre /opt/java/openjdk
COPY --from=builder /build/target/school-1.0.0.jar /app/app.jar
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create upload directory with correct permissions
RUN mkdir -p /var/data/uploads && chown -R appuser:appgroup /var/data/uploads

# Switch to non-root user
USER appuser

EXPOSE 7000
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar"]
