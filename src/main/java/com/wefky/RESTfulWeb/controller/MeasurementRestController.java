package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.service.MeasurementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementRestController {

    private final MeasurementService measurementService;

    public MeasurementRestController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    // GET all
    @GetMapping
    public List<Measurement> getAllMeasurements(
            @RequestParam(required=false) String measurementUnit,
            @RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
            @RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end,
            @RequestParam(required=false) String cityName
    ) {
        // if no filter params, just get all
        if ((measurementUnit == null || measurementUnit.isEmpty()) &&
            start == null && end == null &&
            (cityName == null || cityName.isEmpty())) {
            return measurementService.getAllMeasurements();
        } else {
            // advanced filtering
            return measurementService.filterMeasurements(
                    measurementUnit == null || measurementUnit.isEmpty() ? null : measurementUnit,
                    start,
                    end,
                    cityName == null || cityName.isEmpty() ? null : cityName
            );
        }
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<Measurement> getMeasurement(@PathVariable Long id) {
        Measurement m = measurementService.getMeasurementById(id);
        if (m == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(m);
    }

    // POST (create)
    @PostMapping
    public ResponseEntity<Measurement> createMeasurement(@RequestBody Measurement measurement) {
        Measurement saved = measurementService.saveMeasurement(measurement);
        return ResponseEntity.ok(saved);
    }

    // PUT (update)
    @PutMapping("/{id}")
    public ResponseEntity<Measurement> updateMeasurement(@PathVariable Long id, @RequestBody Measurement updated) {
        Measurement existing = measurementService.getMeasurementById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        // update fields
        existing.setMeasurementUnit(updated.getMeasurementUnit());
        existing.setAmount(updated.getAmount());
        existing.setTimestamp(updated.getTimestamp());
        existing.setLocation(updated.getLocation());
        // etc.
        measurementService.saveMeasurement(existing);
        return ResponseEntity.ok(existing);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeasurement(@PathVariable Long id) {
        measurementService.deleteMeasurement(id);
        return ResponseEntity.noContent().build();
    }
}
