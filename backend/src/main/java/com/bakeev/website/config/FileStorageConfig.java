package com.bakeev.website.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileStorageConfig {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.photo-dir}")
    private String photoDir;

    @PostConstruct
    public void init() {
        try {
            Path pathUpload = Path.of(uploadDir);
            if (!Files.exists(pathUpload)) {
                Files.createDirectories(pathUpload);
            }
            Path pathPhoto = Path.of(photoDir);
            if (!Files.exists(pathPhoto)) {
                Files.createDirectories(pathPhoto);
            }

        } catch (Exception e) {
            System.out.println("Error creating dir" + e.getMessage());
        }
    }

}
