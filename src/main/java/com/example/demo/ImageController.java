package com.example.demo;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@SessionAttributes({"originalImage", "processedImage"})
public class ImageController {

    private final FileStorageService fileStorageService;
    private final ImageProcessingService imageProcessingService;
    private final FeatureDetectionService featureDetectionService;
    private final String uploadDir;

    public ImageController(
            FileStorageService fileStorageService,
            ImageProcessingService imageProcessingService,
            FeatureDetectionService featureDetectionService,
            @Value("${upload.dir}") String uploadDir
    ) {
        this.fileStorageService = fileStorageService;
        this.imageProcessingService = imageProcessingService;
        this.featureDetectionService = featureDetectionService;
        this.uploadDir = uploadDir;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleUpload(@RequestParam("file") MultipartFile file) {
        try {
            String filename = fileStorageService.storeImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("originalImage", filename);
            response.put("processedImage", filename);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload-second")
    public ResponseEntity<?> handleSecondUpload(@RequestParam("file") MultipartFile file) {
        try {
            String filename = fileStorageService.storeImage(file);
            return ResponseEntity.ok(Map.of("secondImage", filename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/adjust-brightness")
    public ResponseEntity<?> adjustBrightness(@RequestBody Map<String, Object> body) {
        try {
            float value = Float.parseFloat(body.get("value").toString());
            String currentImage = body.get("currentImage").toString();

            Mat image = ImageUtils.readImage(uploadDir + currentImage);
            Mat processedImage = imageProcessingService.adjustBrightness(image, value);

            String processedFilename = ImageUtils.saveProcessedImage(
                    processedImage,
                    "brightness",
                    uploadDir
            );

            return ResponseEntity.ok(Map.of("processedImage", processedFilename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/grb2gray")
    public ResponseEntity<?> convertToGrayscale(@RequestBody Map<String,Object> body){
        try {
            String currentImage = body.get("currentImage").toString();
            Mat image = ImageUtils.readImage(uploadDir + currentImage);
            Mat processedImage = imageProcessingService.convertToGrayscale(image);

            String processedFilename = ImageUtils.saveProcessedImage(
                    processedImage,
                    "grey",
                    uploadDir
            );

            return ResponseEntity.ok(Map.of("processedImage",processedFilename));
        } catch ( Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/prewitt")
    public ResponseEntity<?> applyPrewittEdgeDetection(@RequestBody Map<String,Object> body){
        try {
            String currentImage = body.get("currentImage").toString();
            Mat image = ImageUtils.readImage(uploadDir + currentImage);
            Mat processedImage = imageProcessingService.applyPrewitt(image);

            String processedFilename = ImageUtils.saveProcessedImage(
                    processedImage,
                    "prewitt",
                    uploadDir);

            return ResponseEntity.ok(Map.of("processedImage",processedFilename));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/adjust-channel")
    public ResponseEntity<?> adjustColorChannel(@RequestBody Map<String, Object> body) {
        try {
            String channel = body.get("channel").toString();
            float value = Float.parseFloat(body.get("value").toString());
            String currentImage = body.get("currentImage").toString();

            Mat image = ImageUtils.readImage(uploadDir + currentImage);
            Mat processedImage = imageProcessingService.adjustColorChannel(image, channel, value);

            String filename = ImageUtils.saveProcessedImage(
                    processedImage,
                    channel,
                    uploadDir);

            return ResponseEntity.ok(Map.of("processedImage", filename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/apply-filter")
    public ResponseEntity<?> applyFilter(@RequestBody Map<String, Object> body) {
        try {
            String filterType = body.get("filterType").toString();
            String currentImage = body.get("currentImage").toString();

            Mat image = ImageUtils.readImage(uploadDir + currentImage);
            Mat processedImage = imageProcessingService.applyFilter(image, filterType);

            String filename = ImageUtils.saveProcessedImage(
                    processedImage,
                    "filter_" + filterType,
                    uploadDir
            );

            return ResponseEntity.ok(Map.of("processedImage", filename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/detect-features")
    public ResponseEntity<?> detectFeatures(@RequestBody Map<String, Object> body) {
        try {
            String detectorType = body.get("detectorType").toString();
            String currentImage = body.get("currentImage").toString();

            Mat image = ImageUtils.readImage(uploadDir + currentImage);
            Mat processedImage = featureDetectionService.detectFeatures(image, detectorType);

            String filename = ImageUtils.saveProcessedImage(
                    processedImage,
                    "features_" + detectorType,
                    uploadDir
            );

            return ResponseEntity.ok(Map.of("processedImage", filename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/image-fusion")
    public ResponseEntity<?> blendImages(@RequestBody Map<String, Object> body) {
        try {
            String currentImage = body.get("currentImage").toString();
            String secondImage = body.get("secondImage").toString();
            float blendValue = Float.parseFloat(body.get("blendValue").toString());

            Mat image1 = ImageUtils.readImage(uploadDir + currentImage);
            Mat image2 = ImageUtils.readImage(uploadDir + secondImage);
            Imgproc.resize(image2, image2, new Size(image1.cols(), image1.rows()));
            Mat blendedImage = imageProcessingService.blendImages(image1, image2, blendValue);

            String processedFilename = ImageUtils.saveProcessedImage(
                    blendedImage,
                    "fused",
                    uploadDir);

            return ResponseEntity.ok(Map.of("processedImage", processedFilename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> downloadProcessedImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir + filename);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}