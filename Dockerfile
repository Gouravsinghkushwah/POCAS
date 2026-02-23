# Multi-stage build for React + Spring Boot
FROM node:16-alpine AS frontend-build

WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

# Spring Boot stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

COPY src ./src

# Copy React build from frontend stage
COPY --from=frontend-build /app/frontend/build src/main/resources/static

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/POCAS-0.0.1-SNAPSHOT.jar"]
