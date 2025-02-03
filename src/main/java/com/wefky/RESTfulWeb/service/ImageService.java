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

    /**
     * Searches for images based on the provided search criteria.
     * 
     * If the search string is null or blank, the method returns all active images.
     * Otherwise, it attempts to parse the search string as a numeric ID. If parsing succeeds,
     * a numeric-based search is performed; if it fails, the search is treated as textual.
     * Additionally, the method populates the base64 representation for each image before returning the list.
     * 
     * @param search the search term, which may be either a numeric identifier or text.
     * @return a list of images that match the search criteria.
     */
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
            // Not a number; treat search as text.
        }
        List<Image> images = (searchId != null)
                ? imageRepository.searchImages(searchId, search)
                : imageRepository.searchImages(null, search.trim());
        populateBase64(images);
        return images;
    }

    /**
     * Searches for deleted images based on the provided search criteria.
     *
     * If the search parameter is null or blank, the method retrieves all deleted images.
     * Otherwise, it attempts to parse the search string as a Long. If parsing is successful,
     * it uses the numeric value along with the search term; if parsing fails, it treats the
     * search term as text (after trimming).
     *
     * Before returning the results, the method populates each Image with a Base64 representation.</p>
     *
     * @param search the search criterion, which may be a numeric ID in string form or text.
     *         If null or blank, all deleted images are retrieved.
     * @return a list of deleted Image objects matching the search criteria.
     */
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
            // Not a number; treat search as text.
        }
        List<Image> images = (searchId != null)
                ? imageRepository.searchDeletedImages(searchId, search)
                : imageRepository.searchDeletedImages(null, search.trim());
        populateBase64(images);
        return images;
    }

    /**
     * Retrieves an image by its unique identifier.
     * This method executes in a read-only transactional context. It attempts to find
     * an image in the repository using the provided {id}. If an image is found
     * and it is not marked as deleted, it populates the image's Base64 representation
     * and returns an {@link java.util.Optional} containing the image. Otherwise, it returns
     * an empty {java.util.Optional}.
     *
     * @param id the unique identifier of the image
     * @return an {java.util.Optional} containing the non-deleted image if present,
     *         or an empty {java.util.Optional} if the image is not found or marked as deleted
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
     * Persists the given Image entity using the image repository.
     * If the provided Image object is new (i.e., its imageId is {null}),
     * the method sets its upload date to the current local date and time.
     * After saving, it populates the base64 representation of the saved image.
     *
     * @param image the Image object to be saved
     * @return the saved Image object with updated information such as the upload date,
     *         a populated base64 representation, and a non-null imageId if it was generated
     */
    public Image saveImage(Image image) {
        if (image.getImageId() == null) {
            image.setUploadDate(java.time.LocalDateTime.now());
        }
        Image saved = imageRepository.save(image);
        populateBase64(saved);
        return saved;
    }

    /**
     * Performs a soft deletion on an image by setting its deleted flag.
     * The image corresponding to the provided ID is retrieved from the repository.
     * If found, the method sets the image's deleted flag to true, saves the updated
     * image, and logs the action.
     *
     * @param id the unique identifier of the image to be soft-deleted
     */
    public void softDeleteImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(true);
            imageRepository.save(image);
            logger.info("Image with ID {} soft-deleted.", id);
        });
    }

    /**
     * Permanently deletes an image identified by the given ID.
     * This method first checks whether an image with the specified ID exists within the repository. 
     * If the image exists, it is removed permanently using the repository's delete method, and a corresponding 
     * informational log message is recorded. If the image does not exist, a warning is logged instead.
     *
     * @param id the unique identifier of the image to be deleted
     */
    public void permanentlyDeleteImage(Long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            logger.info("Image with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempt to delete non-existent image with ID {}.", id);
        }
    }

    /**
     * Restores a soft-deleted image by setting its deleted flag to false.
     * The method retrieves the image with the specified ID from the repository.
     * If the image is found, its deleted flag is set to false, and the updated image
     * is saved to the repository. A corresponding log message is recorded.
     *
     * @param id the unique identifier of the image to be restored
     */
    public void restoreImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(false);
            imageRepository.save(image);
            logger.info("Image with ID {} restored.", id);
        });
    }
    
    /**
     * Retrieves a list of distinct content types for all images in the repository.
     * This method executes a query to retrieve a list of distinct content types from the image repository.
     * The content types are returned as a list of strings.
     *
     * @return a list of distinct content types for all images in the repository
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctContentTypes() {
        return imageRepository.findDistinctContentTypes();
    }

    // Helper Methods to populate Base64 data. 28.1.2025
    /**
     * Populates the Base64 encoded data field of the given image.
     * This method checks if the image's data and content type are non-null. It then verifies if the
     * content type indicates an image, a PDF, or a Word document (either legacy or OpenXML format). If
     * the content type matches any of these supported formats, the image data is encoded to a Base64
     * string and set on the image. Otherwise, the Base64 data field is set to null.
     * @param img the image object whose data is to be encoded and populated
     */
    private void populateBase64(Image img) {
        if (img.getData() != null && img.getContentType() != null &&
                (img.getContentType().startsWith("image/") ||
                 img.getContentType().equalsIgnoreCase("application/pdf") ||
                 img.getContentType().equalsIgnoreCase("application/msword") ||
                 img.getContentType().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            String encoded = Base64.getEncoder().encodeToString(img.getData());
            img.setBase64Data(encoded);
        } else {
            img.setBase64Data(null);
        }
    }

    /**
     * Populates the Base64 encoded data field for each image in the provided list.
     * This method iterates over the list of images and calls the populateBase64 method
     * on each image to populate its Base64 data field.
     * @param images the list of images to be processed
     */
    private void populateBase64(List<Image> images) {
        images.forEach(this::populateBase64);
    }

    /**
     * Retrieves an image by its unique identifier, including deleted images.
     * This method executes in a read-only transactional context. It attempts to find
     * an image in the repository using the provided {id}. If an image is found,
     * it populates the image's Base64 representation and returns an {@link java.util.Optional}
     * containing the image. If the image is not found, the method returns an empty {java.util.Optional}.
     * @param id the unique identifier of the image
     * @return an {java.util.Optional} containing the image if present, or an empty {java.util.Optional} if the image is not found
     */
    @Transactional(readOnly = true)
    public Optional<Image> getImageByIdIncludingDeleted(Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        if (opt.isPresent()) {
            populateBase64(opt.get());
            return opt;
        }
        return Optional.empty();
    }
}
