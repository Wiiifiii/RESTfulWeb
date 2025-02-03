package com.wefky.RESTfulWeb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wefky.RESTfulWeb.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Retrieves all active locations.
     * @return a list of active locations
     */
    @Query("SELECT l FROM Location l WHERE l.deleted = false")
    List<Location> findAllActive();

    /**
     * Searches for locations based on the provided filters.
     * @param cityName the name of the city to search for (optional)
     * @param postalCode the postal code to search for (optional)
     * @param latMin the minimum latitude to search for (optional)
     * @param latMax the maximum latitude to search for (optional)
     * @return a list of locations that match the search criteria
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
     * Retrieves all deleted locations.
     * @return a list of deleted locations
     */
    @Query("SELECT l FROM Location l WHERE l.deleted = true")
    List<Location> findAllDeleted();
}
