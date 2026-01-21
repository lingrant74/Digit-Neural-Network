package nn;



import java.util.Random;

public class Main {

    public static void main(String[] args) throws Exception {

        String trainImages = "data/train-images-idx3-ubyte";

        String trainLabels = "data/train-labels-idx1-ubyte";

        String testImages  = "data/t10k-images-idx3-ubyte";

        String testLabels  = "data/t10k-labels-idx1-ubyte";

        // Load small training subset for speed; increase later (e.g., 60000)

        MnistLoader.DataPoint[] train = MnistLoader.load(trainImages, trainLabels, 50000);

        MnistLoader.DataPoint[] test  = MnistLoader.load(testImages, testLabels, 0); // 0 = load all in your loader

        // Sanity check

        System.out.println("First training image:");

        MnistLoader.printAsciiDigit(train[0].inputs(), 28, 28);

        System.out.println("Label should be: " + labelFromOneHot(train[0].expectedOutputs()));

        // Build NN

        int[] sizes = {784, 64, 10};

        NeuralNetwork nn = new NeuralNetwork(sizes);

        // Train settings

        int epochs = 30;

        double lr = 0.005;

        Random rng = new Random(42);

        // ---- SGD batch of size 1 (FIXED) ----

        NeuralNetwork.DataPoint[] oneSampleBatch = new NeuralNetwork.DataPoint[1];

        for (int e = 1; e <= epochs; e++) {

            shuffle(train, rng);

            // ---- TRAIN (true SGD: one sample at a time) ----

            for (MnistLoader.DataPoint dp : train) {

                oneSampleBatch[0] = new NeuralNetwork.DataPoint(dp.inputs(), dp.expectedOutputs());

                nn.learn(oneSampleBatch, lr);

            }

            // ---- EVAL ----

            int correct = 0;

            for (MnistLoader.DataPoint dp : test) {

                // Option A: use your helper that returns the predicted index

                int pred = (int) nn.findMaxVal(dp.inputs());

                // Option B (equivalent): int pred = argMax(nn.clacFinalOutput(dp.inputs()));

                int actual = labelFromOneHot(dp.expectedOutputs());

                if (pred == actual) correct++;

            }

            double acc = 100.0 * correct / test.length;

            System.out.printf("Epoch %d accuracy: %.2f%%%n", e, acc);

        }

    }

    // ---------- helpers ----------

    static int argMax(double[] a) {

        int best = 0;

        for (int i = 1; i < a.length; i++) {

            if (a[i] > a[best]) best = i;

        }

        return best;

    }

    static int labelFromOneHot(double[] oneHot) {

        return argMax(oneHot);

    }

    static void shuffle(MnistLoader.DataPoint[] arr, Random rng) {

        for (int i = arr.length - 1; i > 0; i--) {

            int j = rng.nextInt(i + 1);

            MnistLoader.DataPoint tmp = arr[i];

            arr[i] = arr[j];

            arr[j] = tmp;

        }

    }

}
