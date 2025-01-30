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
     * Matches any of the provided criteria (OR logic).
     */
    @Query("""
        SELECT i
        FROM Image i
        WHERE i.deleted = false
          AND (
               (:id IS NOT NULL AND i.imageId = :id)
            OR (:owner IS NOT NULL AND LOWER(i.owner) LIKE LOWER(CONCAT('%', :owner, '%')))
            OR (:contentType IS NOT NULL AND LOWER(i.contentType) = LOWER(:contentType))
          )
    """)
    List<Image> filterImages(
            @Param("id") Long id,
            @Param("owner") String owner,
            @Param("contentType") String contentType
    );

    @Query("SELECT i FROM Image i WHERE i.deleted = true")
    List<Image> findAllDeleted();

    /**
     * Filter deleted (trash) images by optional id, owner, contentType.
     * Matches any of the provided criteria (OR logic).
     */
    @Query("""
        SELECT i
        FROM Image i
        WHERE i.deleted = true
          AND (
               (:id IS NOT NULL AND i.imageId = :id)
            OR (:owner IS NOT NULL AND LOWER(i.owner) LIKE LOWER(CONCAT('%', :owner, '%')))
            OR (:contentType IS NOT NULL AND LOWER(i.contentType) = LOWER(:contentType))
          )
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
}
