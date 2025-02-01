package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing images, including searching, saving, deleting, and restoring images.
 * This class provides methods to handle both active and deleted images, as well as methods to 
 * convert image data to Base64 format for easier handling in web applications.
 * 
 * The class uses Spring's @Service annotation to indicate that it's a service component, 
 * and @Transactional to manage transactions.
 * 
 * Methods:
 * - searchImages(String search): Searches for active images based on imageId, owner, or contentType.
 * - searchDeletedImages(String search): Searches for deleted images based on imageId, owner, or contentType.
 * - getImageById(Long id): Fetches an image by its ID if it's not deleted.
 * - saveImage(Image image): Saves (creates or updates) an image.
 * - softDeleteImage(Long id): Soft-deletes an image by setting its 'deleted' flag to true.
 * - permanentlyDeleteImage(Long id): Permanently deletes an image from the database.
 * - restoreImage(Long id): Restores a soft-deleted image by setting its 'deleted' flag to false.
 * - getDistinctContentTypes(): Fetches distinct content types from active images.
 * - populateBase64(Image img): Populates Base64 data for a single image.
 * - populateBase64(List<Image> images): Populates Base64 data for a list of images.
 * 
 * Dependencies:
 * - ImageRepository: Repository interface for accessing image data.
 * - Logger: Logger for logging information and warnings.
 * 
 * Annotations:
 * - @Service: Indicates that this class is a service component.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments (final fields).
 * - @Transactional: Manages transactions for the methods in this class.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final ImageRepository imageRepository;

    /**
     * Unified search across imageId, owner, and contentType for active images.
     */
    @Transactional(readOnly = true)
    public List<Image> searchImages(String search) {
        if (search == null || search.isBlank()) {
            List<Image> images = imageRepository.findAllActive();
            populateBase64(images);
            return images;
        }

        Long searchId = null;
        String searchText = null;
        try {
            searchId = Long.parseLong(search);
        } catch (NumberFormatException e) {
            // Not a number, treat as text
        }

        if (searchId != null) {
            List<Image> images = imageRepository.searchImages(searchId, search);
            populateBase64(images);
            return images;
        } else {
            searchText = search.trim();
            List<Image> images = imageRepository.searchImages(null, searchText);
            populateBase64(images);
            return images;
        }
    }

    /**
     * Unified search across imageId, owner, and contentType for deleted images.
     */
    @Transactional(readOnly = true)
    public List<Image> searchDeletedImages(String search) {
        if (search == null || search.isBlank()) {
            List<Image> images = imageRepository.findAllDeleted();
            populateBase64(images);
            return images;
        }

        Long searchId = null;
        String searchText = null;
        try {
            searchId = Long.parseLong(search);
        } catch (NumberFormatException e) {
            // Not a number, treat as text
        }

        if (searchId != null) {
            List<Image> images = imageRepository.searchDeletedImages(searchId, search);
            populateBase64(images);
            return images;
        } else {
            searchText = search.trim();
            List<Image> images = imageRepository.searchDeletedImages(null, searchText);
            populateBase64(images);
            return images;
        }
    }

    /**
     * Fetch an image by its ID if it's not deleted.
     */
    @Transactional(readOnly = true)
    public Optional<Image> getImageById(Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        if (opt.isPresent() && !opt.get().isDeleted()) {
            populateBase64(opt.get());
            return opt;
        }
        return Optional.empty();
    }

    /**
     * Save (create or update) an image.
     */
    public Image saveImage(Image image) {
        if (image.getImageId() == null) {
            // New image => set upload date
            image.setUploadDate(java.time.LocalDateTime.now());
        }
        Image saved = imageRepository.save(image);
        populateBase64(saved);
        return saved;
    }

    /**
     * Soft-delete an image by setting its 'deleted' flag to true.
     */
    public void softDeleteImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(true);
            imageRepository.save(image);
            logger.info("Image with ID {} soft deleted.", id);
        });
    }

    /**
     * Permanently delete an image from the database.
     */
    public void permanentlyDeleteImage(Long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            logger.info("Image with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Image with ID {}.", id);
        }
    }

    /**
     * Restore a soft-deleted image by setting its 'deleted' flag to false.
     */
    public void restoreImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(false);
            imageRepository.save(image);
            logger.info("Image with ID {} restored.", id);
        });
    }

    /**
     * Fetch distinct content types from active images.
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctContentTypes() {
        List<String> contentTypes = imageRepository.findDistinctContentTypes();
        return contentTypes;
    }

    /**
     * Populate Base64 data for a single image.
     */
    private void populateBase64(Image img) {
        if (img.getData() != null && img.getContentType() != null) {
            if (img.getContentType().startsWith("image/")) {
                String encoded = Base64.getEncoder().encodeToString(img.getData());
                img.setBase64Data(encoded);
            } else {
                img.setBase64Data(null); // Not an image
            }
        } else {
            img.setBase64Data(null); // No data or content type
        }
    }

    /**
     * Populate Base64 data for a list of images.
     */
    private void populateBase64(List<Image> images) {
        for (Image img : images) {
            populateBase64(img);
        }
    }
}
