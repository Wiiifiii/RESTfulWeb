package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Location;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    // Active only
    @Query("""
           SELECT l FROM Location l 
           WHERE l.deleted = false
           """)
    List<Location> findAllActive();

    // Filter: cityName, postalCode, lat range (all case-insensitive or partial matching)
    @Query("""
           SELECT l 
           FROM Location l 
           WHERE l.deleted = false
             AND (
               :cityName IS NULL 
               OR LOWER(l.cityName) LIKE LOWER(CONCAT('%', :cityName, '%'))
             )
             AND (
               :postalCode IS NULL 
               OR LOWER(l.postalCode) LIKE LOWER(CONCAT('%', :postalCode, '%'))
             )
             AND (
               :latMin IS NULL OR l.latitude >= :latMin
             )
             AND (
               :latMax IS NULL OR l.latitude <= :latMax
             )
           """)
    List<Location> filterLocations(
            @Param("cityName") String cityName,
            @Param("postalCode") String postalCode,
            @Param("latMin") Float latMin,
            @Param("latMax") Float latMax
    );

    // Trash
    @Query("SELECT l FROM Location l WHERE l.deleted = true")
    List<Location> findAllDeleted();
}
