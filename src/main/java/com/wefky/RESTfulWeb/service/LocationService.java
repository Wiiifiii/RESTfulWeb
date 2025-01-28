package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;

    /**
     * Retrieves all active (non-deleted) locations.
     */
    @Transactional(readOnly = true)
    public List<Location> getAllActiveLocations() {
        return locationRepository.findAllActive();
    }

    /**
     * Filters locations based on optional city name, postal code, latitude range.
     *
     * @param cityName    Optional search term for city name.
     * @param postalCode  Optional search term for postal code.
     * @param latMin      Optional minimum latitude.
     * @param latMax      Optional maximum latitude.
     * @return List of filtered active Location entities.
     */
    @Transactional(readOnly = true)
    public List<Location> filterLocations(String cityName, String postalCode, Float latMin, Float latMax) {
        return locationRepository.filterLocations(cityName, postalCode, latMin, latMax);
    }

    /**
     * Retrieves a location by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    /**
     * Saves a location.
     */
    @Transactional
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    /**
     * Soft deletes a location.
     */
    @Transactional
    public void softDeleteLocation(Long id) {
        locationRepository.findById(id).ifPresent(location -> {
            location.setDeleted(true);
            locationRepository.save(location);
            logger.info("Location with ID {} soft deleted.", id);
        });
    }

    /**
     * Permanently deletes a location.
     */
    @Transactional
    public void permanentlyDeleteLocation(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            logger.info("Location with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Location with ID {}.", id);
        }
    }

    /**
     * Retrieves all deleted (soft-deleted) locations.
     */
    @Transactional(readOnly = true)
    public List<Location> getAllDeletedLocations() {
        return locationRepository.findAllDeleted();
    }

    /**
     * Restores a soft-deleted location.
     */
    @Transactional
    public void restoreLocation(Long id) {
        locationRepository.findById(id).ifPresent(location -> {
            location.setDeleted(false);
            locationRepository.save(location);
            logger.info("Location with ID {} restored.", id);
        });
    }
}
