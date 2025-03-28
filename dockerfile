# Stage 1: Build the application using Java 17 and Maven
FROM eclipse-temurin:21-jdk-alpine AS build
# Install Maven in the Alpine container
RUN apk add --no-cache maven
WORKDIR /app
# Copy the pom.xml and source files
COPY pom.xml .
COPY src ./src
# Package the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image using Java 17
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Copy the built jar from the build stage (adjust jar name if necessary)
COPY --from=build /app/target/RESTfulWeb-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
