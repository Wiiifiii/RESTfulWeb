package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import com.wefky.RESTfulWeb.repository.UserRepository;
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
 * This is optional if you only need the web-based approach.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageRestController {

    private static final Logger logger = LoggerFactory.getLogger(ImageRestController.class);

    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Image> getAllImages(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String contentType
    ) {
        if (id == null && (owner == null || owner.isBlank()) && (contentType == null || contentType.isBlank())) {
            return imageService.getAllActiveImages();
        }
        return imageService.filterImages(id, owner, contentType);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        Optional<Image> opt = imageService.getImageById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    // Soft-delete via REST
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

    // Restore via REST
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

    // Permanent Delete via REST
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
