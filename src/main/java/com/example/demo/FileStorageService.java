package com.example.demo;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private final String uploadDir;

    public FileStorageService(@Value("${upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String storeImage(MultipartFile file) throws IOException, FileValidationException {
        validateFile(file);
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath);
        validateImageContent(filePath);

        return filename;
    }

    private void validateFile(MultipartFile file) throws FileValidationException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileValidationException("File size exceeds 10MB limit");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new FileValidationException("Invalid file name");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileValidationException("Only JPG, JPEG, PNG files are allowed");
        }
    }

    private void validateImageContent(Path filePath) throws IOException, FileValidationException {
        Mat image = Imgcodecs.imread(filePath.toString());
        if (image == null || image.empty()) {
            Files.deleteIfExists(filePath);
            throw new FileValidationException("Invalid image file");
        }
        image.release();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // Runs every 30 minutes
    public void autoDelete() {
        File dir = new File(uploadDir);
        long currTime = System.currentTimeMillis();
        long threshold = currTime - TimeUnit.MINUTES.toMillis(15);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.lastModified() < threshold) {
                        if (file.delete()) {
                            System.out.println("Deleted file: " + file.getName());
                        } else {
                            System.out.println("Failed to delete file: " + file.getName());
                        }
                    }
                }
            }
        } else {
            System.out.println("Directory not found or is not a valid directory: " + uploadDir);
        }
    }
}