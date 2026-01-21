package nn;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MnistLoader {

    // Your record from NeuralNetwork
    public static record DataPoint(double[] inputs, double[] expectedOutputs) {}

    public static DataPoint[] load(String imagesPath, String labelsPath, int limit) throws IOException {
        try (DataInputStream img = new DataInputStream(new BufferedInputStream(new FileInputStream(imagesPath)));
             DataInputStream lab = new DataInputStream(new BufferedInputStream(new FileInputStream(labelsPath)))) {

            int imgMagic = img.readInt();   // 2051 expected
            int numImages = img.readInt();
            int numRows = img.readInt();    // 28
            int numCols = img.readInt();    // 28

            int labMagic = lab.readInt();   // 2049 expected
            int numLabels = lab.readInt();

            if (imgMagic != 2051) throw new IOException("Bad image magic: " + imgMagic);
            if (labMagic != 2049) throw new IOException("Bad label magic: " + labMagic);
            if (numImages != numLabels) throw new IOException("Images != Labels: " + numImages + " vs " + numLabels);

            int n = (limit <= 0) ? numImages : Math.min(limit, numImages);
            int imageSize = numRows * numCols;

            List<DataPoint> data = new ArrayList<>(n);

            byte[] imageBytes = new byte[imageSize];

            for (int idx = 0; idx < n; idx++) {
                // Read one image
                img.readFully(imageBytes);

                double[] inputs = new double[imageSize];
                for (int i = 0; i < imageSize; i++) {
                    // unsigned byte -> 0..255, then normalize to 0..1
                    int pixel = imageBytes[i] & 0xFF;
                    inputs[i] = pixel / 255.0;
                }

                // Read one label
                int label = lab.readUnsignedByte();

                // One-hot encode label (0..9)
                double[] expected = new double[10];
                expected[label] = 1.0;

                data.add(new DataPoint(inputs, expected));
            }

            return data.toArray(new DataPoint[0]);
        }
    }

    // Quick ASCII visualization to confirm you loaded correctly
    public static void printAsciiDigit(double[] inputs, int rows, int cols) {
        for (int r = 0; r < rows; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                double v = inputs[r * cols + c]; // 0..1
                char ch =
                        v > 0.8 ? '#' :
                        v > 0.5 ? 'O' :
                        v > 0.2 ? '.' : ' ';
                sb.append(ch);
            }
            System.out.println(sb);
        }
    }
}