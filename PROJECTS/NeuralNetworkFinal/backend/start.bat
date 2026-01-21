@echo off
REM Start script for the Neural Network API Server (Windows)

echo Building the API server...
call mvn clean package

if errorlevel 1 (
    echo Build failed. Please check the errors above.
    exit /b 1
)

echo.
echo Starting the API server...
echo The server will train the model on startup (this may take a few minutes).
echo.

java -jar target\neural-network-api-1.0-SNAPSHOT-jar-with-dependencies.jar

