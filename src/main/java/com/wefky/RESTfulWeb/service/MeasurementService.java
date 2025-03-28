package com.wefky.RESTfulWeb.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MeasurementService {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementService.class);
    private final MeasurementRepository measurementRepository;

    /**
     * Retrieves all active measurements.
     *
     * @return a list of active measurements.
     */
    @Transactional(readOnly = true)
    public List<Measurement> getAllActiveMeasurements() {
        return measurementRepository.findAllActive();
    }

    /**
     * Filters measurements based on the provided criteria.
     *
     * @param measurementUnit the unit of measurement to filter by (optional).
     * @param startDate the start date to filter by (optional).
     * @param endDate the end date to filter by (optional).
     * @param cityName the name of the city to filter by (optional).
     * @return a list of measurements that match the criteria.
     */
    @Transactional(readOnly = true)
    public List<Measurement> filterMeasurements(String measurementUnit, LocalDateTime startDate, LocalDateTime endDate, String cityName) {
        return measurementRepository.filterMeasurementsNative(measurementUnit, startDate, endDate, cityName);
    }

    /**
     * Retrieves a measurement by its ID.
     *
     * @param id the ID of the measurement.
     * @return an Optional containing the measurement if found, or empty if not found.
     */
    @Transactional(readOnly = true)
    public Optional<Measurement> getMeasurementById(Long id) {
        return measurementRepository.findById(id);
    }

    /**
     * Saves a measurement.
     *
     * @param measurement the measurement to save.
     * @return the saved measurement.
     */
    @Transactional
    public Measurement saveMeasurement(Measurement measurement) {
        // Auto-set the timestamp if not provided. 25.1.2025
        if (measurement.getTimestamp() == null) {
            measurement.setTimestamp(LocalDateTime.now());
        }
        return measurementRepository.save(measurement);
    }

    /**
     * Soft deletes a measurement by setting its deleted flag to true.
     *
     * @param id the ID of the measurement to soft delete.
     */
    @Transactional
    public void softDeleteMeasurement(Long id) {
        measurementRepository.findById(id).ifPresent(measurement -> {
            measurement.setDeleted(true);
            measurementRepository.save(measurement);
            logger.info("Measurement with ID {} soft deleted.", id);
        });
    }

    /**
     * Permanently deletes a measurement.
     *
     * @param id the ID of the measurement to permanently delete.
     */
    @Transactional
    public void permanentlyDeleteMeasurement(Long id) {
        if (measurementRepository.existsById(id)) {
            measurementRepository.deleteById(id);
            logger.info("Measurement with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Measurement with ID {}.", id);
        }
    }

    /**
     * Retrieves all deleted measurements.
     *
     * @return a list of deleted measurements.
     */
    @Transactional(readOnly = true)
    public List<Measurement> getAllDeletedMeasurements() {
        return measurementRepository.findAllDeleted();
    }

    /**
     * Filters deleted measurements based on the provided criteria.
     *
     * @param measurementUnit the unit of measurement to filter by (optional).
     * @param start the start date to filter by (optional).
     * @param end the end date to filter by (optional).
     * @param cityName the name of the city to filter by (optional).
     * @return a list of deleted measurements that match the criteria.
     */
    @Transactional(readOnly = true)
    public List<Measurement> filterDeletedMeasurements(String measurementUnit, LocalDateTime start, LocalDateTime end, String cityName) {
        return measurementRepository.findAllDeleted(measurementUnit, start, end, cityName);
    }

    /**
     * Restores a soft-deleted measurement by setting its deleted flag to false.
     *
     * @param id the ID of the measurement to restore.
     */
    @Transactional
    public void restoreMeasurement(Long id) {
        measurementRepository.findById(id).ifPresent(measurement -> {
            measurement.setDeleted(false);
            measurementRepository.save(measurement);
            logger.info("Measurement with ID {} restored.", id);
        });
    }
}
