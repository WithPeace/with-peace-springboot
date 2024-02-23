# Start a new stage
FROM openjdk:17-jdk-alpine
WORKDIR /app
ARG JAR_FILE=./build/libs
COPY ${JAR_FILE}/*.jar app.jar
ENTRYPOINT ["java", "-jar","./app.jar", "--spring.profiles.active=dev"]