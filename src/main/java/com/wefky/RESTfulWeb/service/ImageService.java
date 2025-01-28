package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;

    /**
     * Retrieves all active images.
     */
    @Transactional(readOnly = true)
    public List<Image> getAllActiveImages() {
        return imageRepository.findAllActive();
    }

    /**
     * Filters active images based on optional criteria.
     *
     * @param id          Optional image ID.
     * @param owner       Optional owner name.
     * @param contentType Optional content type.
     * @return List of filtered active images.
     */
    @Transactional(readOnly = true)
    public List<Image> filterImages(Long id, String owner, String contentType) {
        return imageRepository.filterImages(id, owner, contentType);
    }

    /**
     * Retrieves an image by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Image> getImageById(Long id) {
        return imageRepository.findById(id);
    }

    /**
     * Saves an image.
     */
    @Transactional
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    /**
     * Soft deletes an image.
     */
    @Transactional
    public void softDeleteImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(true);
            imageRepository.save(image);
            logger.info("Image with ID {} soft deleted.", id);
        });
    }

    /**
     * Permanently deletes an image.
     */
    @Transactional
    public void permanentlyDeleteImage(Long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            logger.info("Image with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Image with ID {}.", id);
        }
    }

    /**
     * Retrieves all deleted images.
     */
    @Transactional(readOnly = true)
    public List<Image> getAllDeletedImages() {
        return imageRepository.findAllDeleted();
    }

    /**
     * Restores a soft-deleted image.
     */
    @Transactional
    public void restoreImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(false);
            imageRepository.save(image);
            logger.info("Image with ID {} restored.", id);
        });
    }
}
