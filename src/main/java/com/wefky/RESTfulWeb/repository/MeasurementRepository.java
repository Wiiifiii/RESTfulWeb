package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    @Query("SELECT m FROM Measurement m WHERE m.deleted = false")
    List<Measurement> findAllActive();

    // Native query with explicit CAST in both the IS NOT NULL check and the comparison.
    @Query(value = "SELECT m.* " +
                   "FROM measurements m " +
                   "JOIN locations l ON l.location_id = m.location_id_fk " +
                   "WHERE m.deleted = false " +
                   "AND ( " +
                   "     (CAST(:measurementUnit AS text) IS NOT NULL " +
                   "          AND lower(cast(m.measurement_unit as text)) LIKE lower(CONCAT('%', CAST(:measurementUnit AS text), '%'))) " +
                   "  OR (CAST(:start AS timestamp) IS NOT NULL " +
                   "          AND m.timestamp >= CAST(:start AS timestamp)) " +
                   "  OR (CAST(:end AS timestamp) IS NOT NULL " +
                   "          AND m.timestamp <= CAST(:end AS timestamp)) " +
                   "  OR (CAST(:cityName AS text) IS NOT NULL " +
                   "          AND lower(l.city_name) LIKE lower(CONCAT('%', CAST(:cityName AS text), '%'))) " +
                   ")",
           nativeQuery = true)
    List<Measurement> filterMeasurementsNative(
            @Param("measurementUnit") String measurementUnit,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cityName") String cityName
    );

    @Query("SELECT m FROM Measurement m WHERE m.deleted = true")
    List<Measurement> findAllDeleted();
}
