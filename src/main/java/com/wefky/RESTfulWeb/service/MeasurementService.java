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

    @Transactional(readOnly = true)
    public List<Measurement> getAllActiveMeasurements() {
        return measurementRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public List<Measurement> filterMeasurements(String measurementUnit, LocalDateTime startDate, LocalDateTime endDate, String cityName) {
        return measurementRepository.filterMeasurementsNative(measurementUnit, startDate, endDate, cityName);
    }

    @Transactional(readOnly = true)
    public Optional<Measurement> getMeasurementById(Long id) {
        return measurementRepository.findById(id);
    }

    @Transactional
    public Measurement saveMeasurement(Measurement measurement) {
        return measurementRepository.save(measurement);
    }

    @Transactional
    public void softDeleteMeasurement(Long id) {
        measurementRepository.findById(id).ifPresent(measurement -> {
            measurement.setDeleted(true);
            measurementRepository.save(measurement);
            logger.info("Measurement with ID {} soft deleted.", id);
        });
    }

    @Transactional
    public void permanentlyDeleteMeasurement(Long id) {
        if (measurementRepository.existsById(id)) {
            measurementRepository.deleteById(id);
            logger.info("Measurement with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Measurement with ID {}.", id);
        }
    }

    @Transactional(readOnly = true)
    public List<Measurement> getAllDeletedMeasurements() {
        return measurementRepository.findAllDeleted();
    }

    @Transactional
    public void restoreMeasurement(Long id) {
        measurementRepository.findById(id).ifPresent(measurement -> {
            measurement.setDeleted(false);
            measurementRepository.save(measurement);
            logger.info("Measurement with ID {} restored.", id);
        });
    }

    @Transactional(readOnly = true)
    public List<Measurement> filterDeletedMeasurements(String measurementUnit, LocalDateTime start, LocalDateTime end, String cityName) {
        return measurementRepository.filterDeletedMeasurements(measurementUnit, start, end, cityName);
    }
}
