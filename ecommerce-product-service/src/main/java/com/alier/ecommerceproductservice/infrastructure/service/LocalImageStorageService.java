package com.alier.ecommerceproductservice.infrastructure.service;

import com.alier.ecommerceproductservice.application.service.ImageStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class LocalImageStorageService implements ImageStorageService {

    @Value("${product.image.storage.local.upload-dir:./uploads/images}")
    private String uploadDir;

    @Value("${product.image.storage.local.public-path-segment:images}")
    private String publicPathSegment; // e.g., /images/product-uuid/filename.jpg

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            log.info("Local image storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize local image storage location: {}", uploadDir, e);
            throw new RuntimeException("Could not initialize local image storage location", e);
        }
    }

    @Override
    public String storeImage(UUID productId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IOException("Cannot store file with relative path outside current directory " + originalFilename);
        }

        // Generate a unique filename to prevent overwrites and include product context
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String uniqueFilename = productId.toString() + "_" + UUID.randomUUID().toString() + (fileExtension != null ? "." + fileExtension : "");

        Path productSpecificDir = rootLocation.resolve(productId.toString());
        Files.createDirectories(productSpecificDir); // Ensure product-specific directory exists

        Path destinationFile = productSpecificDir.resolve(uniqueFilename).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(productSpecificDir.toAbsolutePath())) {
            // This is a security check
            throw new IOException("Cannot store file outside designated product directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored image {} for product {}", uniqueFilename, productId);
        }

        // Return a path that can be used to construct a public URL
        return productId.toString() + "/" + uniqueFilename;
    }

    @Override
    public void deleteImage(String imagePath) throws IOException {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            log.warn("Attempted to delete image with null or empty path.");
            return;
        }
        try {
            Path filePath = rootLocation.resolve(imagePath).normalize();
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                // Security check to prevent deleting outside rootLocation
                if (!filePath.toAbsolutePath().startsWith(rootLocation.toAbsolutePath())) {
                    log.error("Attempt to delete file outside storage root: {}", imagePath);
                    throw new SecurityException("Attempt to delete file outside storage root.");
                }
                Files.delete(filePath);
                log.info("Deleted image: {}", imagePath);
            } else {
                log.warn("Image not found or not a regular file, cannot delete: {}", imagePath);
            }
        } catch (NoSuchFileException e) {
            log.warn("Image not found during deletion attempt: {}", imagePath);
            // Optionally re-throw or handle as per requirements (e.g., if deletion must succeed)
        } catch (DirectoryNotEmptyException e) {
            log.error("Attempted to delete a directory as if it were an image file: {}", imagePath, e);
            throw new IOException("Cannot delete directory: " + imagePath, e);
        } catch (IOException e) {
            log.error("Error deleting image {}: {}", imagePath, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public byte[] retrieveImageBytes(String imagePath) throws IOException {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            throw new IOException("Image path cannot be null or empty.");
        }
        Path filePath = this.rootLocation.resolve(imagePath).normalize();
        // Security check to prevent accessing outside rootLocation
        if (!filePath.toAbsolutePath().startsWith(rootLocation.toAbsolutePath())) {
            log.error("Attempt to access file outside storage root: {}", imagePath);
            throw new SecurityException("Attempt to access file outside storage root.");
        }
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new NoSuchFileException("Image not found or not readable: " + imagePath);
        }
        return Files.readAllBytes(filePath);
    }

    @Override
    public String getPublicUrl(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }
        // Example: http://localhost:8081/api/v1/images/product-uuid/image.jpg
        // This assumes a controller endpoint /api/v1/images/{productId}/{filename} will serve the image
        // Or, if Spring Boot serves static content from this path:
        // return ServletUriComponentsBuilder.fromCurrentContextPath()
        //         .pathSegment(publicPathSegment)
        //         .pathSegment(imagePath.startsWith("/") ? imagePath.substring(1) : imagePath)
        //         .toUriString();

        // For a controller based approach like /api/v1/images/{imagePathVarPart1}/{imagePathVarPart2}
        // The imagePath should be structured e.g. "productId/filename.jpg"
        // We need to make sure 'publicPathSegment' is the base path for the image controller itself.
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/") // Assuming common base for APIs
                .pathSegment(publicPathSegment) // e.g., "product-images"
                .path("/")
                .path(imagePath) // imagePath here is like "{productId}/{filename}"
                .toUriString();
    }
} 