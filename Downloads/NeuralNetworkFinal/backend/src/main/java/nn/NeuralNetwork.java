package nn;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NeuralNetwork {

    Layer[] layers;

    public NeuralNetwork(int[] layerSizes) {
        layers = new Layer[layerSizes.length - 1];

        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(layerSizes[i], layerSizes[i + 1]);
        }
    }

    // Constructor for loading saved model
    private NeuralNetwork(Layer[] layers) {
        this.layers = layers;
    }

    //fill all of the neutrons
    public double[] clacFinalOutput(double[] inputs) {
        double[] ans = inputs;
        for (Layer ele : layers) {
            ans = ele.calcOutput(ans);
        }
        return ans;
    }

    //gives the index of the final output
    public double findMaxVal(double[] inputs) {
        double[] ans = clacFinalOutput(inputs);
        double max = ans[0];
        for (double ele : ans) {
            max = Math.max(max, ele);
        }
        int count = 0;
        for (double ele : ans) {
            if (ele == max) {
                return count;
            }
            count++;
        }
        return 0;
    }

    public record DataPoint(double[] inputs, double[] expectedOutputs) {

    }

    public double Cost(DataPoint datapoint) {
        double[] output = clacFinalOutput(datapoint.inputs());
        double errorCost = 0;

        Layer outputLayer = layers[layers.length - 1];
        for (int i = 0; i < outputLayer.outputs.length; i++) {
            errorCost += outputLayer.errorCost(output[i], datapoint.expectedOutputs()[i]);
        }
        return errorCost;
    }

    //finds average cost
    public double avgCost(DataPoint[] datapoints) {
        double cost = 0;
        for (DataPoint ele : datapoints) {
            cost += Cost(ele);
        }
        return cost / datapoints.length;
    }

    //backpropagation
    public void updateAllGradient(DataPoint datapoints) {
        clacFinalOutput(datapoints.inputs());
        // [layers.length-1] would be the output layer
        Layer outputLayer = layers[layers.length - 1];
        double[] nodeValues = outputLayer.calcOutputNodeValues(datapoints.expectedOutputs());
        outputLayer.updateGradient(nodeValues);

        //looping through all the hidden layers
        for (int i = layers.length - 2; i >= 0; i--) {
            Layer hiddenLayer = layers[i];
            nodeValues = hiddenLayer.calcHiddenLayerNodeValues(layers[i + 1], nodeValues);
            hiddenLayer.updateGradient(nodeValues);
        }
    }

    public void applyAllGradients(double learnRate) {
        for (Layer layer : layers) {
            layer.applyGradients(learnRate);
        }
    }

    public void clearAllGradients() {
        for (Layer layer : layers) {
            layer.clearGradient();
        }
    }

    public void learn(DataPoint[] traingingBatch, double learningRate) {
        for (DataPoint ele : traingingBatch) {
            updateAllGradient(ele);
        }

        applyAllGradients(learningRate/ traingingBatch.length);

        clearAllGradients();
    }

    /**
     * Save the neural network model to a JSON file
     */
    public void save(String filePath) throws IOException {
        Gson gson = new Gson();
        JsonObject modelJson = new JsonObject();
        
        // Save layer structure
        JsonArray layersJson = new JsonArray();
        for (Layer layer : layers) {
            JsonObject layerJson = new JsonObject();
            layerJson.addProperty("numInput", layer.getNumInput());
            layerJson.addProperty("numOutput", layer.getNumOutput());
            
            // Save weights as 2D array
            JsonArray weightsJson = new JsonArray();
            double[][] weights = layer.getWeight();
            for (double[] row : weights) {
                JsonArray rowJson = new JsonArray();
                for (double val : row) {
                    rowJson.add(val);
                }
                weightsJson.add(rowJson);
            }
            layerJson.add("weights", weightsJson);
            
            // Save biases as array
            JsonArray biasesJson = new JsonArray();
            for (double bias : layer.getBias()) {
                biasesJson.add(bias);
            }
            layerJson.add("biases", biasesJson);
            
            layersJson.add(layerJson);
        }
        
        modelJson.add("layers", layersJson);
        
        // Write to file
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(modelJson, writer);
        }
    }

    /**
     * Load a neural network model from a JSON file
     */
    public static NeuralNetwork load(String filePath) throws IOException {
        Gson gson = new Gson();
        
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject modelJson = gson.fromJson(reader, JsonObject.class);
            JsonArray layersJson = modelJson.getAsJsonArray("layers");
            
            Layer[] loadedLayers = new Layer[layersJson.size()];
            
            for (int i = 0; i < layersJson.size(); i++) {
                JsonObject layerJson = layersJson.get(i).getAsJsonObject();
                int numInput = layerJson.get("numInput").getAsInt();
                int numOutput = layerJson.get("numOutput").getAsInt();
                
                // Load weights
                JsonArray weightsJson = layerJson.getAsJsonArray("weights");
                double[][] weights = new double[numInput][numOutput];
                for (int j = 0; j < weightsJson.size(); j++) {
                    JsonArray rowJson = weightsJson.get(j).getAsJsonArray();
                    for (int k = 0; k < rowJson.size(); k++) {
                        weights[j][k] = rowJson.get(k).getAsDouble();
                    }
                }
                
                // Load biases
                JsonArray biasesJson = layerJson.getAsJsonArray("biases");
                double[] biases = new double[numOutput];
                for (int j = 0; j < biasesJson.size(); j++) {
                    biases[j] = biasesJson.get(j).getAsDouble();
                }
                
                loadedLayers[i] = new Layer(numInput, numOutput, weights, biases);
            }
            
            return new NeuralNetwork(loadedLayers);
        }
    }

}
