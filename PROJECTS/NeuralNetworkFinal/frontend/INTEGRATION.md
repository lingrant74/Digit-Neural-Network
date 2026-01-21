# Frontend-Backend Integration Guide

This project consists of a React frontend and a Java backend for handwritten digit recognition.

## Architecture

- **Frontend**: React + TypeScript + Vite (runs on port 5173)
- **Backend**: Java REST API Server (runs on port 8080)
- **Neural Network**: Java implementation trained on MNIST dataset

## Prerequisites

### Backend Requirements
- Java 17 or higher
- Maven 3.6 or higher

### Frontend Requirements
- Node.js 16+ and npm

## Setup and Running

### 1. Start the Backend API Server

Navigate to the backend directory:

```bash
cd backend
```

**Option A: Using the startup script (recommended)**

On Linux/Mac:
```bash
./start.sh
```

On Windows:
```cmd
start.bat
```

**Option B: Manual build and run**

```bash
mvn clean package
java -jar target/neural-network-api-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The backend will:
1. Load the MNIST training data
2. Train the neural network (this takes a few minutes)
3. Start the API server on port 8080

**Note**: The first startup will take several minutes as the model trains. Subsequent runs will also train (currently, model persistence is not implemented).

### 2. Start the Frontend

Open a new terminal and navigate to the project root:

```bash
npm install  # Only needed the first time
npm run dev
```

The frontend will start on `http://localhost:5173`

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

## How It Works

1. **Frontend**: User draws a digit on the canvas (280x280 pixels)
2. **Preprocessing**: The canvas is resized to 28x28 and converted to grayscale (784 pixel values normalized 0-1)
3. **API Call**: Frontend sends POST request to `http://localhost:8080/predict` with pixel array
4. **Prediction**: Backend neural network processes the input and returns prediction + confidence
5. **Display**: Frontend displays the predicted digit and confidence score

## Development Notes

### Backend Structure
```
backend/
├── src/main/java/nn/      # Java source files
│   ├── APIServer.java     # REST API server
│   ├── NeuralNetwork.java # Neural network implementation
│   ├── Layer.java         # Neural network layer
│   ├── MnistLoader.java   # MNIST dataset loader
│   └── Main.java          # Original training script
├── src/main/resources/data/  # MNIST dataset files
└── pom.xml                # Maven configuration
```

### Frontend Structure
```
src/
├── pages/
│   └── Index.tsx          # Main page with canvas and prediction
├── components/
│   ├── DrawingCanvas.tsx  # Drawing canvas component
│   └── PredictionResult.tsx # Prediction display component
└── hooks/
    └── useDigitPrediction.ts # API hook for predictions
```

## Troubleshooting

### Backend won't start
- Ensure Java 17+ is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Check that the data files exist in `src/main/resources/data/`

### Frontend can't connect to backend
- Ensure the backend is running on port 8080
- Check browser console for CORS errors (CORS should be enabled)
- Verify the API URL in the frontend matches the backend port

### Model training takes too long
- The model trains on 6000 samples for 40 epochs
- This is intentional to balance accuracy and startup time
- For production, consider saving/loading a trained model

## Future Improvements

- [ ] Save trained model to disk and load on startup
- [ ] Add model serialization/deserialization
- [ ] Implement model versioning
- [ ] Add more training data for better accuracy
- [ ] Add batch prediction endpoint
- [ ] Add authentication/rate limiting
- [ ] Docker containerization

