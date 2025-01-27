package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing Measurement entities.
 */
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    /**
     * Retrieves all active measurements (not deleted).
     *
     * @return List of active Measurement entities.
     */
    @Query("SELECT m FROM Measurement m WHERE m.deleted = false")
    List<Measurement> findAllActive();

    /**
     * Retrieves all deleted measurements.
     *
     * @return List of deleted Measurement entities.
     */
    @Query("SELECT m FROM Measurement m WHERE m.deleted = true")
    List<Measurement> findAllDeleted();

    /**
     * Filters active measurements based on optional criteria.
     *
     * @param unit      Optional search term for measurement unit.
     * @param start     Optional start timestamp for filtering.
     * @param end       Optional end timestamp for filtering.
     * @param cityName  Optional search term for city name.
     * @return List of filtered active Measurement entities.
     */
    @Query("SELECT m FROM Measurement m WHERE m.deleted = false " +
           "AND (:unit IS NULL OR LOWER(m.measurementUnit) LIKE LOWER(CONCAT('%', :unit, '%'))) " +
           "AND (:start IS NULL OR m.timestamp >= :start) " +
           "AND (:end IS NULL OR m.timestamp <= :end) " +
           "AND (:cityName IS NULL OR (m.location IS NOT NULL AND LOWER(m.location.cityName) LIKE LOWER(CONCAT('%', :cityName, '%'))))")
    List<Measurement> filterMeasurements(
            @Param("unit") String unit,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cityName") String cityName
    );

    /**
     * Filters deleted measurements based on optional criteria.
     *
     * @param unit      Optional search term for measurement unit.
     * @param start     Optional start timestamp for filtering.
     * @param end       Optional end timestamp for filtering.
     * @param cityName  Optional search term for city name.
     * @return List of filtered deleted Measurement entities.
     */
    @Query("SELECT m FROM Measurement m WHERE m.deleted = true " +
           "AND (:unit IS NULL OR LOWER(m.measurementUnit) LIKE LOWER(CONCAT('%', :unit, '%'))) " +
           "AND (:start IS NULL OR m.timestamp >= :start) " +
           "AND (:end IS NULL OR m.timestamp <= :end) " +
           "AND (:cityName IS NULL OR (m.location IS NOT NULL AND LOWER(m.location.cityName) LIKE LOWER(CONCAT('%', :cityName, '%'))))")
    List<Measurement> filterDeletedMeasurements(
            @Param("unit") String unit,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cityName") String cityName
    );
}
