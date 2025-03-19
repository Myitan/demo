package com.example.demo;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageProcessingService {

    public Mat adjustBrightness(Mat original, float value) {
        validateColorSpace(original, 3);
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(original, hsvImage, Imgproc.COLOR_BGR2HSV);

        List<Mat> channels = new ArrayList<>();
        Core.split(hsvImage, channels);
        channels.get(2).convertTo(channels.get(2), -1, value, 0);
        Core.merge(channels, hsvImage);

        Mat result = new Mat();
        Imgproc.cvtColor(hsvImage, result, Imgproc.COLOR_HSV2BGR);
        return result;
    }

    private void validateColorSpace(Mat image, int expectedChannels) {
        if (image.channels() != expectedChannels) {
            throw new IllegalArgumentException(
                    "Invalid color space. Expected " + expectedChannels + " channels"
            );
        }
    }

    public Mat convertToGrayscale(Mat image) {
        Mat nextImgage = new Mat();
        Imgproc.cvtColor(image,nextImgage, Imgproc.COLOR_BGR2GRAY);
        return nextImgage;
    }

    public Mat applyPrewitt(Mat image) {
        validateColorSpace(image, 3);

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat gradX = new Mat();
        Mat gradY = new Mat();

        Mat kernelX = new Mat(3, 3, CvType.CV_32F) {{
            put(0, 0, -1, 0, 1);
            put(1, 0, -1, 0, 1);
            put(2, 0, -1, 0, 1);
        }};
        Mat kernelY = new Mat(3, 3, CvType.CV_32F) {{
            put(0, 0, -1, -1, -1);
            put(0, 1, 0, 0, 0);
            put(0, 2, 1, 1, 1);
        }};

        Imgproc.filter2D(gray, gradX, CvType.CV_32F, kernelX);
        Imgproc.filter2D(gray, gradY, CvType.CV_32F, kernelY);

        Core.convertScaleAbs(gradX, gradX);
        Core.convertScaleAbs(gradY, gradY);

        Mat result = new Mat();
        Core.addWeighted(gradX, 0.5, gradY, 0.5, 0, result);

        return result;
    }
    public Mat adjustColorChannel(Mat image, String channel, float value) {
        List<Mat> channels = new ArrayList<>();
        Core.split(image, channels);

        switch(channel.toLowerCase()) {
            case "red":
                channels.get(2).convertTo(channels.get(2), -1, 1, value);
                break;
            case "green":
                channels.get(1).convertTo(channels.get(1), -1, 1, value);
                break;
            case "blue":
                channels.get(0).convertTo(channels.get(0), -1, 1, value);
                break;
        }

        Mat result = new Mat();
        Core.merge(channels, result);
        return result;
    }

    public Mat applyFilter(Mat image, String filterType) {
        validateColorSpace(image, 3);

        switch(filterType.toLowerCase()) {
            case "average":
                Imgproc.blur(image, image, new Size(6, 6));
                break;

            case "disk":
                // Create disk-shaped kernel
                Mat kernel = Imgproc.getStructuringElement(
                        Imgproc.MORPH_ELLIPSE,
                        new Size(15, 15)
                );
                Imgproc.filter2D(image, image, -1, kernel);
                break;

            case "laplacian":
                Mat laplacian = new Mat();
                Imgproc.Laplacian(image, laplacian, CvType.CV_32F, 3);
                laplacian.convertTo(image, CvType.CV_8U);
                break;

            case "sobel":
                Mat gradX = new Mat();
                Imgproc.Sobel(image, gradX, CvType.CV_32F, 1, 0);
                Core.convertScaleAbs(gradX, image);
                break;

            case "log":
                Imgproc.GaussianBlur(image, image, new Size(5, 5), 0.9);
                Imgproc.Laplacian(image, image, CvType.CV_32F, 3);
                image.convertTo(image, CvType.CV_8U);
                break;

            default:
                throw new IllegalArgumentException("Unknown filter type: " + filterType);
        }
        return image;
    }

    public Mat blendImages(Mat image1, Mat image2, float blendValue) {
        Mat blended = new Mat();
        Core.addWeighted(image1, 1 - blendValue, image2, blendValue, 0, blended);
        return blended;
    }
}