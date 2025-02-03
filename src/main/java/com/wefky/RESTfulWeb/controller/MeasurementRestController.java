package com.wefky.RESTfulWeb.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementRestController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementRestController.class);
    private final MeasurementRepository measurementRepository;

    /**
     * Retrieves a list of measurements based on the provided filters.
     * If no filters are provided, returns all active measurements.
     *
     * @param measurementUnit the unit of measurement to filter by (optional)
     * @param start the start date to filter by (optional, format: dd/MM/yyyy)
     * @param end the end date to filter by (optional, format: dd/MM/yyyy)
     * @param cityName the name of the city to filter by (optional)
     * @return a ResponseEntity containing the list of measurements
     */
    @GetMapping
    public ResponseEntity<List<Measurement>> getAllMeasurements(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) @DateTimeFormat(pattern="dd/MM/yyyy") LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(pattern="dd/MM/yyyy") LocalDate end,
            @RequestParam(required = false) String cityName
    ) {
        LocalDateTime startDateTime = (start != null) ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = (end != null) ? end.atTime(LocalTime.MAX) : null;
        boolean noFilters = (measurementUnit == null || measurementUnit.isEmpty())
                && startDateTime == null
                && endDateTime == null
                && (cityName == null || cityName.isEmpty());
        if (noFilters) {
            List<Measurement> measurements = measurementRepository.findAllActive();
            return ResponseEntity.ok(measurements);
        } else {
            List<Measurement> measurements = measurementRepository.filterMeasurementsNative(
                    (measurementUnit == null || measurementUnit.isEmpty()) ? null : measurementUnit,
                    startDateTime,
                    endDateTime,
                    (cityName == null || cityName.isEmpty()) ? null : cityName
            );
            return ResponseEntity.ok(measurements);
        }
    }

    /**
     * Retrieves a measurement by its ID.
     *
     * @param id the ID of the measurement to retrieve
     * @return a ResponseEntity containing the measurement if found and not deleted,
     *         or a ResponseEntity with a 404 Not Found status if the measurement is not found or is deleted
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
     * Creates a new Measurement.
     * 
     * This method handles HTTP POST requests to create a new Measurement entity.
     * If the timestamp of the measurement is not provided, it sets the current time as the timestamp.
     * It also ensures that the measurement ID is null and the deleted flag is set to false before saving.
     * 
     * @param measurement the Measurement object to be created
     * @return ResponseEntity containing the created Measurement object and HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<Measurement> createMeasurement(@RequestBody Measurement measurement) {
      
        if (measurement.getTimestamp() == null) {
            measurement.setTimestamp(LocalDateTime.now());
        }
        measurement.setMeasurementId(null);
        measurement.setDeleted(false);
        Measurement saved = measurementRepository.save(measurement);
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * Updates an existing Measurement with the provided data.
     *
     * @param id the ID of the Measurement to update
     * @param updated the Measurement object containing updated data
     * @return a ResponseEntity containing the updated Measurement if found and not deleted,
     *         or a ResponseEntity with a 404 Not Found status if the Measurement does not exist or is deleted
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
     * Soft deletes a measurement by setting its 'deleted' flag to true.
     * 
     * @param id the ID of the measurement to be soft deleted
     * @return a ResponseEntity with status 204 (No Content) if the measurement was successfully soft deleted,
     *         or status 404 (Not Found) if the measurement does not exist or is already deleted
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
     * Restores a deleted measurement by its ID.
     *
     * @param id the ID of the measurement to restore
     * @return ResponseEntity containing the restored Measurement if found and restored,
     *         or ResponseEntity with not found status if the measurement does not exist or is not deleted
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
     * Permanently deletes a measurement by its ID.
     * 
     * This method is secured and can only be accessed by users with the "ROLE_ADMIN" authority.
     * If the measurement with the specified ID does not exist, a 404 Not Found response is returned.
     * If the deletion is successful, a 204 No Content response is returned.
     * 
     * @param id the ID of the measurement to be deleted
     * @return a ResponseEntity with status 404 if the measurement is not found, or 204 if the deletion is successful
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
