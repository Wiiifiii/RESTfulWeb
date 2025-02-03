package com.wefky.RESTfulWeb.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        try {
            Optional<Image> opt = imageService.getImageById(id);
            return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching image by ID: ", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Endpoint to retrieve a file (only non-deleted files).
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) {
        Optional<Image> opt = imageService.getImageById(id);
        if (opt.isPresent()) {
            Image image = opt.get();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + (image.getTitle() != null ? image.getTitle() : "file") + "\"")
                    .body(image.getData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteImage(@PathVariable Long id) {
        try {
            imageService.softDeleteImage(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error soft deleting image: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Image> restoreImage(@PathVariable Long id) {
        try {
            imageService.restoreImage(id);
            Optional<Image> restored = imageService.getImageById(id);
            return restored.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error restoring image: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteImage(@PathVariable Long id) {
        try {
            imageService.permanentlyDeleteImage(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error permanently deleting image: ", e);
            return ResponseEntity.status(500).build();
        }
    }
}
