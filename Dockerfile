#FROM openjdk:17-jdk-slim
FROM maven:3-openjdk-17-slim as build
RUN apt-get update -y && apt-get install -y libfontconfig1
RUN mkdir -p /app
RUN mkdir -p /app/data
RUN mkdir -p /app/pdf
COPY data/*.png /app/data
COPY data/*.pdf /app/pdf
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]