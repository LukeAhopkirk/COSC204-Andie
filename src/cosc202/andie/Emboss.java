package cosc202.andie;

import java.awt.Graphics2D;
import java.awt.image.*;

/**
 * <p>
 * ImageOperation to apply an Emboss filter.
 * </p>
 * 
 * <p>
 * Type of image filter that produces a 3D effect by detecting edges in the
 * image and highlighting them using a lighting effect. The filter calculates
 * the
 * difference between neighboring pixel values and uses this information to
 * create
 * a raised or lowered appearance, giving the illusion of depth to the image.
 * </p>
 * 
 */
public class Emboss implements ImageOperation, java.io.Serializable {

    private boolean[] numbersTrue;

    /**
     * <p>
     * Constructor for emboss class
     * </p>
     * 
     * @param numbersTrue The array of booleans to determine which kernel to use
     */
    Emboss(boolean[] numbersTrue) {
        this.numbersTrue = numbersTrue;
    }

    /**
     * <p>
     * Apply an Emboss filter to an image.
     * </p>
     * 
     * <p>
     * Uses 9 different kernels for different edge directions,
     * one of which are applied to 1 image
     * </p>
     * 
     * @param input The image to apply the Emboss filter to.
     * @return The resulting (Embossed) image.
     */
    public BufferedImage apply(BufferedImage input) {
        float[] array1 = { 0, 0, 0,
                1, 0, -1,
                0, 0, 0 };

        float[] array2 = { 1, 0, 0,
                0, 0, 0,
                0, 0, -1 };

        float[] array3 = { 0, 1, 0,
                0, 0, 0,
                0, -1, 0 };

        float[] array4 = { 0, 0, 1,
                0, 0, 0,
                -1, 0, 0 };

        float[] array5 = { 0, 0, 0,
                -1, 0, 1,
                0, 0, 0 };

        float[] array6 = { -1, 0, 0,
                0, 0, 0,
                0, 0, 1 };

        float[] array7 = { 0, -1, 0,
                0, 0, 0,
                0, 1, 0 };

        float[] array8 = { 0, 0, -1,
                0, 0, 0,
                1, 0, 0 };

        // Make a 3x3 kernels for horizontal and vertical
        Kernel kernel1 = new Kernel(3, 3, array1);
        Kernel kernel2 = new Kernel(3, 3, array2);
        Kernel kernel3 = new Kernel(3, 3, array3);
        Kernel kernel4 = new Kernel(3, 3, array4);
        Kernel kernel5 = new Kernel(3, 3, array5);
        Kernel kernel6 = new Kernel(3, 3, array6);
        Kernel kernel7 = new Kernel(3, 3, array7);
        Kernel kernel8 = new Kernel(3, 3, array8);

        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage paddedInput = new BufferedImage(width + 2, height + 2, input.getType());
        Graphics2D g = paddedInput.createGraphics();
        g.drawImage(input, 1, 1, null);
        g.dispose();

        // Apply the ConvolveOp to the padded image
        BufferedImage output;
        if (numbersTrue[0] == true) {
            output = customConvolution(kernel1, paddedInput);
        }
        if (numbersTrue[1] == true) {
            output = customConvolution(kernel2, paddedInput);
        }
        if (numbersTrue[2] == true) {
            output = customConvolution(kernel3, paddedInput);
        }
        if (numbersTrue[3] == true) {
            output = customConvolution(kernel4, paddedInput);
        }
        if (numbersTrue[4] == true) {
            output = customConvolution(kernel5, paddedInput);
        }
        if (numbersTrue[5] == true) {
            output = customConvolution(kernel6, paddedInput);
        }
        if (numbersTrue[6] == true) {
            output = customConvolution(kernel7, paddedInput);
        } else {
            output = customConvolution(kernel8, paddedInput);
        }

        // Crop the image to its original size
        output = output.getSubimage(1, 1, width, height);

        return output;
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