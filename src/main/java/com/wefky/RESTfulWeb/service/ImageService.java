package com.wefky.RESTfulWeb.service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public List<Image> searchImages(String search) {
        if (search == null || search.isBlank()) {
            List<Image> images = imageRepository.findAllActive();
            populateBase64(images);
            return images;
        }
        Long searchId = null;
        try {
            searchId = Long.parseLong(search);
        } catch (NumberFormatException e) {
            // Not a number, treat search as text
        }
        List<Image> images;
        if (searchId != null) {
            images = imageRepository.searchImages(searchId, search);
        } else {
            images = imageRepository.searchImages(null, search.trim());
        }
        populateBase64(images);
        return images;
    }

    @Transactional(readOnly = true)
    public List<Image> searchDeletedImages(String search) {
        if (search == null || search.isBlank()) {
            List<Image> images = imageRepository.findAllDeleted();
            populateBase64(images);
            return images;
        }
        Long searchId = null;
        try {
            searchId = Long.parseLong(search);
        } catch (NumberFormatException e) {
            // Not a number, treat as text
        }
        List<Image> images;
        if (searchId != null) {
            images = imageRepository.searchDeletedImages(searchId, search);
        } else {
            images = imageRepository.searchDeletedImages(null, search.trim());
        }
        populateBase64(images);
        return images;
    }

    @Transactional(readOnly = true)
    public Optional<Image> getImageById(Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        if (opt.isPresent() && !opt.get().isDeleted()) {
            populateBase64(opt.get());
            return opt;
        }
        return Optional.empty();
    }

    public Image saveImage(Image image) {
        if (image.getImageId() == null) {
            image.setUploadDate(java.time.LocalDateTime.now());
        }
        Image saved = imageRepository.save(image);
        populateBase64(saved);
        return saved;
    }

    public void softDeleteImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(true);
            imageRepository.save(image);
            logger.info("Image with ID {} soft-deleted.", id);
        });
    }

    public void permanentlyDeleteImage(Long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            logger.info("Image with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempt to delete non-existent image with ID {}.", id);
        }
    }

    public void restoreImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(false);
            imageRepository.save(image);
            logger.info("Image with ID {} restored.", id);
        });
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctContentTypes() {
        return imageRepository.findDistinctContentTypes();
    }

    // --- Helper Methods to populate Base64 data ---
    private void populateBase64(Image img) {
        if (img.getData() != null && img.getContentType() != null && img.getContentType().startsWith("image/")) {
            String encoded = Base64.getEncoder().encodeToString(img.getData());
            img.setBase64Data(encoded);
        } else {
            img.setBase64Data(null);
        }
    }

    private void populateBase64(List<Image> images) {
        images.forEach(this::populateBase64);
    }
}
