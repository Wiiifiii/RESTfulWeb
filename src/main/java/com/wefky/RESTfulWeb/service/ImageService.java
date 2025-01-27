package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public List<Image> getAllImages() {
        return imageRepository.findAllActive();
    }

    /**
     * Filters images based on owner.
     */
    public List<Image> filterImages(String owner) {
        return imageRepository.filterImages(owner);
    }

    /**
     * Retrieves an image by ID.
     */
    public Image getImageById(Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        return opt.orElse(null);
    }

    /**
     * Saves an image.
     */
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    /**
     * Soft deletes an image.
     */
    public void deleteImage(Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        if (opt.isPresent()) {
            Image image = opt.get();
            image.setDeleted(true);
            imageRepository.save(image);
            logger.info("Image with ID {} soft deleted.", id);
        } else {
            logger.warn("Attempted to delete non-existent Image with ID {}.", id);
        }
    }

    /**
     * Permanently deletes an image.
     */
    public void permanentlyDeleteImage(Long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            logger.info("Image with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Image with ID {}.", id);
        }
    }
}
