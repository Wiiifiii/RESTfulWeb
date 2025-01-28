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

    @Transactional(readOnly = true)
    public List<Image> getAllActiveImages() {
        return imageRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public List<Image> filterImages(Long id, String owner, String contentType) {
        return imageRepository.filterImages(id, owner, contentType);
    }

    @Transactional(readOnly = true)
    public Optional<Image> getImageById(Long id) {
        return imageRepository.findById(id);
    }

    @Transactional
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    @Transactional
    public void softDeleteImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(true);
            imageRepository.save(image);
            logger.info("Image with ID {} soft deleted.", id);
        });
    }

    @Transactional
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
        return imageRepository.findAllDeleted();
    }

    @Transactional
    public void restoreImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(false);
            imageRepository.save(image);
            logger.info("Image with ID {} restored.", id);
        });
    }
}
