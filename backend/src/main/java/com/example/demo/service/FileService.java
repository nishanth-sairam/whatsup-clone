package com.example.demo.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
@NoArgsConstructor
public class FileService {

    @Value("${application.file.upload.media-output.path}")
    private String fileUploadPath;

    public String saveFile(@NonNull MultipartFile file, @NonNull UUID senderId) throws IOException {

        final String fileUploadSubPath = String.format("user/%s", senderId);
        return uploadFile(file, fileUploadSubPath);
    }

    private String uploadFile(@NonNull MultipartFile file, @NonNull String fileUploadSubPath) throws IOException {
        final String finalUploadFilePath = String.format("%s/%s", fileUploadPath, fileUploadSubPath);
        File targetFolder = new File(finalUploadFilePath);
        if (!targetFolder.exists()) {
            boolean isCreated = targetFolder.mkdirs();
            if (!isCreated) {
                log.error("Failed to create directory: {}", finalUploadFilePath);
                return null;
            }
        }
        final String fileExtension = getFileExtension(file.getOriginalFilename());
        String targetFilePath = String.format("%s/%s.%s", finalUploadFilePath, UUID.randomUUID(), fileExtension);
        Path targetPath = Path.of(targetFilePath);
        try {
            Files.write(targetPath, file.getBytes());
            log.info("File saved successfully at: {}", targetFilePath);
            return targetFilePath;

        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage());
        }
        return null;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return ""; // No extension found
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();

    }
}
