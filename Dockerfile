# Stage 1: Build the application
FROM maven:3.8.6-openjdk-17 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and the source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the first stage
COPY --from=builder /app/target/my-springboot-app.jar /app/chatapp.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/chatapp.jar"]
