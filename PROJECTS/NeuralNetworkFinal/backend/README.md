# Neural Network API Server

REST API server for handwritten digit recognition using a neural network trained on the MNIST dataset.

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Server

**Linux/Mac:**
```bash
./start.sh
```

**Windows:**
```cmd
start.bat
```

**Manual:**
```bash
mvn clean package
java -jar target/neural-network-api-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The server will:
1. Load MNIST training data
2. Train the neural network (takes a few minutes)
3. Start the API server on `http://localhost:8080`

## API Endpoints

### POST /predict
Predicts a digit from a 28x28 pixel array.

**Request:**
```json
{
  "pixels": [0.0, 0.1, ..., 0.9]  // 784 values (0-1 normalized)
}
```

**Response:**
```json
{
  "prediction": 5,
  "confidence": 87.5
}
```

### GET /health
Health check endpoint.

**Response:**
```json
{
  "status": "healthy",
  "model": "loaded"
}
```

## Project Structure

```
backend/
├── src/main/java/nn/
│   ├── APIServer.java      # REST API server
│   ├── NeuralNetwork.java  # Neural network implementation
│   ├── Layer.java          # Neural network layer
│   ├── MnistLoader.java    # MNIST dataset loader
│   └── Main.java           # Original training script
├── src/main/resources/data/  # MNIST dataset files
├── pom.xml                 # Maven configuration
├── start.sh                # Linux/Mac startup script
└── start.bat               # Windows startup script
```

## Notes

- The model trains on startup (6000 samples, 40 epochs)
- Model persistence is not yet implemented, so it trains each time
- The server includes CORS headers to allow requests from the frontend

