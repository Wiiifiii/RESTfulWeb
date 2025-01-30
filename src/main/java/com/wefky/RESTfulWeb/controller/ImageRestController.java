package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
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
 * REST Controller for images (/api/images).
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageRestController {

    private static final Logger logger = LoggerFactory.getLogger(ImageRestController.class);

    private final ImageService imageService;
    private final ImageRepository imageRepository;

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
            Optional<Image> opt = imageService.getImageById(id);
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
            Optional<Image> opt = imageRepository.findById(id);
            if (opt.isEmpty() || opt.get().isDeleted()) {
                return ResponseEntity.notFound().build();
            }
            Image image = opt.get();
            image.setDeleted(true);
            imageRepository.save(image);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error soft deleting image via REST API: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Restore a soft-deleted image by ID.
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Image> restoreImage(@PathVariable Long id) {
        try {
            Optional<Image> opt = imageRepository.findById(id);
            if (opt.isEmpty() || !opt.get().isDeleted()) {
                return ResponseEntity.notFound().build();
            }
            Image image = opt.get();
            image.setDeleted(false);
            imageRepository.save(image);
            return ResponseEntity.ok(image);
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
            if (!imageRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            imageRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error permanently deleting image via REST API: ", e);
            return ResponseEntity.status(500).build();
        }
    }
}
