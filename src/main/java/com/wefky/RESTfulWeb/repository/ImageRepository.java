package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i WHERE i.deleted = false")
    List<Image> findAllActive();

    @Query("SELECT i FROM Image i WHERE i.deleted = true")
    List<Image> findAllDeleted();

    /**
     * Unified search across imageId, owner, and contentType for active images.
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
     * Unified search across imageId, owner, and contentType for deleted images.
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
     * Fetch distinct content types from active images.
     */
    @Query("SELECT DISTINCT i.contentType FROM Image i WHERE i.deleted = false")
    List<String> findDistinctContentTypes();
}
