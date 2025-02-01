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

        // Use a native query with COALESCE for filtering active measurements.
        @Query(value = "SELECT m.* " +
                        "FROM measurements m " +
                        "JOIN locations l ON l.location_id = m.location_id_fk " +
                        "WHERE m.deleted = false " +
                        "  AND lower(cast(m.measurement_unit as text)) LIKE lower(CONCAT('%', COALESCE(:measurementUnit, ''), '%')) "
                        +
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
