# Use Amazon Corretto 17 as the base image
FROM amazoncorretto:17

# Set the working directory in the container
WORKDIR /app

# Copy the build.gradle.kts and settings.gradle.kts files
COPY build.gradle.kts settings.gradle.kts ./

# Copy the source code
COPY src ./src

# Copy the gradle wrapper
COPY gradlew ./
# Copy gradle wrapper files
COPY gradle ./gradle

# Install dependencies and build the application
RUN ./gradlew build -x test

# Expose the port the app runs on
EXPOSE 8080

# Define the command to run the application
CMD ["java", "-jar", "build/libs/user-service-0.0.1-SNAPSHOT.jar"]