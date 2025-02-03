package com.wefky.RESTfulWeb.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wefky.RESTfulWeb.entity.Measurement;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

/**
 * Retrieves all active (non-deleted) Measurement entities.
 *
 * @return a list of Measurement objects where the deleted flag is set to false.
 */
    @Query("SELECT m FROM Measurement m WHERE m.deleted = false")
    List<Measurement> findAllActive();
   
/**
 * Retrieves a list of measurements that match the provided filtering criteria.
 *
 * This query joins the measurements table with the locations table on the location_id
 * and applies several filters:
 *
 * - Ensures the measurement is not marked as deleted.
 * - Filters by measurement unit using a case-insensitive partial match.
 * - Filters measurements based on a timestamp range; if start or end is not provided,
 *   the respective condition is effectively ignored.
 * - Filters by city name in a case-insensitive manner using partial match.
 *
 * @param measurementUnit the measurement unit to search for; if null or empty, this filter is ignored
 * @param start the lower bound for the measurement timestamp; if null, no lower bound is applied
 * @param end the upper bound for the measurement timestamp; if null, no upper bound is applied
 * @param cityName the city name to search for; if null or empty, this filter is ignored
 * @return a list of {@link Measurement} objects that satisfy the provided filters
 */
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

/**
 * Retrieves all measurements that are marked as deleted.
 *
 * This method executes a JPQL query to find and return a list of measurements where the
 * deletion flag is set to true.
 *
 * @return a list of deleted measurements.
 */
    @Query("SELECT m FROM Measurement m WHERE m.deleted = true")
    List<Measurement> findAllDeleted();

/**
 * Retrieves a list of deleted measurements based on optional filtering criteria.
 *
 * This method returns measurements that have been marked as deleted. The filtering parameters are applied
 * in a case-insensitive manner and can be omitted (null) to bypass the corresponding filter:
 * - measurementUnit: If provided, filters measurements whose measurement unit contains the specified value.
 * - start: If provided, filters measurements with a timestamp greater than or equal to the specified start time.
 * - end: If provided, filters measurements with a timestamp less than or equal to the specified end time.
 * - cityName: If provided, filters measurements based on a case-insensitive search for the specified city name in the location.
 *
 * @param measurementUnit optional filter for the measurement unit (supports partial, case-insensitive matching)
 * @param start           optional filter for the start timestamp (inclusive)
 * @param end             optional filter for the end timestamp (inclusive)
 * @param cityName        optional filter for the city name (supports partial, case-insensitive matching)
 * @return a list of measurements that are marked as deleted and match the provided criteria
 */
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
