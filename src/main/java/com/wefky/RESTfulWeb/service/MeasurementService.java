package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.LocationRepository;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final LocationRepository locationRepository;

    public MeasurementService(MeasurementRepository measurementRepository,
                              LocationRepository locationRepository) {
        this.measurementRepository = measurementRepository;
        this.locationRepository = locationRepository;
    }

    // Basic CRUD from the service perspective
    public List<Measurement> getAllMeasurements() {
        // If you have a soft-delete approach, call measurementRepository.findAllActive()
        return measurementRepository.findAll();
    }

    public Measurement getMeasurementById(Long id) {
        return measurementRepository.findById(id).orElse(null);
    }

    public Measurement saveMeasurement(Measurement m) {
        return measurementRepository.save(m);
    }

    public void deleteMeasurement(Long id) {
        measurementRepository.deleteById(id);
    }

    // Example advanced query:
    // Filter by measurementUnit, start/end date, optional cityName for location
    public List<Measurement> filterMeasurements(String unit, LocalDateTime start, LocalDateTime end, String cityName) {
        // your measurementRepository might have a query like
        // measurementRepository.filterMeasurements(unit, start, end, cityName)
        // If you do 2-step city filtering via locationRepository, you can do that here.
        return measurementRepository.filterMeasurements(unit, start, end, cityName);
    }

    // If you want to load a location by name or ID and set it
    public Location getLocationById(Long locId) {
        return locationRepository.findById(locId).orElse(null);
    }
}
