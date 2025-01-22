package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.repository.LocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationRestController {

    private final LocationRepository locationRepository;

    public LocationRestController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    // GET all with optional filters
    @GetMapping
    public List<Location> getAllLocations(
            @RequestParam(required=false) String cityName,
            @RequestParam(required=false) String postalCode,
            @RequestParam(required=false) Float latMin,
            @RequestParam(required=false) Float latMax
    ) {
        // if no params, return all active
        if ((cityName == null || cityName.isEmpty()) &&
            (postalCode == null || postalCode.isEmpty()) &&
            latMin == null && latMax == null) {
            return locationRepository.findAllActive();
        } else {
            // filter
            return locationRepository.filterLocations(
                cityName == null || cityName.isEmpty() ? null : cityName,
                postalCode == null || postalCode.isEmpty() ? null : postalCode,
                latMin,
                latMax
            );
        }
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocation(@PathVariable Long id) {
        var opt = locationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    // POST (create)
    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        Location saved = locationRepository.save(location);
        return ResponseEntity.ok(saved);
    }

    // PUT (update)
    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @RequestBody Location updated) {
        var opt = locationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Location existing = opt.get();
        existing.setPostalCode(updated.getPostalCode());
        existing.setCityName(updated.getCityName());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        // etc.
        locationRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        // if doing real delete
        locationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
