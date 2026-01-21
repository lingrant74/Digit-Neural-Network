package nn;
import java.util.Random;

public class Layer {
    private static final Random rng = new Random(42);

    int numInput;
    int numOutput;

    double[][] weight;
    double[] bias;

    double[][] weightGradient;
    double[] biasGradient;

    double[] inputs;
    double[] outputs;

    double[] weightedInputs;

    public Layer(int numInput, int numOutput) {
        this.numInput = numInput;
        this.numOutput = numOutput;

        this.weight = new double[numInput][numOutput];
        this.bias = new double[numOutput];

        this.weightGradient = new double[numInput][numOutput];
        this.biasGradient = new double[numOutput];

        this.inputs = new double[numInput];
        this.outputs = new double[numOutput];

        this.weightedInputs = new double[numOutput];

        double scale = Math.sqrt(1.0 / numInput);

        for (int j = 0; j < numInput; j++) {
            for (int i = 0; i < numOutput; i++) {
                weight[j][i] = (rng.nextDouble() * 2.0 - 1.0) * scale;
            }
        }

        for (int i = 0; i < numOutput; i++) {
            bias[i] = 0.0;
        }
    }

    //calculates the values of the output layer
    public double[] calcOutput(double[] input) {
        if (input.length != numInput) {
            throw new IllegalArgumentException(
                    String.format("Size of input must be equal to %d", numInput)
            );
        }

        double[] ans = new double[numOutput];
        for (int i = 0; i < numOutput; i++) {
            double elementOutput = 0;
            for (int j = 0; j < numInput; j++) {
                elementOutput = weight[j][i] * input[j] + elementOutput;
            }
            elementOutput += bias[i];
            weightedInputs[i] = elementOutput;
            ans[i] = activationFunc(elementOutput);
        }
        this.inputs = input;
        this.outputs = ans;
        return ans;
    }

    //Activation function
    public double activationFunc(double weight) {
        return 1.0 / (1.0 + Math.exp(-weight));
    }

    //calculate error
    public double errorCost(double output, double expectedOutput) {
        return (output - expectedOutput) * (output - expectedOutput);
    }

    //backpropogation
    //derivatives
    public double activationFuncDerivative(double weight) {
        double act = activationFunc(weight);
        return act * (1 - act);
    }

    public double nodeCostDerivative(double output, double expectedOutput) {
        return 2 * (output - expectedOutput);
    }

    public double[] calcOutputNodeValues(double[] expectedOutput) {
        double[] ans = new double[expectedOutput.length];
        for (int i = 0; i < expectedOutput.length; i++) {
            double act = activationFuncDerivative(weightedInputs[i]);
            double cost = nodeCostDerivative(outputs[i], expectedOutput[i]);
            ans[i] = act * cost;
        }
        return ans;
    }

    //updadates Gradient
    public void updateGradient(double[] NodeValues) {
        for (int i = 0; i < numOutput; i++) {
            for (int j = 0; j < numInput; j++) {
                weightGradient[j][i] += inputs[j] * NodeValues[i];
            }
            biasGradient[i] += NodeValues[i];
        }
    }

    //calculating hidden layer
    public double[] calcHiddenLayerNodeValues(Layer oldLayer, double[] oldNodeVal) {
        double[] newNodeVal = new double[numOutput];
        for (int newIndex = 0; newIndex < newNodeVal.length; newIndex++) {
            for (int oldIndex = 0; oldIndex < oldNodeVal.length; oldIndex++) {
                newNodeVal[newIndex] += oldLayer.weight[newIndex][oldIndex] * oldNodeVal[oldIndex];
            }
        }
        for (int y = 0; y < newNodeVal.length; y++) {
            newNodeVal[y] *= activationFuncDerivative(weightedInputs[y]);
        }
        return newNodeVal;
    }

    //applying gradietns
    public void applyGradients(double learnRate) {
        for (int i = 0; i < numOutput; i++) {
            bias[i] -= biasGradient[i] * learnRate;
            for (int j = 0; j < numInput; j++) {
                weight[j][i] -= weightGradient[j][i] * learnRate;
            }
        }
    }

    public void clearGradient() {
        for (int i = 0; i < numOutput; i++) {
            biasGradient[i] = 0;
            for (int j = 0; j < numInput; j++) {
                weightGradient[j][i] = 0;
            }
        }
    }

    // Methods for model persistence
    public double[][] getWeight() {
        return weight;
    }

    public double[] getBias() {
        return bias;
    }

    public void setWeight(double[][] weight) {
        this.weight = weight;
    }

    public void setBias(double[] bias) {
        this.bias = bias;
    }

    public int getNumInput() {
        return numInput;
    }

    public int getNumOutput() {
        return numOutput;
    }

    // Constructor for loading saved model
    public Layer(int numInput, int numOutput, double[][] weight, double[] bias) {
        this.numInput = numInput;
        this.numOutput = numOutput;
        this.weight = weight;
        this.bias = bias;

        this.weightGradient = new double[numInput][numOutput];
        this.biasGradient = new double[numOutput];
        this.inputs = new double[numInput];
        this.outputs = new double[numOutput];
        this.weightedInputs = new double[numOutput];
    }
}
