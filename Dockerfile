# Etapa 1: Build
FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder /app/target/user-service.jar app.jar


ENTRYPOINT ["java", "-jar", "app.jar"]

