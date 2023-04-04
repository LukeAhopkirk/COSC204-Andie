package cosc202.andie;

import java.awt.image.*;

/**
 * <p>
 * ImageOperation to apply a Gaussian Blur filter.
 * </p>
 * 
 * <p>
 * Type of nonlinear filter that replaces the value of each pixel in 
 * an image with the median value of the pixels in its surrounding neighborhood.
 * 
*/
public class GaussianB implements ImageOperation, java.io.Serializable {

    private int kernelSize;
    private float sigma;

    /**
     * <p>
     * Construct a Mean filter with the given size.
     * </p>
     * 
     * <p>
     * A larger kernel size will result in a more intense blur effect, 
     * and a larger sigma value will result in a wider and smoother blur.
     * </p>
     * 
     * @param kernelSize Size of the square matrix used to perform convolution on the image.
     * @param sigma Amount of smoothing or blurring applied to an image.
     */
    public GaussianB(int kernelSize, float sigma) {
        if (kernelSize < 0) {
            throw new IllegalArgumentException("Kernel Size cannot be negative.");
        }

        this.kernelSize = kernelSize;
        this.sigma = sigma;
    }

    /**
     * <p>
     * Apply a Gaussian Blur filter to an image.
     * </p>
     * The kernel is created by iterating over each pixel in the kernel matrix and calculating its value 
     * using the Gaussian function. The sum of all the pixel values is then normalized to 1.0 to ensure 
     * that the resulting image does not become brighter or darker.
     * <p>
     * </p>
     * 
     * @param input The image to apply the Gaussian Blur filter to.
     * @return The resulting (blurred) image.
     */
    public BufferedImage apply(BufferedImage input) {
        // Create a Gaussian kernel
        int k = (kernelSize - 1) / 2;
        float[] kernelData = new float[kernelSize * kernelSize];
        float sum = 0;
        for (int y = -k; y <= k; y++) {
            for (int x = -k; x <= k; x++) {
                float value = (float) (1.0 / (2.0 * Math.PI * sigma * sigma)
                        * Math.exp(-(x * x + y * y) / (2.0 * sigma * sigma)));
                kernelData[(y + k) * kernelSize + (x + k)] = value;
                sum += value;
            }
        }
        for (int i = 0; i < kernelData.length; i++) {
            kernelData[i] /= sum;
        }
        Kernel kernel = new Kernel(kernelSize, kernelSize, kernelData);

        // Create a ConvolveOp with the Gaussian kernel
        ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        // Apply the ConvolveOp to the image
        BufferedImage output = convolve.filter(input, null);

        return output;
    }
}
