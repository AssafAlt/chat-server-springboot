# First stage: build the JAR file
FROM maven:3.8.1-openjdk-11-slim AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .


# Copy the source code
COPY src ./src


 RUN mvn clean package

# Second stage: create a smaller runtime image
FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/chatapp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

