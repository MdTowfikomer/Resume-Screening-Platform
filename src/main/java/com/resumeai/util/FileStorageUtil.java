package com.resumeai.util;

import com.resumeai.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class FileStorageUtil {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Value("${app.resume.storage-path:./uploads/resumes}")
    private String storagePath;

    public String storeFile(MultipartFile file) throws IOException {
        validateFile(file);

        Path uploadDir = Paths.get(storagePath);
        Files.createDirectories(uploadDir);

        String extension = getExtension(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "." + extension;
        Path destination = uploadDir.resolve(storedFilename);

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        log.info("File stored: {}", destination.toAbsolutePath());
        return storedFilename;
    }

    public InputStream getFileInputStream(String storedFilename) throws IOException {
        Path filePath = Paths.get(storagePath).resolve(storedFilename);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Resume file not found: " + storedFilename);
        }
        return Files.newInputStream(filePath);
    }

    public void deleteFile(String storedFilename) {
        try {
            Path filePath = Paths.get(storagePath).resolve(storedFilename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", storedFilename);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File must not be empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException(
                    "Unsupported file type: " + file.getContentType()
                            + ". Allowed: PDF, DOC, DOCX");
        }
        if (file.getSize() > 10 * 1024 * 1024L) {
            throw new InvalidFileException("File size exceeds maximum allowed 10MB");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
