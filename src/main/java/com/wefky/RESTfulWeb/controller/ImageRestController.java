package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import com.wefky.RESTfulWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageRestController {

    private static final Logger logger = LoggerFactory.getLogger(ImageRestController.class);

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    /**
     * GET all active images with optional filters.
     */
    @GetMapping
    public List<Image> getAllImages(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String contentType
    ) {
        // If no filters, we could return findAllActive(),
        // otherwise use filterImages. Using a single approach:
        return imageRepository.filterImages(id, owner, contentType);
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
     * RESTORE image.
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
