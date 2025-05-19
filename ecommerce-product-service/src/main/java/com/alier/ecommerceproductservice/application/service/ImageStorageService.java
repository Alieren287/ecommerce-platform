package com.alier.ecommerceproductservice.application.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Interface for services that handle storage of product images.
 * Implementations can support local disk, cloud storage (S3, Azure Blob), etc.
 */
public interface ImageStorageService {

    /**
     * Stores an image file associated with a product.
     *
     * @param productId The ID of the product the image belongs to.
     * @param file      The image file to store.
     * @return The publicly accessible URL or a unique identifier/path for the stored image.
     * @throws IOException If an error occurs during file storage.
     */
    String storeImage(UUID productId, MultipartFile file) throws IOException;

    /**
     * Deletes an image file from storage.
     *
     * @param imageUrlOrPath The URL or path of the image to delete.
     * @throws IOException If an error occurs during file deletion.
     */
    void deleteImage(String imageUrlOrPath) throws IOException;

    /**
     * Retrieves an image file as a byte array.
     *
     * @param imagePath The path or identifier of the image to retrieve.
     * @return Byte array of the image.
     * @throws IOException If an error occurs during file retrieval (e.g., file not found).
     */
    byte[] retrieveImageBytes(String imagePath) throws IOException;

    /**
     * Generates a full, publicly accessible URL for a given image path/identifier.
     * This is important if storeImage returns a relative path or internal identifier.
     *
     * @param imagePath The stored path or identifier of the image.
     * @return The full public URL.
     */
    String getPublicUrl(String imagePath);
} 