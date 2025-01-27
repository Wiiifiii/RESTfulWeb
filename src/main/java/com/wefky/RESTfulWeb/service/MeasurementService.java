package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementService.class);

    private final MeasurementRepository measurementRepository;

    /**
     * Retrieves all active measurements.
     */
    public List<Measurement> getAllMeasurements() {
        return measurementRepository.findAllActive();
    }

    /**
     * Filters measurements based on criteria.
     */
    public List<Measurement> filterMeasurements(String measurementUnit, LocalDateTime start, LocalDateTime end, String cityName) {
        return measurementRepository.filterMeasurements(measurementUnit, start, end, cityName);
    }

    /**
     * Retrieves a measurement by ID.
     */
    public Measurement getMeasurementById(Long id) {
        Optional<Measurement> opt = measurementRepository.findById(id);
        return opt.orElse(null);
    }

    /**
     * Saves a measurement.
     */
    public Measurement saveMeasurement(Measurement measurement) {
        return measurementRepository.save(measurement);
    }

    /**
     * Soft deletes a measurement.
     */
    public void deleteMeasurement(Long id) {
        Optional<Measurement> opt = measurementRepository.findById(id);
        if (opt.isPresent()) {
            Measurement measurement = opt.get();
            measurement.setDeleted(true);
            measurementRepository.save(measurement);
            logger.info("Measurement with ID {} soft deleted.", id);
        } else {
            logger.warn("Attempted to delete non-existent Measurement with ID {}.", id);
        }
    }

    /**
     * Permanently deletes a measurement.
     */
    public void permanentlyDeleteMeasurement(Long id) {
        if (measurementRepository.existsById(id)) {
            measurementRepository.deleteById(id);
            logger.info("Measurement with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Measurement with ID {}.", id);
        }
    }
}
