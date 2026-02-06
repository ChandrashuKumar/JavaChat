# Use Java 17 as base image
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy source files and the JDBC driver
COPY *.java .
COPY mysql-connector-j-9.6.0.jar .

# Compile all Java files with the JDBC driver in classpath
RUN javac -cp ".:mysql-connector-j-9.6.0.jar" *.java

# Run the ChatServer
CMD ["java", "-cp", ".:mysql-connector-j-9.6.0.jar", "ChatServer"]
