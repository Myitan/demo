package com.example.demo;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageUtils {

    public static Mat readImage(String filePath) {
        Path path = Paths.get(filePath);
        if (!path.toFile().exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        Mat image = Imgcodecs.imread(filePath);
        if (image.empty()) {
            throw new IllegalArgumentException("Failed to load image: " + filePath);
        }

        switch (image.channels()) {
            case 1 -> Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2BGR);
            case 4 -> Imgproc.cvtColor(image, image, Imgproc.COLOR_BGRA2BGR);
        }
        return image;
    }

    public static String saveProcessedImage(Mat image, String prefix, String uploadDir) {
        if (!uploadDir.endsWith("/")) uploadDir += "/";
        String filename = prefix + "_" + System.currentTimeMillis() + ".jpg";
        Imgcodecs.imwrite(uploadDir + filename, image);
        return filename;
    }
}