package com.wefky.RESTfulWeb.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.repository.LocationRepository;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing Locations via API.
 */
/**
 * REST controller for managing locations.
 * Provides endpoints for CRUD operations and filtering on locations.
 * 
 * Endpoints:
 * - GET /api/locations: Retrieve all active locations with optional filters.
 * - GET /api/locations/{id}: Retrieve a location by its ID.
 * - POST /api/locations: Create a new location.
 * - PUT /api/locations/{id}: Update an existing location.
 * - DELETE /api/locations/{id}: Soft delete a location.
 * - POST /api/locations/{id}/restore: Restore a soft-deleted location.
 * - DELETE /api/locations/{id}/permanent: Permanently delete a location (Admin only).
 * 
 * Dependencies:
 * - LocationRepository: Repository for accessing location data.
 * - Logger: Logger for logging information and errors.
 * 
 * Annotations:
 * - @RestController: Indicates that this class is a REST controller.
 * - @RequestMapping("/api/locations"): Maps HTTP requests to /api/locations to this controller.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments (final fields).
 * - @Secured("ROLE_ADMIN"): Secures the endpoint to be accessible only by users with ROLE_ADMIN.
 * 
 * Methods:
 * - getAllLocations: Retrieves all active locations with optional filters for city name, postal code, latitude minimum, and latitude maximum.
 * - getLocation: Retrieves a location by its ID. Returns 404 if the location is not found or is deleted.
 * - createLocation: Creates a new location. Sets the location ID to null and deleted flag to false before saving.
 * - updateLocation: Updates an existing location by its ID. Returns 404 if the location is not found or is deleted.
 * - softDeleteLocation: Soft deletes a location by setting its deleted flag to true. Returns 404 if the location is not found or is already deleted.
 * - restoreLocation: Restores a soft-deleted location by setting its deleted flag to false. Returns 404 if the location is not found or is not deleted.
 * - permanentlyDeleteLocation: Permanently deletes a location by its ID. Returns 404 if the location is not found. Accessible only by users with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationRestController {

    private static final Logger logger = LoggerFactory.getLogger(LocationRestController.class);

    private final LocationRepository locationRepository;

    /**
     * GET active locations with optional filters.
     */
    @GetMapping
    public List<Location> getAllLocations(
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) Float latMin,
            @RequestParam(required = false) Float latMax
    ) {
        boolean noFilters = (cityName == null || cityName.isEmpty())
                            && (postalCode == null || postalCode.isEmpty())
                            && latMin == null
                            && latMax == null;
        if (noFilters) {
            return locationRepository.findAllActive();
        } else {
            return locationRepository.filterLocations(
                    cityName == null || cityName.isEmpty() ? null : cityName,
                    postalCode == null || postalCode.isEmpty() ? null : postalCode,
                    latMin,
                    latMax
            );
        }
    }

    /**
     * GET location by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocation(@PathVariable Long id) {
        Optional<Location> opt = locationRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    /**
     * POST create new location.
     */
    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        location.setLocationId(null);
        location.setDeleted(false);
        Location saved = locationRepository.save(location);
        return ResponseEntity.ok(saved);
    }

    /**
     * PUT update existing location.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @RequestBody Location updated) {
        Optional<Location> opt = locationRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Location existing = opt.get();
        existing.setPostalCode(updated.getPostalCode());
        existing.setCityName(updated.getCityName());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        locationRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    /**
     * DELETE (Soft Delete) location.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteLocation(@PathVariable Long id) {
        Optional<Location> opt = locationRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Location location = opt.get();
        location.setDeleted(true);
        locationRepository.save(location);
        return ResponseEntity.noContent().build();
    }

    /**
     * RESTORE location.
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Location> restoreLocation(@PathVariable Long id) {
        Optional<Location> opt = locationRepository.findById(id);
        if (opt.isEmpty() || !opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        Location location = opt.get();
        location.setDeleted(false);
        locationRepository.save(location);
        return ResponseEntity.ok(location);
    }

    /**
     * DELETE (Hard Delete) location. ADMIN ONLY.
     */
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteLocation(@PathVariable Long id) {
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        locationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
