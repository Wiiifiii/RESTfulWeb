package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Locations via API.
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationRestController {

    private static final Logger logger = LoggerFactory.getLogger(LocationRestController.class);

    private final LocationRepository locationRepository;

    @GetMapping
    public List<Location> getAllLocations(
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) Float latMin,
            @RequestParam(required = false) Float latMax
    ) {
        if ((cityName == null || cityName.isEmpty()) &&
            (postalCode == null || postalCode.isEmpty()) &&
            latMin == null && latMax == null) {
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

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocation(@PathVariable Long id) {
        Optional<Location> opt = locationRepository.findById(id);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        location.setLocationId(null);
        location.setDeleted(false);
        Location saved = locationRepository.save(location);
        return ResponseEntity.ok(saved);
    }

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
