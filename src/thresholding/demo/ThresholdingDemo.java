package thresholding.demo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ThresholdingDemo {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat applyThreshold(Mat inputImage, int threshold) {
        Mat outputImage = new Mat(inputImage.rows(), inputImage.cols(), inputImage.type());
        for (int r = 0; r < inputImage.rows(); r++) {
            for (int c = 0; c < inputImage.cols(); c++) {
                double intensity[] = inputImage.get(r, c);
                if (intensity[0] <= threshold) {
                    outputImage.put(r, c, 0);
                } else {
                    outputImage.put(r, c, 255);
                }
            }
        }
        return outputImage;
    }

    public static long[] getHistogram(Mat inputImage) {
        long histogram[] = new long[256];
        for (int r = 0; r < inputImage.rows(); r++)
            for (int c = 0; c < inputImage.cols(); c++) {
                double intensity[] = inputImage.get(r, c);
                int value = (int) intensity[0];
                histogram[value]++;
            }
        return histogram;
    }
    
    // improved Basic Global Thresholding using Histograms
    public static int furtherImprovedBGT(Mat inputImage) {
        long histogram[] = getHistogram(inputImage);
        double threshold = 127;
        double newThreshold = threshold;
        int iteration = 0;

        long cf[] = new long[histogram.length];
        long cwf[] = new long[histogram.length];
        
        // run a loop to fill out the entries of cf and cwf
        cf[0] = histogram[0];
        cwf[0] = histogram[0] * 0;
        
        for (int i = 1; i < histogram.length; i++) {
            cf[i] = cf[i - 1] + histogram[i];
            cwf[i] = cwf[i - 1] + histogram[i] * i;
        }
        
        do {
            System.out.printf("Iteration %d Threshold %.3f\n", iteration, threshold);
            iteration++;
            threshold = newThreshold;
            double g1sum = cwf[(int) threshold];
            long g1count = cf[(int) threshold];
            double g2sum = cwf[histogram.length - 1] - g1sum;
            long g2count = cf[histogram.length - 1] - g1count;

            double m1 = g1sum / g1count;
            double m2 = g2sum / g2count;

            newThreshold = (m1 + m2) / 2;
        } while (Math.abs(threshold - newThreshold) > 1);
        
        System.out.println("Number of iterations: " + iteration);
        return (int) newThreshold;
    }
    
    // improved Basic Global Thresholding using Histograms
    public static int improvedBGT(Mat inputImage) {
        long histogram[] = getHistogram(inputImage);
        double threshold = 127;
        double newThreshold = threshold;
        int iteration = 0;

        do {
            iteration++;
            threshold = newThreshold;
            double g1sum = 0;
            int g1count = 0;
            double g2sum = 0;
            int g2count = 0;

            // try next: using cumulative sums
            for (int i = 0; i < histogram.length; i++) {
                if (i <= threshold) {
                    g1count += histogram[i];
                    g1sum += i * histogram[i];
                } else {
                    g2count += histogram[i];
                    g2sum += i * histogram[i];
                }
            }

            double m1 = g1sum / g1count;
            double m2 = g2sum / g2count;

            newThreshold = (m1 + m2) / 2;
        } while (Math.abs(threshold - newThreshold) > 1);
        
        System.out.println("Number of iterations: " + iteration);
        return (int) newThreshold;
    }
    
    // DIP 3rd Edition page 742
    public static int basicGlobalThresholding(Mat inputImage) {
        // step 1
        // initial guess for threshold, T
        double threshold = 127;
        double newThreshold = threshold;
        int iteration = 0;
        
        do {
            iteration++;
            threshold = newThreshold;
            double g1sum = 0;
            int g1count = 0;
            double g2sum = 0;
            int g2count = 0;

            for (int r = 0; r < inputImage.rows(); r++) {
                for (int c = 0; c < inputImage.cols(); c++) {
                    double intensity[] = inputImage.get(r, c);
                    if (intensity[0] <= threshold) {
                        g1sum += intensity[0];
                        g1count++;
                    } else {
                        g2sum += intensity[0];
                        g2count++;
                    }
                }
            }

            double m1 = g1sum / g1count;
            double m2 = g2sum / g2count;

            newThreshold = (m1 + m2) / 2;
        } while (Math.abs(threshold - newThreshold) > 1);
        
        System.out.println("Number of iterations: " + iteration);
        return (int) newThreshold;
    }

    public static void main(String[] args) {
        Mat inputImage = Imgcodecs.imread("meat.jpg", Imgcodecs.IMREAD_GRAYSCALE);
        
        // epoch - January 1, 1970
        long startTime = System.currentTimeMillis();
        //int threshold = basicGlobalThresholding(inputImage);
        int threshold = improvedBGT(inputImage);
//        int threshold = furtherImprovedBGT(inputImage);
        long stopTime = System.currentTimeMillis();
        System.out.printf("Time taken for basic global thresholding: %.2f\n", (stopTime - startTime) / 1000.0);
        System.out.println("Threshold " + threshold);
        Mat outputImage = applyThreshold(inputImage, threshold);
        Imgcodecs.imwrite("output.png", outputImage);
    }

}
