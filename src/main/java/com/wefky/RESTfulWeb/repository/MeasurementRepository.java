package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    /**
     * Retrieves all active (non-deleted) measurements.
     *
     * @return List of active measurements.
     */
    @Query("""
           SELECT m FROM Measurement m
           WHERE m.deleted = false
           """)
    List<Measurement> findAllActive();

    /**
     * Filters active measurements based on optional criteria.
     *
     * @param measurementUnit Optional measurement unit.
     * @param start           Optional start timestamp.
     * @param end             Optional end timestamp.
     * @param cityName        Optional city name.
     * @return List of filtered active measurements.
     */
    @Query("""
           SELECT m FROM Measurement m
           WHERE m.deleted = false
             AND (:measurementUnit IS NULL OR LOWER(m.measurementUnit) LIKE LOWER(CONCAT('%', :measurementUnit, '%')))
             AND (:start IS NULL OR m.timestamp >= :start)
             AND (:end IS NULL OR m.timestamp <= :end)
             AND (:cityName IS NULL OR LOWER(m.location.cityName) LIKE LOWER(CONCAT('%', :cityName, '%')))
           """)
    List<Measurement> filterMeasurements(
            @Param("measurementUnit") String measurementUnit,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cityName") String cityName
    );

    /**
     * Retrieves all deleted (soft-deleted) measurements.
     *
     * @return List of deleted measurements.
     */
    @Query("SELECT m FROM Measurement m WHERE m.deleted = true")
    List<Measurement> findAllDeleted();
}
