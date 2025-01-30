package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i WHERE i.deleted = false")
    List<Image> findAllActive();

    /**
     * Filter active images by optional id, owner (partial), contentType (exact).
     * Matches all of the provided criteria (AND logic).
     */
    @Query("""
        SELECT i
        FROM Image i
        WHERE i.deleted = false
          AND (:id IS NULL OR i.imageId = :id)
          AND (:owner IS NULL OR LOWER(i.owner) LIKE LOWER(CONCAT('%', :owner, '%')))
          AND (:contentType IS NULL OR LOWER(i.contentType) = LOWER(:contentType))
    """)
    List<Image> filterImages(
            @Param("id") Long id,
            @Param("owner") String owner,
            @Param("contentType") String contentType
    );

    @Query("""
        SELECT i
        FROM Image i
        WHERE i.deleted = true
          AND (:id IS NULL OR i.imageId = :id)
          AND (:owner IS NULL OR LOWER(i.owner) LIKE LOWER(CONCAT('%', :owner, '%')))
          AND (:contentType IS NULL OR LOWER(i.contentType) = LOWER(:contentType))
    """)
    List<Image> filterDeletedImages(
            @Param("id") Long id,
            @Param("owner") String owner,
            @Param("contentType") String contentType
    );

    /**
     * Fetch distinct content types from active images.
     */
    @Query("SELECT DISTINCT i.contentType FROM Image i WHERE i.deleted = false")
    List<String> findDistinctContentTypes();

    /**
     * Search images by a single search term that can match imageId, owner, or contentType.
     */
    @Query("""
        SELECT i
        FROM Image i
        WHERE i.deleted = false
          AND (
               (:id IS NOT NULL AND i.imageId = :id)
            OR (:text IS NOT NULL AND LOWER(i.owner) LIKE LOWER(CONCAT('%', :text, '%')))
            OR (:text IS NOT NULL AND LOWER(i.contentType) = LOWER(:text))
          )
    """)
    List<Image> searchImages(
            @Param("id") Long id,
            @Param("text") String text
    );

    /**
     * Search deleted images by a single search term that can match imageId, owner, or contentType.
     */
    @Query("""
        SELECT i
        FROM Image i
        WHERE i.deleted = true
          AND (
               (:id IS NOT NULL AND i.imageId = :id)
            OR (:text IS NOT NULL AND LOWER(i.owner) LIKE LOWER(CONCAT('%', :text, '%')))
            OR (:text IS NOT NULL AND LOWER(i.contentType) = LOWER(:text))
          )
    """)
    List<Image> searchDeletedImages(
            @Param("id") Long id,
            @Param("text") String text
    );

    
@Query("SELECT i FROM Image i WHERE i.deleted = true")
List<Image> findAllDeleted();
}



