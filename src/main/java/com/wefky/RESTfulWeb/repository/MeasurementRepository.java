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

    // Native query for filtering active measurements
    @Query(value = "SELECT m.* " +
                   "FROM measurements m " +
                   "JOIN locations l ON l.location_id = m.location_id_fk " +
                   "WHERE m.deleted = false " +
                   "  AND lower(cast(m.measurement_unit as text)) LIKE lower(CONCAT('%', COALESCE(:measurementUnit, ''), '%')) " +
                   "  AND m.timestamp >= COALESCE(CAST(:start AS timestamp), m.timestamp) " +
                   "  AND m.timestamp <= COALESCE(CAST(:end AS timestamp), m.timestamp) " +
                   "  AND lower(l.city_name) LIKE lower(CONCAT('%', COALESCE(:cityName, ''), '%'))",
           nativeQuery = true)
    List<Measurement> filterMeasurementsNative(
            @Param("measurementUnit") String measurementUnit,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cityName") String cityName
    );

    @Query("SELECT m FROM Measurement m WHERE m.deleted = true")
    List<Measurement> findAllDeleted();

    // --- UPDATED: Native query for filtering deleted measurements ---
    @Query(value = "SELECT m.* " +
                   "FROM measurements m " +
                   "JOIN locations l ON l.location_id = m.location_id_fk " +
                   "WHERE m.deleted = true " +
                   "  AND lower(cast(m.measurement_unit as text)) LIKE lower(CONCAT('%', COALESCE(:measurementUnit, ''), '%')) " +
                   "  AND m.timestamp >= COALESCE(CAST(:start AS timestamp), m.timestamp) " +
                   "  AND m.timestamp <= COALESCE(CAST(:end AS timestamp), m.timestamp) " +
                   "  AND lower(l.city_name) LIKE lower(CONCAT('%', COALESCE(:cityName, ''), '%'))",
           nativeQuery = true)
    List<Measurement> filterDeletedMeasurements(@Param("measurementUnit") String measurementUnit,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("cityName") String cityName);
}
