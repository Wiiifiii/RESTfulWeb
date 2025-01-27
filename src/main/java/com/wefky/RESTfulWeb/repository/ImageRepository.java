package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Retrieves all active (non-deleted) images.
     *
     * @return List of active images.
     */
    @Query("""
           SELECT i FROM Image i
           WHERE i.deleted = false
           """)
    List<Image> findAllActive();

    /**
     * Filters active images based on optional criteria.
     *
     * @param owner Optional owner name.
     * @return List of filtered active images.
     */
    @Query("""
           SELECT i FROM Image i
           WHERE i.deleted = false
             AND (:owner IS NULL OR LOWER(i.owner) LIKE LOWER(CONCAT('%', :owner, '%')))
           """)
    List<Image> filterImages(
            @Param("owner") String owner
    );

    /**
     * Retrieves all deleted (soft-deleted) images.
     *
     * @return List of deleted images.
     */
    @Query("SELECT i FROM Image i WHERE i.deleted = true")
    List<Image> findAllDeleted();
}
