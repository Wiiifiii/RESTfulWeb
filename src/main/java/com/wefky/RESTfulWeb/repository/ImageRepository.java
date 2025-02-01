package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for managing Image entities.
 * Extends JpaRepository to provide basic CRUD operations.
 * Contains custom queries for specific use cases.
 */
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Retrieves all active (non-deleted) images.
     * 
     * @return a list of active images.
     */
    @Query("SELECT i FROM Image i WHERE i.deleted = false")
    List<Image> findAllActive();

    /**
     * Retrieves all deleted images.
     * 
     * @return a list of deleted images.
     */
    @Query("SELECT i FROM Image i WHERE i.deleted = true")
    List<Image> findAllDeleted();

    /**
     * Searches for active images based on imageId, owner, or contentType.
     * The search is case-insensitive for owner and contentType.
     * 
     * @param id the image ID to search for (optional).
     * @param text the text to search for in owner or contentType (optional).
     * @return a list of active images matching the search criteria.
     */
    @Query("""
        SELECT i FROM Image i 
        WHERE i.deleted = false AND (
            (:id IS NOT NULL AND i.imageId = :id) OR 
            (:text IS NOT NULL AND LOWER(i.owner) LIKE LOWER(CONCAT('%', :text, '%'))) OR 
            (:text IS NOT NULL AND LOWER(i.contentType) = LOWER(:text))
        )
    """)
    List<Image> searchImages(
            @Param("id") Long id,
            @Param("text") String text
    );

    /**
     * Searches for deleted images based on imageId, owner, or contentType.
     * The search is case-insensitive for owner and contentType.
     * 
     * @param id the image ID to search for (optional).
     * @param text the text to search for in owner or contentType (optional).
     * @return a list of deleted images matching the search criteria.
     */
    @Query("""
        SELECT i FROM Image i 
        WHERE i.deleted = true AND (
            (:id IS NOT NULL AND i.imageId = :id) OR 
            (:text IS NOT NULL AND LOWER(i.owner) LIKE LOWER(CONCAT('%', :text, '%'))) OR 
            (:text IS NOT NULL AND LOWER(i.contentType) = LOWER(:text))
        )
    """)
    List<Image> searchDeletedImages(
            @Param("id") Long id,
            @Param("text") String text
    );

    /**
     * Fetches distinct content types from active images.
     * 
     * @return a list of distinct content types from active images.
     */
    @Query("SELECT DISTINCT i.contentType FROM Image i WHERE i.deleted = false")
    List<String> findDistinctContentTypes();
}
