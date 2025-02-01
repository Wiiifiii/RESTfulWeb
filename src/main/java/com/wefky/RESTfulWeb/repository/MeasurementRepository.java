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

    @Query("""
           SELECT m FROM Measurement m
           WHERE m.deleted = false
             AND (
                (:measurementUnit IS NOT NULL AND LOWER(m.measurementUnit) LIKE LOWER(CONCAT('%', :measurementUnit, '%')))
             OR (:start IS NOT NULL AND m.timestamp >= :start)
             OR (:end IS NOT NULL AND m.timestamp <= :end)
             OR (:cityName IS NOT NULL AND LOWER(m.location.cityName) LIKE LOWER(CONCAT('%', :cityName, '%')))
             OR (:measurementUnit IS NULL AND :start IS NULL AND :end IS NULL AND :cityName IS NULL)
             )
           """)
    List<Measurement> filterMeasurements(
            @Param("measurementUnit") String measurementUnit,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cityName") String cityName
    );

    @Query("SELECT m FROM Measurement m WHERE m.deleted = true")
    List<Measurement> findAllDeleted();
}
