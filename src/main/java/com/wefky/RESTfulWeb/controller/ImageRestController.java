package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Images via API.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageRestController {

    private static final Logger logger = LoggerFactory.getLogger(ImageRestController.class);

    private final ImageRepository imageRepository;

    /**
     * GET all active images with optional filters.
     */
    @GetMapping
    public List<Image> getAllImages(
            @RequestParam(required = false) String owner
    ) {
        if (owner == null || owner.isEmpty()) {
            return imageRepository.findAllActive();
        } else {
            return imageRepository.filterImages(owner);
        }
    }

    /**
     * GET image by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    /**
     * POST create new image.
     */
    @PostMapping
    public ResponseEntity<Image> uploadImage(@RequestBody Image image) {
        // Ensure that the ID is not set for new entities
        image.setImageId(null);
        image.setDeleted(false);
        Image saved = imageRepository.save(image);
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * PUT update existing image.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Image> updateImage(@PathVariable Long id, @RequestBody Image imageDetails) {
        Optional<Image> opt = imageRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Image existing = opt.get();
        existing.setOwner(imageDetails.getOwner());
        existing.setData(imageDetails.getData());
        // Prevent updating the deleted flag via REST API
        imageRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    /**
     * DELETE (Soft Delete) image.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteImage(@PathVariable Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Image image = opt.get();
        image.setDeleted(true);
        imageRepository.save(image);
        return ResponseEntity.noContent().build();
    }

    /**
     * RESTORE image (Soft Delete Inversion).
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Image> restoreImage(@PathVariable Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        if (opt.isEmpty() || !opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Image image = opt.get();
        image.setDeleted(false);
        imageRepository.save(image);
        return ResponseEntity.ok(image);
    }

    /**
     * DELETE (Hard Delete) image. ADMIN ONLY.
     */
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteImage(@PathVariable Long id) {
        if (!imageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        imageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
