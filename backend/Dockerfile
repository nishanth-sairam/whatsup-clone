# Multi-stage build for Spring Boot application
FROM maven:3.9.4-eclipse-temurin-25 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create uploads directory
RUN mkdir -p /app/uploads

# Copy the built JAR file
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 9090

# Run the application with increased memory and file upload optimizations
ENTRYPOINT ["java", "-Xmx1G", "-Xms512m", "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]