# Multi-stage build for React + Spring Boot
FROM node:18-alpine AS frontend-build

# Create app directory and set permissions
RUN mkdir /app && chown node:node /app
WORKDIR /app/frontend
USER node

COPY --chown=node:node frontend/package*.json ./
RUN npm install

COPY --chown=node:node frontend/ ./
RUN chmod +x node_modules/.bin/react-scripts
RUN npm run build

# Switch back to root for Spring Boot stage
USER root

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
