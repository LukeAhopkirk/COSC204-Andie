package cosc202.andie;

import java.awt.Graphics2D;
import java.awt.image.*;

import javax.swing.JOptionPane;

/**
 * <p>
 * ImageOperation to apply a Sharpen filter.
 * </p>
 * 
 * <p>
 * Type of image filter that enhances edges and fine details in an image.
 * It works by increasing the contrast between neighboring pixels, making edges
 * appear sharper and more defined.
 * </p>
 * 
 */
public class Sharpen implements ImageOperation, java.io.Serializable {

    /**
     * <p>
     * Default constructor
     * </p>
     */
    Sharpen() {
    }

    /**
     * <p>
     * Apply a Sharpen filter to an image.
     * </p>
     * 
     * <p>
     * Increases the contrast between adjacent pixels in the image.
     * The kernel used places a higher weight on the central
     * pixel and its adjacent pixels, which results in these pixels having a
     * greater influence on the output value.
     * </p>
     * 
     * @param input The image to apply the Sharpen filter to.
     * @return The resulting (sharpened) image.
     */
    public BufferedImage apply(BufferedImage input) {
        float[] array = { 0, -1 / 2.0f, 0,
                -1 / 2.0f, 3, -1 / 2.0f,
                0, -1 / 2.0f, 0 };

        // Make a 3x3 kernel
        Kernel kernel = new Kernel(3, 3, array);

        // Asking user if they want to account for negative numbers
        boolean userNegatives;
        int userSelection = JOptionPane.showConfirmDialog(null,
                "Do you wish to account for negative numbers?",
                "Negative Numbers", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (userSelection == JOptionPane.YES_OPTION) {
            userNegatives = true;
        } else {
            userNegatives = false;
        }

        if (userNegatives) {
            int width = input.getWidth();
            int height = input.getHeight();
            BufferedImage paddedInput = new BufferedImage(width + 2, height + 2, input.getType());
            Graphics2D g = paddedInput.createGraphics();
            g.drawImage(input, 1, 1, null);
            g.dispose();

            // Apply the ConvolveOp to the padded image
            BufferedImage output = customConvolution(kernel, paddedInput);

            // Crop the image to its original size
            output = output.getSubimage(1, 1, width, height);

            return output;

        } else {

            ConvolveOp convOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

            // Pad the image with zeros
            int width = input.getWidth();
            int height = input.getHeight();
            BufferedImage paddedInput = new BufferedImage(width + 2, height + 2, input.getType());
            Graphics2D g = paddedInput.createGraphics();
            g.drawImage(input, 1, 1, null);
            g.dispose();

            // Apply the ConvolveOp to the padded image
            BufferedImage output = convOp.filter(paddedInput, null);

            // Crop the image to its original size
            output = output.getSubimage(1, 1, width, height);

            return output;
        }

        // for (int y = 0; y < input.getHeight(); y++) {
        // for (int x = 0; x < input.getWidth(); x++) {
        // int imageVal = input.getRGB(x, y);
        // int red = (imageVal >> 16) & 0xFF;
        // int green = (imageVal >> 8) & 0xFF;
        // int blue = imageVal & 0xFF;
        // System.out.println("R: " + red + " G: " + green + " B: " + blue);
        // }
        // }
    }

    /**
     * <p>
     * Applies image convolution manually
     * </p>
     * 
     * @param kernel Kernel to be applied
     * @param input  BufferedImage to be convoluted
     * @return The resulting (convoluted) image.
     */
    private static BufferedImage customConvolution(Kernel kernel, BufferedImage input) {
        float[] kernelArray = kernel.getKernelData(null);
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage output = new BufferedImage(width, height, input.getType());
        int kw = (int) Math.sqrt(kernelArray.length);
        int kernelOffset = kw / 2;

        // Calculate the mid-value
        int minPixelValue = 255;
        int maxPixelValue = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = input.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int pixelValue = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                if (pixelValue < minPixelValue) {
                    minPixelValue = pixelValue;
                }
                if (pixelValue > maxPixelValue) {
                    maxPixelValue = pixelValue;
                }
            }
        }
        int midValue = (minPixelValue + maxPixelValue) / 2;

        // Loop through each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                float rSum = 0;
                float gSum = 0;
                float bSum = 0;

                // Loop through each kernel element
                for (int j = -kernelOffset; j <= kernelOffset; j++) {
                    for (int i = -kernelOffset; i <= kernelOffset; i++) {

                        // Calculate pixel coordinates
                        int px = x + i;
                        int py = y + j;

                        // Check if pixel is within bounds
                        if (px >= 0 && px < width && py >= 0 && py < height) {
                            // Get RGB values for current pixel
                            int rgb = input.getRGB(px, py);
                            int r = (rgb >> 16) & 0xFF;
                            int g = (rgb >> 8) & 0xFF;
                            int b = rgb & 0xFF;

                            // Get corresponding kernel value
                            float k = kernelArray[(j + kernelOffset) * kw + i + kernelOffset];

                            // Accumulate weighted sum for each channel
                            rSum += r * k;
                            gSum += g * k;
                            bSum += b * k;
                        }
                    }
                }

                // Combine channel sums into single pixel value
                int rOut = (int) Math.max(0, Math.min(255, rSum + midValue));
                int gOut = (int) Math.max(0, Math.min(255, gSum + midValue));
                int bOut = (int) Math.max(0, Math.min(255, bSum + midValue));
                int rgbOut = (rOut << 16) | (gOut << 8) | bOut;

                // Set output pixel value
                output.setRGB(x, y, rgbOut);
            }
        }

        return output;
    }
}