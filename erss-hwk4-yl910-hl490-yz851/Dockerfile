# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

# Copy the project files to the container
COPY src /home/app/src
COPY pom.xml /home/app

# Set the working directory
WORKDIR /home/app

# Build the application and skip tests
RUN mvn clean package -DskipTests

# Expose the port the app runs on
EXPOSE 8899 12345

# Run the jar file. Adjust the JAR file name according to your actual output from Maven build.
ENTRYPOINT ["java","-jar","target/stocktrading-1.0-SNAPSHOT.jar"]
