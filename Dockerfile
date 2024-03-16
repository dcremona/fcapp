FROM openjdk:17-jdk-slim
RUN mkdir -p /app
RUN mkdir -p /app/data
RUN mkdir -p /app/pdf
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]