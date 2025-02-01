package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Measurements via API.
 */
/**
 * MeasurementRestController is a REST controller that handles HTTP requests for managing measurements.
 * It provides endpoints for creating, retrieving, updating, soft deleting, restoring, and permanently deleting measurements.
 * 
 * Endpoints:
 * - GET /api/measurements: Retrieve all active measurements with optional filters.
 * - GET /api/measurements/{id}: Retrieve a specific measurement by its ID.
 * - POST /api/measurements: Create a new measurement.
 * - PUT /api/measurements/{id}: Update an existing measurement by its ID.
 * - DELETE /api/measurements/{id}: Soft delete a measurement by its ID.
 * - POST /api/measurements/{id}/restore: Restore a soft-deleted measurement by its ID.
 * - DELETE /api/measurements/{id}/permanent: Permanently delete a measurement by its ID (Admin only).
 * 
 * Dependencies:
 * - MeasurementRepository: Repository interface for accessing measurement data.
 * - Logger: Logger for logging information and errors.
 * 
 * Annotations:
 * - @RestController: Indicates that this class is a REST controller.
 * - @RequestMapping("/api/measurements"): Maps HTTP requests to /api/measurements to this controller.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments (final fields).
 * - @GetMapping: Maps HTTP GET requests to handler methods.
 * - @PostMapping: Maps HTTP POST requests to handler methods.
 * - @PutMapping: Maps HTTP PUT requests to handler methods.
 * - @DeleteMapping: Maps HTTP DELETE requests to handler methods.
 * - @Secured("ROLE_ADMIN"): Secures the endpoint to be accessible only by users with the ROLE_ADMIN authority.
 * 
 * Methods:
 * - getAllMeasurements: Retrieves all active measurements with optional filters for measurement unit, start date, end date, and city name.
 * - getMeasurement: Retrieves a specific measurement by its ID.
 * - createMeasurement: Creates a new measurement.
 * - updateMeasurement: Updates an existing measurement by its ID.
 * - softDeleteMeasurement: Soft deletes a measurement by its ID.
 * - restoreMeasurement: Restores a soft-deleted measurement by its ID.
 * - permanentlyDeleteMeasurement: Permanently deletes a measurement by its ID (Admin only).
 */
@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementRestController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementRestController.class);
    private final MeasurementRepository measurementRepository;

    /**
     * GET active measurements with optional filters.
     * Uses Finnish date format: dd/MM/yyyy HH:mm:ss.
     */
    @GetMapping
    public List<Measurement> getAllMeasurements(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false)
            @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) String cityName
    ) {
        boolean noFilters = (measurementUnit == null || measurementUnit.isEmpty())
                            && start == null
                            && end == null
                            && (cityName == null || cityName.isEmpty());
        if (noFilters) {
            return measurementRepository.findAllActive();
        } else {
            return measurementRepository.filterMeasurementsNative(
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
        measurementRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    /**
     * DELETE (Soft Delete) measurement.
     * (Note: This endpoint is used by the REST API. The Web Controller now uses a POST mapping for soft delete.)
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
     * RESTORE measurement.
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
