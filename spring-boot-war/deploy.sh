#!/bin/bash

# Stop any running containers
docker-compose down

# Build the WAR file
./gradlew clean build

# Build and start the Docker containers
docker-compose up --build -d

echo "Application is being deployed..."
echo "Wait a few seconds and then visit http://localhost:8080/test"
echo "To view logs, run: docker-compose logs -f" 