package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    /**
     * Retrieves all active (non-deleted) measurements.
     *
     * @return List of active measurements.
     */
    @Query("SELECT m FROM Measurement m WHERE m.deleted = false")
    List<Measurement> findAllActive();

    /**
     * Filters active measurements based on optional parameters.
     *
     * @param measurementUnit Optional measurement unit.
     * @param start Optional start timestamp (formatted as ISO_LOCAL_DATE_TIME).
     * @param end Optional end timestamp (formatted as ISO_LOCAL_DATE_TIME).
     * @param cityName Optional city name (through Location).
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

    /**
     * Notes:
     * - Ensure that `LocalDateTime` is formatted properly when calling filter methods.
     * - Use ISO_LOCAL_DATE_TIME format (e.g., "2023-01-27T12:34:56") for consistent parsing.
     * - If issues arise with date-time parsing in the application, check:
     *     1. Controller endpoints for proper date-time handling.
     *     2. Frontend or API clients for sending date-time in ISO format.
     *     3. Database schema to ensure `timestamp` columns match `LocalDateTime` expectations.
     */
}
