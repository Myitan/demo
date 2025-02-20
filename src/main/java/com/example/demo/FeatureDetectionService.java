package com.example.demo;

import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeatureDetectionService {

    public Mat detectFeatures(Mat image, String detectorType) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        MatOfKeyPoint keyPoints = new MatOfKeyPoint();

        switch(detectorType.toLowerCase()) {
            case "fast":
                FastFeatureDetector fast = FastFeatureDetector.create();
                fast.detect(grayImage, keyPoints);
                break;

            case "harris":
                // Harris corners using corner detection
                Mat dst = new Mat();
                Imgproc.cornerHarris(grayImage, dst, 2, 3, 0.04);
                Mat dstNorm = new Mat();
                Core.normalize(dst, dstNorm, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1);
                this.convertHarrisToKeyPoints(dstNorm, keyPoints);
                break;

            case "kaze":
                KAZE kaze = KAZE.create();
                kaze.detect(grayImage, keyPoints);
                break;

            case "mser":
                MSER mser = MSER.create();
                mser.detect(grayImage, keyPoints);
                break;

            default:
                throw new IllegalArgumentException("Unsupported detector type: " + detectorType);
        }

        Mat outputImage = new Mat();
        Features2d.drawKeypoints(image, keyPoints, outputImage);
        return outputImage;
    }

    private void convertHarrisToKeyPoints(Mat harrisOutput, MatOfKeyPoint keyPoints) {
        keyPoints.fromList(new ArrayList<>());
        float[] harrisData = new float[(int) harrisOutput.total()];
        harrisOutput.get(0, 0, harrisData);

        List<KeyPoint> points = new ArrayList<>();
        for (int y = 0; y < harrisOutput.rows(); y++) {
            for (int x = 0; x < harrisOutput.cols(); x++) {
                if (harrisData[y * harrisOutput.cols() + x] > 100) {
                    points.add(new KeyPoint(x, y, 3));
                }
            }
        }
        keyPoints.fromList(points);
    }
}