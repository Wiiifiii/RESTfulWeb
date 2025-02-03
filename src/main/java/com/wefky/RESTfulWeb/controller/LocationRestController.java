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
    /**
     * Retrieves a list of locations based on the provided filters.
     * If no filters are provided, returns all active locations.
     *
     * @param cityName   Optional filter by city name.
     * @param postalCode Optional filter by postal code.
     * @param latMin     Optional filter by minimum latitude.
     * @param latMax     Optional filter by maximum latitude.
     * @return A list of locations matching the provided filters, or all active locations if no filters are provided.
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
    /**
     * Retrieves a location by its ID.
     *
     * @param id the ID of the location to retrieve
     * @return a ResponseEntity containing the location if found and not deleted,
     *         or a ResponseEntity with a 404 Not Found status if the location is not found or is deleted
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
    /**
     * Creates a new location.
     * 
     * This method handles HTTP POST requests to create a new location. It sets the
     * location ID to null and the deleted flag to false before saving the location
     * to the repository.
     * 
     * @param location the location to be created
     * @return a ResponseEntity containing the saved location
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
    /**
     * Updates an existing location with the provided details.
     *
     * @param id the ID of the location to update
     * @param updated the updated location details
     * @return a ResponseEntity containing the updated location if found, or a 404 Not Found status if the location does not exist or is marked as deleted
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
    /**
     * Soft deletes a location by setting its 'deleted' flag to true.
     *
     * @param id the ID of the location to be soft deleted
     * @return ResponseEntity<Void> with HTTP status 204 (No Content) if the location was successfully soft deleted,
     *         or HTTP status 404 (Not Found) if the location does not exist or is already deleted
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
    /**
     * REST endpoint to restore a deleted location.
     *
     * @param id the ID of the location to be restored
     * @return ResponseEntity containing the restored Location object if successful,
     *         or a 404 Not Found status if the location does not exist or is not marked as deleted
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
    /**
     * Permanently deletes a location by its ID.
     * 
     * This method is secured and requires the user to have the "ROLE_ADMIN" authority.
     * If the location with the specified ID does not exist, it returns a 404 Not Found response.
     * If the location is successfully deleted, it returns a 204 No Content response.
     * 
     * @param id the ID of the location to be deleted
     * @return a ResponseEntity with status 204 No Content if the deletion is successful,
     *         or 404 Not Found if the location does not exist
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
