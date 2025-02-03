package com.wefky.RESTfulWeb.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wefky.RESTfulWeb.entity.Measurement;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    @Query("SELECT m FROM Measurement m WHERE m.deleted = false")
    List<Measurement> findAllActive();

    // Native query for filtering active measurements.
    // Note the correction in the JOIN clause: it now uses m.location_id instead of m.location_id_fk.
    @Query(value = "SELECT m.* " +
            "FROM measurements m " +
            "JOIN locations l ON l.location_id = m.location_id " +
            "WHERE m.deleted = false " +
            "  AND lower(cast(m.measurement_unit as text)) LIKE lower(CONCAT('%', COALESCE(:measurementUnit, ''), '%')) " +
            "  AND m.timestamp >= COALESCE(:start, m.timestamp) " +
            "  AND m.timestamp <= COALESCE(:end, m.timestamp) " +
            "  AND lower(l.city_name) LIKE lower(CONCAT('%', COALESCE(:cityName, ''), '%'))", nativeQuery = true)
    List<Measurement> filterMeasurementsNative(
            @Param("measurementUnit") String measurementUnit,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cityName") String cityName);

    @Query("SELECT m FROM Measurement m WHERE m.deleted = true")
    List<Measurement> findAllDeleted();

    // JPQL query for filtering deleted measurements.
    @Query("""
        SELECT m FROM Measurement m
        WHERE m.deleted = true
          AND (:measurementUnit IS NULL OR LOWER(m.measurementUnit) LIKE LOWER(CONCAT('%', :measurementUnit, '%')))
          AND (:start IS NULL OR m.timestamp >= :start)
          AND (:end IS NULL OR m.timestamp <= :end)
          AND (:cityName IS NULL OR LOWER(m.location.cityName) LIKE LOWER(CONCAT('%', :cityName, '%')))
    """)
    List<Measurement> findAllDeleted(@Param("measurementUnit") String measurementUnit,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("cityName") String cityName);
}
