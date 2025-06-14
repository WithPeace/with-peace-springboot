# Start a new stage
FROM openjdk:17-jdk-alpine

# Set timezone to Asiz/Seoul
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

WORKDIR /app
COPY app.jar app.jar
ENTRYPOINT ["java", "-jar", "./app.jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local}"]
