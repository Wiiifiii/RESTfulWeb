package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.projection.ImageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // Find all active (non-deleted) images as metadata
    @Query("SELECT i.imageId AS imageId, i.owner AS owner, i.deleted AS deleted, i.contentType AS contentType FROM Image i WHERE i.deleted = false")
    List<ImageMetadata> findAllActive();

    // Find all deleted images (for the trash) as metadata
    @Query("SELECT i.imageId AS imageId, i.owner AS owner, i.deleted AS deleted, i.contentType AS contentType FROM Image i WHERE i.deleted = true")
    List<ImageMetadata> findAllDeleted();

    // Custom filter method for active images returning metadata
    @Query("""
           SELECT i.imageId AS imageId, i.owner AS owner, i.deleted AS deleted, i.contentType AS contentType
           FROM Image i
           WHERE i.deleted = false
             AND (:ownerSearch IS NULL OR LOWER(i.owner) LIKE LOWER(CONCAT('%', :ownerSearch, '%')))
             AND (:idSearch IS NULL OR i.imageId = :idSearch)
             AND (:contentTypeSearch IS NULL OR LOWER(i.contentType) LIKE LOWER(CONCAT('%', :contentTypeSearch, '%')))
           """)
    List<ImageMetadata> filterImages(
            @Param("ownerSearch") String ownerSearch,
            @Param("idSearch") Long idSearch,
            @Param("contentTypeSearch") String contentTypeSearch
    );

    // Custom filter method for deleted images returning metadata
    @Query("""
           SELECT i.imageId AS imageId, i.owner AS owner, i.deleted AS deleted, i.contentType AS contentType
           FROM Image i
           WHERE i.deleted = true
             AND (:ownerSearch IS NULL OR LOWER(i.owner) LIKE LOWER(CONCAT('%', :ownerSearch, '%')))
             AND (:idSearch IS NULL OR i.imageId = :idSearch)
             AND (:contentTypeSearch IS NULL OR LOWER(i.contentType) LIKE LOWER(CONCAT('%', :contentTypeSearch, '%')))
           """)
    List<ImageMetadata> filterDeletedImages(
            @Param("ownerSearch") String ownerSearch,
            @Param("idSearch") Long idSearch,
            @Param("contentTypeSearch") String contentTypeSearch
    );
}
