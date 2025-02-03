package com.wefky.RESTfulWeb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wefky.RESTfulWeb.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Retrieves all active images.
     * @return a list of active images
     */
    @Query("SELECT i FROM Image i WHERE i.deleted = false")
    List<Image> findAllActive();

    /**
     * Retrieves all deleted images.
     * @return a list of deleted images
     */
    @Query("SELECT i FROM Image i WHERE i.deleted = true")
    List<Image> findAllDeleted();

    /**
     * Searches for images based on the provided filters.
     * @param id the ID of the image to search for (optional)
     * @param text the text to search for in the owner or content type (optional)
     * @return a list of images that match the search criteria
     */
    @Query("""
        SELECT i FROM Image i 
        WHERE i.deleted = false AND (
            (:id IS NOT NULL AND i.imageId = :id) OR 
            (:text IS NOT NULL AND LOWER(i.owner) LIKE LOWER(CONCAT('%', :text, '%'))) OR 
            (:text IS NOT NULL AND LOWER(i.contentType) = LOWER(:text))
        )
    """)
    List<Image> searchImages(@Param("id") Long id, @Param("text") String text);

    /**
     * Searches for deleted images based on the provided filters.
     * @param id the ID of the image to search for (optional)
     * @param text the text to search for in the owner or content type (optional)
     * @return a list of deleted images that match the search criteria
     */
    @Query("""
        SELECT i FROM Image i 
        WHERE i.deleted = true AND (
            (:id IS NOT NULL AND i.imageId = :id) OR 
            (:text IS NOT NULL AND LOWER(i.owner) LIKE LOWER(CONCAT('%', :text, '%'))) OR 
            (:text IS NOT NULL AND LOWER(i.contentType) = LOWER(:text))
        )
    """)
    List<Image> searchDeletedImages(@Param("id") Long id, @Param("text") String text);

    /**
     * Retrieves a list of distinct content types for active images.
     * @return a list of distinct content types
     */
    @Query("SELECT DISTINCT i.contentType FROM Image i WHERE i.deleted = false")
    List<String> findDistinctContentTypes();
}
