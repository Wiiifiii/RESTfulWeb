package com.wefky.RESTfulWeb.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.service.ImageService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing images.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageRestController {

    private static final Logger logger = LoggerFactory.getLogger(ImageRestController.class);
    private final ImageService imageService;

    @GetMapping
    public ResponseEntity<List<Image>> getAllImages(@RequestParam(required = false) String search) {
        try {
            List<Image> images = imageService.searchImages(search);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            logger.error("Error fetching images via REST API: ", e);
            return ResponseEntity.status(500).body(null);
        }
    }

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
