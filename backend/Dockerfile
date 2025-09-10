# Multi-stage build for Spring Boot application
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create uploads directory
RUN mkdir -p /app/uploads

# Copy the built JAR file
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 9090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]