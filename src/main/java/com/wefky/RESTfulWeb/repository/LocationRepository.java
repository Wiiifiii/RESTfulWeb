package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Location;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Retrieves all active locations (not deleted).
     */
    @Query("""
           SELECT l FROM Location l
           WHERE l.deleted = false
           """)
    List<Location> findAllActive();

    /**
     * Filters active locations based on optional criteria.
     */
    @Query("""
           SELECT l FROM Location l
           WHERE l.deleted = false
             AND (:cityName IS NULL OR LOWER(l.cityName) LIKE LOWER(CONCAT('%', :cityName, '%')))
             AND (:postalCode IS NULL OR LOWER(l.postalCode) LIKE LOWER(CONCAT('%', :postalCode, '%')))
             AND (:latMin IS NULL OR l.latitude >= :latMin)
             AND (:latMax IS NULL OR l.latitude <= :latMax)
           """)
    List<Location> filterLocations(
            @Param("cityName") String cityName,
            @Param("postalCode") String postalCode,
            @Param("latMin") Float latMin,
            @Param("latMax") Float latMax
    );

    /**
     * Retrieves all deleted locations (soft-deleted).
     */
    @Query("SELECT l FROM Location l WHERE l.deleted = true")
    List<Location> findAllDeleted();
}
