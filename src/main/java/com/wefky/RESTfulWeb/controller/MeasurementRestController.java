package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Measurements via API.
 */
@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementRestController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementRestController.class);

    private final MeasurementRepository measurementRepository;

    /**
     * GET all active measurements with optional filters.
     */
    @GetMapping
    public List<Measurement> getAllMeasurements(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) String cityName
    ) {
        // if no filter params, return all active
        if ((measurementUnit == null || measurementUnit.isEmpty()) &&
            start == null && end == null &&
            (cityName == null || cityName.isEmpty())) {
            return measurementRepository.findAllActive();
        } else {
            // filter
            return measurementRepository.filterMeasurements(
                (measurementUnit == null || measurementUnit.isEmpty()) ? null : measurementUnit,
                start,
                end,
                (cityName == null || cityName.isEmpty()) ? null : cityName
            );
        }
    }

    /**
     * GET measurement by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Measurement> getMeasurement(@PathVariable Long id) {
        Optional<Measurement> opt = measurementRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    /**
     * POST create new measurement.
     */
    @PostMapping
    public ResponseEntity<Measurement> createMeasurement(@RequestBody Measurement measurement) {
        // Ensure that the ID is not set for new entities
        measurement.setMeasurementId(null);
        measurement.setDeleted(false);
        Measurement saved = measurementRepository.save(measurement);
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * PUT update existing measurement.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Measurement> updateMeasurement(@PathVariable Long id, @RequestBody Measurement updated) {
        Optional<Measurement> opt = measurementRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Measurement existing = opt.get();
        existing.setMeasurementUnit(updated.getMeasurementUnit());
        existing.setAmount(updated.getAmount());
        existing.setTimestamp(updated.getTimestamp());
        existing.setLocation(updated.getLocation());
        // Prevent updating the deleted flag via REST API
        measurementRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    /**
     * DELETE (Soft Delete) measurement.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteMeasurement(@PathVariable Long id) {
        Optional<Measurement> opt = measurementRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Measurement measurement = opt.get();
        measurement.setDeleted(true);
        measurementRepository.save(measurement);
        return ResponseEntity.noContent().build();
    }

    /**
     * RESTORE measurement (Soft Delete Inversion).
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Measurement> restoreMeasurement(@PathVariable Long id) {
        Optional<Measurement> opt = measurementRepository.findById(id);
        if (opt.isEmpty() || !opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Measurement measurement = opt.get();
        measurement.setDeleted(false);
        measurementRepository.save(measurement);
        return ResponseEntity.ok(measurement);
    }

    /**
     * DELETE (Hard Delete) measurement. ADMIN ONLY.
     */
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteMeasurement(@PathVariable Long id) {
        if (!measurementRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        measurementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
