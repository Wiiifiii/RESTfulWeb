package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public List<Image> getAllActiveImages() {
        List<Image> images = imageRepository.findAllActive();
        populateBase64(images);
        return images;
    }

    @Transactional(readOnly = true)
    public List<Image> filterImages(Long id, String owner, String contentType) {
        List<Image> images = imageRepository.filterImages(id, owner, contentType);
        populateBase64(images);
        return images;
    }

    @Transactional(readOnly = true)
    public Optional<Image> getImageById(Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        opt.ifPresent(this::populateBase64);
        return opt;
    }

    /**
     * Create or update an Image. Sets uploadDate if new.
     */
    public Image saveImage(Image image) {
        if (image.getImageId() == null) {
            // new => set upload date
            image.setUploadDate(LocalDateTime.now());
        }
        Image saved = imageRepository.save(image);
        populateBase64(saved);
        return saved;
    }

    public void softDeleteImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(true);
            imageRepository.save(image);
            logger.info("Image with ID {} soft deleted.", id);
        });
    }

    public void permanentlyDeleteImage(Long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            logger.info("Image with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Image with ID {}.", id);
        }
    }

    @Transactional(readOnly = true)
    public List<Image> getAllDeletedImages() {
        List<Image> images = imageRepository.findAllDeleted();
        populateBase64(images);
        return images;
    }

    @Transactional(readOnly = true)
    public List<Image> filterDeletedImages(Long id, String owner, String contentType) {
        List<Image> images = imageRepository.filterDeletedImages(id, owner, contentType);
        populateBase64(images);
        return images;
    }

    public void restoreImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(false);
            imageRepository.save(image);
            logger.info("Image with ID {} restored.", id);
        });
    }

    // Helper to populate base64 for a single or list
    private void populateBase64(Image img) {
        if (img.getData() != null && img.getContentType() != null && img.getContentType().startsWith("image/")) {
            String encoded = Base64.getEncoder().encodeToString(img.getData());
            img.setBase64Data(encoded);
        }
    }

    private void populateBase64(List<Image> images) {
        for (Image img : images) {
            populateBase64(img);
        }
    }
}
