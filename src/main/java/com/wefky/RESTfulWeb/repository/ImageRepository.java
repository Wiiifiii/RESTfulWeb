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

    // Custom filter method returning metadata
    @Query("""
           SELECT i.imageId AS imageId, i.owner AS owner, i.deleted AS deleted, i.contentType AS contentType
           FROM Image i
           WHERE i.deleted = false
             AND (
               :ownerSearch IS NULL
               OR LOWER(i.owner) LIKE LOWER(CONCAT('%', :ownerSearch, '%'))
             )
           """)
    List<ImageMetadata> filterImages(@Param("ownerSearch") String ownerSearch);
}
