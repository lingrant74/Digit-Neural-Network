package nn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class APIServer {
    private static NeuralNetwork neuralNetwork;
    private static final Gson gson = new Gson();
    private static final int PORT = 8080;
    private static final String MODEL_FILE = "model.json";

    public static void main(String[] args) throws Exception {
        System.out.println("Initializing Neural Network...");
        
        // Check for retrain flag
        boolean forceRetrain = false;
        for (String arg : args) {
            if (arg.equals("--retrain") || arg.equals("-r")) {
                forceRetrain = true;
                System.out.println("Retrain flag detected. Will train a new model...");
                break;
            }
        }
        
        // Try to load saved model first (unless retrain is forced)
        File modelFile = new File(MODEL_FILE);
        if (!forceRetrain && modelFile.exists()) {
            System.out.println("Found saved model. Loading from " + MODEL_FILE + "...");
            try {
                neuralNetwork = NeuralNetwork.load(MODEL_FILE);
                System.out.println("Model loaded successfully!");
            } catch (Exception e) {
                System.err.println("Failed to load model: " + e.getMessage());
                System.out.println("Will train a new model...");
            }
        } else if (forceRetrain && modelFile.exists()) {
            System.out.println("Retraining requested. Existing model will be overwritten.");
        }
        
        // Train model if it doesn't exist or failed to load or retrain is forced
        if (neuralNetwork == null) {
            System.out.println("No saved model found. Training new model...");
            
            // Load and train the model - try resources first, then fallback to data directory
            String trainImages = getResourcePath("data/train-images-idx3-ubyte");
            String trainLabels = getResourcePath("data/train-labels-idx1-ubyte");
            String testImages = getResourcePath("data/t10k-images-idx3-ubyte");
            String testLabels = getResourcePath("data/t10k-labels-idx1-ubyte");

            // Load training data
            System.out.println("Loading training data...");
            MnistLoader.DataPoint[] train = MnistLoader.load(trainImages, trainLabels, 60000);
            MnistLoader.DataPoint[] test = MnistLoader.load(testImages, testLabels, 0); // 0 = load all

            // Build and train the neural network
            int[] sizes = {784, 64, 10};
            neuralNetwork = new NeuralNetwork(sizes);

            System.out.println("Training neural network...");
            int epochs = 1000;
            double lr = 0.005;
            Random rng = new Random(42);

            NeuralNetwork.DataPoint[] oneSampleBatch = new NeuralNetwork.DataPoint[1];

            for (int e = 1; e <= epochs; e++) {
                shuffle(train, rng);

                for (MnistLoader.DataPoint dp : train) {
                    oneSampleBatch[0] = new NeuralNetwork.DataPoint(dp.inputs(), dp.expectedOutputs());
                    neuralNetwork.learn(oneSampleBatch, lr);
                }

                // Evaluate accuracy
                int correct = 0;
                for (MnistLoader.DataPoint dp : test) {
                    int pred = (int) neuralNetwork.findMaxVal(dp.inputs());
                    int actual = labelFromOneHot(dp.expectedOutputs());
                    if (pred == actual) correct++;
                }

                double acc = 100.0 * correct / test.length;
                System.out.printf("Epoch %d accuracy: %.2f%%%n", e, acc);
            }

            System.out.println("Training completed!");
            
            // Save the trained model
            System.out.println("Saving model to " + MODEL_FILE + "...");
            try {
                neuralNetwork.save(MODEL_FILE);
                System.out.println("Model saved successfully!");
            } catch (Exception e) {
                System.err.println("Warning: Failed to save model: " + e.getMessage());
                System.err.println("Model will need to be retrained on next startup.");
            }
        }
        
        System.out.println("Starting API server on port " + PORT + "...");

        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/predict", new PredictHandler());
        server.createContext("/health", new HealthHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("API server is running at http://localhost:" + PORT);
        System.out.println("Endpoints:");
        System.out.println("  POST /predict - Predict digit from pixel array");
        System.out.println("  GET  /health  - Health check");
    }

    static class PredictHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Enable CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed. Use POST.");
                return;
            }

            try {
                // Read request body
                InputStream requestBody = exchange.getRequestBody();
                String requestText = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                
                // Parse JSON
                JsonObject jsonRequest = gson.fromJson(requestText, JsonObject.class);
                if (!jsonRequest.has("pixels")) {
                    sendError(exchange, 400, "Missing 'pixels' field in request body");
                    return;
                }

                double[] pixels = gson.fromJson(jsonRequest.get("pixels"), double[].class);
                
                if (pixels.length != 784) {
                    sendError(exchange, 400, "Pixel array must contain exactly 784 values (28x28 image)");
                    return;
                }

                // Normalize pixels to ensure they're in 0-1 range (they should already be)
                for (int i = 0; i < pixels.length; i++) {
                    pixels[i] = Math.max(0.0, Math.min(1.0, pixels[i]));
                }

                // Get prediction
                double[] output = neuralNetwork.clacFinalOutput(pixels);
                int prediction = (int) neuralNetwork.findMaxVal(pixels);
                
                // Calculate confidence as the maximum output value (normalized to 0-100)
                double maxOutput = output[0];
                for (double val : output) {
                    maxOutput = Math.max(maxOutput, val);
                }
                double confidence = maxOutput * 100.0;

                // Create response
                JsonObject response = new JsonObject();
                response.addProperty("prediction", prediction);
                response.addProperty("confidence", Math.round(confidence * 100.0) / 100.0);

                String responseText = gson.toJson(response);
                byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();

            } catch (Exception e) {
                System.err.println("Error processing prediction: " + e.getMessage());
                e.printStackTrace();
                sendError(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            
            JsonObject response = new JsonObject();
            response.addProperty("status", "healthy");
            response.addProperty("model", "loaded");
            
            String responseText = gson.toJson(response);
            byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
            
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        String errorText = gson.toJson(error);
        byte[] errorBytes = errorText.getBytes(StandardCharsets.UTF_8);
        
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, errorBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(errorBytes);
        os.close();
    }

    // Helper methods from Main.java
    private static int labelFromOneHot(double[] oneHot) {
        return argMax(oneHot);
    }

    private static int argMax(double[] a) {
        int best = 0;
        for (int i = 1; i < a.length; i++) {
            if (a[i] > a[best]) best = i;
        }
        return best;
    }

    private static void shuffle(MnistLoader.DataPoint[] arr, Random rng) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            MnistLoader.DataPoint tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    // Helper method to get resource path (works both from JAR and from filesystem)
    private static String getResourcePath(String resourcePath) throws IOException {
        // First try to load from resources (works when running from JAR)
        InputStream is = APIServer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) {
            is.close();
            // When running from JAR, we need to extract to temp file
            return extractResourceToTempFile(resourcePath);
        }
        // Fallback to filesystem path (for development)
        File file = new File(resourcePath);
        if (file.exists()) {
            return resourcePath;
        }
        // Try alternative path
        File altFile = new File("src/main/resources/" + resourcePath);
        if (altFile.exists()) {
            return altFile.getAbsolutePath();
        }
        throw new FileNotFoundException("Could not find resource: " + resourcePath);
    }

    private static String extractResourceToTempFile(String resourcePath) throws IOException {
        InputStream is = APIServer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }
        
        File tempFile = File.createTempFile("mnist_" + new File(resourcePath).getName(), ".tmp");
        tempFile.deleteOnExit();
        
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } finally {
            is.close();
        }
        
        return tempFile.getAbsolutePath();
    }
}

