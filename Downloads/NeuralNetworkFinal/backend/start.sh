#!/bin/bash

# Start script for the Neural Network API Server

echo "Building the API server..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the errors above."
    exit 1
fi

echo ""
echo "Starting the API server..."
echo "The server will train the model on startup (this may take a few minutes)."
echo ""

java -jar target/neural-network-api-1.0-SNAPSHOT-jar-with-dependencies.jar

