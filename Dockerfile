FROM openjdk:25-jdk-slim

WORKDIR /app

# Copy the source code
COPY . .

# Build the application
RUN ./gradlew assemble -x test

# Run the application
CMD ["./gradlew", "bootRun"]

