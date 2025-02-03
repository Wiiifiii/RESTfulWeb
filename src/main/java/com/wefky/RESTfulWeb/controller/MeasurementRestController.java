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

    @GetMapping("/{id}")
    public ResponseEntity<Measurement> getMeasurement(@PathVariable Long id) {
        Optional<Measurement> opt = measurementRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    @PostMapping
    public ResponseEntity<Measurement> createMeasurement(@RequestBody Measurement measurement) {
        // Ensure the measurement timestamp is set automatically.
        if (measurement.getTimestamp() == null) {
            measurement.setTimestamp(LocalDateTime.now());
        }
        measurement.setMeasurementId(null);
        measurement.setDeleted(false);
        Measurement saved = measurementRepository.save(measurement);
        return ResponseEntity.status(201).body(saved);
    }

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
