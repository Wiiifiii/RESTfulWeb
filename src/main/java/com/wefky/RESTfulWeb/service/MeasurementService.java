package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementService.class);

    private final MeasurementRepository measurementRepository;

    /**
     * Retrieves all active (non-deleted) measurements.
     *
     * @return List of active measurements.
     */
    @Transactional(readOnly = true)
    public List<Measurement> getAllActiveMeasurements() {
        return measurementRepository.findAllActive();
    }

    /**
     * Filters measurements based on optional parameters.
     *
     * @param measurementUnit Optional measurement unit.
     * @param startDate       Optional start timestamp.
     * @param endDate         Optional end timestamp.
     * @param cityName        Optional city name for filtering by location.
     * @return List of filtered active measurements.
     */
    @Transactional(readOnly = true)
    public List<Measurement> filterMeasurements(String measurementUnit, LocalDateTime startDate, LocalDateTime endDate, String cityName) {
        return measurementRepository.filterMeasurements(measurementUnit, startDate, endDate, cityName);
    }

    /**
     * Retrieves a measurement by its ID.
     *
     * @param id Measurement ID.
     * @return Optional containing the measurement if found, empty otherwise.
     */
    @Transactional(readOnly = true)
    public Optional<Measurement> getMeasurementById(Long id) {
        return measurementRepository.findById(id);
    }

    /**
     * Saves or updates a measurement.
     *
     * @param measurement Measurement entity to save.
     * @return Saved measurement.
     */
    @Transactional
    public Measurement saveMeasurement(Measurement measurement) {
        return measurementRepository.save(measurement);
    }

    /**
     * Soft deletes a measurement by setting its `deleted` flag to true.
     *
     * @param id Measurement ID.
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
     * Permanently deletes a measurement by removing it from the database.
     *
     * @param id Measurement ID.
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
     * Retrieves all deleted (soft-deleted) measurements.
     *
     * @return List of deleted measurements.
     */
    @Transactional(readOnly = true)
    public List<Measurement> getAllDeletedMeasurements() {
        return measurementRepository.findAllDeleted();
    }

    /**
     * Restores a soft-deleted measurement by setting its `deleted` flag to false.
     *
     * @param id Measurement ID.
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
