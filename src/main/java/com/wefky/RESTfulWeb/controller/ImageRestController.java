package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing images.
 * Provides endpoints to get, soft-delete, restore, and permanently delete images.
 * 
 * Endpoints:
 * - GET /api/images: Get all active images or search by a single query parameter.
 * - GET /api/images/{id}: Get a specific image by ID.
 * - DELETE /api/images/{id}: Soft-delete an image by ID.
 * - POST /api/images/{id}/restore: Restore a soft-deleted image by ID (Admin only).
 * - DELETE /api/images/{id}/permanent: Permanently delete an image by ID (Admin only).
 * 
 * Dependencies:
 * - ImageService: Service layer for image operations.
 * - Logger: For logging errors and information.
 * 
 * Annotations:
 * - @RestController: Indicates that this class is a REST controller.
 * - @RequestMapping("/api/images"): Maps HTTP requests to /api/images to this controller.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments (final fields).
 * - @Secured("ROLE_ADMIN"): Restricts access to methods to users with the ROLE_ADMIN authority.
 * 
 * Methods:
 * - getAllImages(String search): Fetches all active images or searches by a query parameter.
 * - getImageById(Long id): Fetches a specific image by its ID.
 * - softDeleteImage(Long id): Soft-deletes an image by its ID.
 * - restoreImage(Long id): Restores a soft-deleted image by its ID (Admin only).
 * - permanentlyDeleteImage(Long id): Permanently deletes an image by its ID (Admin only).
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageRestController {

    private static final Logger logger = LoggerFactory.getLogger(ImageRestController.class);

    private final ImageService imageService;

    /**
     * Get all active images or search by a single query parameter.
     */
    @GetMapping
    public ResponseEntity<List<Image>> getAllImages(
            @RequestParam(required = false) String search
    ) {
        try {
            List<Image> images = imageService.searchImages(search);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            logger.error("Error fetching images via REST API: ", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get a specific image by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        try {
            Optional<Image> opt = imageService.searchImages(null).stream()
                    .filter(img -> img.getImageId().equals(id))
                    .findFirst();
            if (opt.isEmpty() || opt.get().isDeleted()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(opt.get());
        } catch (Exception e) {
            logger.error("Error fetching image by ID via REST API: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Soft-delete an image by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteImage(@PathVariable Long id) {
        try {
            Optional<Image> opt = imageService.searchImages(null).stream()
                    .filter(img -> img.getImageId().equals(id))
                    .findFirst();
            if (opt.isEmpty() || opt.get().isDeleted()) {
                return ResponseEntity.notFound().build();
            }
            imageService.softDeleteImage(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error soft deleting image via REST API: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Restore a soft-deleted image by ID.
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/{id}/restore")
    public ResponseEntity<Image> restoreImage(@PathVariable Long id) {
        try {
            Optional<Image> opt = imageService.searchDeletedImages(null).stream()
                    .filter(img -> img.getImageId().equals(id))
                    .findFirst();
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            imageService.restoreImage(id);
            Optional<Image> restored = imageService.searchImages(null).stream()
                    .filter(img -> img.getImageId().equals(id))
                    .findFirst();
            return restored.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error restoring image via REST API: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Permanently delete an image by ID. ADMIN ONLY
     */
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteImage(@PathVariable Long id) {
        try {
            if (!imageService.searchImages(null).stream().anyMatch(img -> img.getImageId().equals(id))) {
                return ResponseEntity.notFound().build();
            }
            imageService.permanentlyDeleteImage(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error permanently deleting image via REST API: ", e);
            return ResponseEntity.status(500).build();
        }
    }
}
