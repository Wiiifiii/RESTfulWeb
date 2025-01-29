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

    @Query("SELECT i FROM Image i WHERE i.deleted = true")
    List<Image> findAllDeleted();

    /**
     * Filter deleted images (trash).
     */
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
}
