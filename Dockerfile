# Start a new stage
FROM openjdk:17-jdk-alpine

# Set timezone to Asiz/Seoul
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

WORKDIR /app
ARG JAR_FILE=./build/libs
COPY ${JAR_FILE}/*.jar app.jar
ENTRYPOINT ["java", "-jar","./app.jar", "--spring.profiles.active=dev"]