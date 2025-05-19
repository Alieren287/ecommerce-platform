package com.alier.ecommerceproductservice.infrastructure.controller;

import com.alier.ecommercecore.common.dto.BaseResponse;
import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import com.alier.ecommerceproductservice.application.service.ImageStorageService;
import com.alier.ecommerceproductservice.application.usecase.DeleteProductImageUseCase;
import com.alier.ecommerceproductservice.application.usecase.GetProductImagesUseCase;
import com.alier.ecommerceproductservice.application.usecase.UploadProductImageUseCase;
import com.alier.ecommercewebcore.rest.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Product Images", description = "Product Image Management APIs")
public class ProductImageController extends BaseController {

    private final UploadProductImageUseCase uploadProductImageUseCase;
    private final DeleteProductImageUseCase deleteProductImageUseCase;
    private final GetProductImagesUseCase getProductImagesUseCase;
    private final ImageStorageService imageStorageService; // For serving images directly

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an image for a product",
            description = "Uploads an image file and associates it with the specified product. Replaces ProductImageUploadRequest DTO approach.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image uploaded and associated successfully, returns updated product with new image URL"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., no file, invalid file type, file too large, missing product ID)"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Failed to store image")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> uploadProductImage(
            @Parameter(description = "Product ID", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID productId,
            @Parameter(description = "Image file to upload", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile imageFile) {
        log.info("REST request to upload image for product ID {}: Filename: {}", productId, imageFile.getOriginalFilename());
        UploadProductImageUseCase.UploadProductImageInput input =
                new UploadProductImageUseCase.UploadProductImageInput(productId, imageFile);
        ProductDTO updatedProduct = uploadProductImageUseCase.execute(input);
        return success(updatedProduct, "Product image uploaded and associated successfully.");
    }

    @GetMapping
    @Operation(summary = "Get all image URLs for a product",
            description = "Retrieves a list of all public image URLs associated with the specified product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image URLs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<List<String>>> getAllProductImagesForProduct(
            @Parameter(description = "Product ID", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID productId) {
        log.info("REST request to get all image URLs for product ID: {}", productId);
        GetProductImagesUseCase.GetProductImagesInput input =
                new GetProductImagesUseCase.GetProductImagesInput(productId);
        List<String> imageUrls = getProductImagesUseCase.execute(input);
        return success(imageUrls);
    }

    @DeleteMapping
    @Operation(summary = "Delete an image from a product by its public URL",
            description = "Disassociates the image URL from the product and attempts to delete the image file from storage.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image disassociated and delete attempted successfully, returns updated product"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., missing or invalid image URL)"),
            @ApiResponse(responseCode = "404", description = "Product not found or image URL not associated with the product")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> deleteProductImageByUrl(
            @Parameter(description = "Product ID", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID productId,
            @Parameter(description = "Full public URL of the image to delete", required = true)
            @RequestParam @NotBlank @org.hibernate.validator.constraints.URL String imageUrl) {
        log.info("REST request to delete image URL {} for product ID: {}", imageUrl, productId);
        DeleteProductImageUseCase.DeleteProductImageInput input =
                new DeleteProductImageUseCase.DeleteProductImageInput(productId, imageUrl);
        ProductDTO updatedProduct = deleteProductImageUseCase.execute(input);
        return success(updatedProduct, "Product image disassociated and deletion from storage attempted.");
    }

    // Endpoint to serve images directly from local storage
    // The path variable `imageFileName` should match the filename part of the path returned by ImageStorageService.storeImage()
    // It will be combined with `productId` to form the full internal path for `ImageStorageService.retrieveImageBytes`.
    @GetMapping("/{imageFileName:.+}") // :.+ to capture filenames with dots
    @Operation(summary = "Retrieve/serve a specific product image file directly",
            description = "Serves the raw image file. The {imageFileName} should be the name of the file as stored (e.g., UUID_randomUUID.jpg).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully", content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE)), // Adjust media type as needed or make dynamic
            @ApiResponse(responseCode = "404", description = "Image not found or product not found"),
            @ApiResponse(responseCode = "500", description = "Error retrieving image")
    })
    public ResponseEntity<Resource> serveProductImage(
            @Parameter(description = "Product ID associated with the image", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID productId,
            @Parameter(description = "Filename of the image (e.g., image.jpg, product_xyz.png)", required = true)
            @PathVariable String imageFileName) {
        log.debug("Request to serve image: {} for product: {}", imageFileName, productId);
        try {
            String internalImagePath = productId.toString() + "/" + imageFileName;
            byte[] imageBytes = imageStorageService.retrieveImageBytes(internalImagePath);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);

            // Try to determine content type
            String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            try {
                // Basic content type detection from filename. Can be enhanced.
                Path path = Paths.get(imageFileName);
                contentType = Files.probeContentType(path);
                if (contentType == null) {
                    // Fallback based on common extensions
                    if (imageFileName.toLowerCase().endsWith(".png")) contentType = MediaType.IMAGE_PNG_VALUE;
                    else if (imageFileName.toLowerCase().endsWith(".jpg") || imageFileName.toLowerCase().endsWith(".jpeg"))
                        contentType = MediaType.IMAGE_JPEG_VALUE;
                    else if (imageFileName.toLowerCase().endsWith(".gif")) contentType = MediaType.IMAGE_GIF_VALUE;
                    else if (imageFileName.toLowerCase().endsWith(".webp")) contentType = "image/webp";
                    else contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
            } catch (IOException e) {
                log.warn("Could not determine content type for image {}: {}", imageFileName, e.getMessage());
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageFileName + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("Error retrieving image {} for product {}: {}", imageFileName, productId, e.getMessage());
            // Consider specific exception for "file not found" to return 404
            if (e instanceof java.nio.file.NoSuchFileException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 