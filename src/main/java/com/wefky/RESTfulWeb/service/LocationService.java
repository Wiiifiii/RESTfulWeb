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

/**
 * Service class for managing locations.
 * Provides methods for retrieving, saving, deleting, and restoring locations.
 */
@Service
@RequiredArgsConstructor
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private final LocationRepository locationRepository;

    /**
     * Retrieves all active locations.
     *
     * @return a list of active locations.
     */
    @Transactional(readOnly = true)
    public List<Location> getAllActiveLocations() {
        return locationRepository.findAllActive();
    }

    /**
     * Filters locations based on the provided criteria.
     *
     * @param cityName the name of the city to filter by (optional).
     * @param postalCode the postal code to filter by (optional).
     * @param latMin the minimum latitude to filter by (optional).
     * @param latMax the maximum latitude to filter by (optional).
     * @return a list of locations that match the criteria.
     */
    @Transactional(readOnly = true)
    public List<Location> filterLocations(String cityName, String postalCode, Float latMin, Float latMax) {
        return locationRepository.filterLocations(cityName, postalCode, latMin, latMax);
    }

    /**
     * Retrieves a location by its ID.
     *
     * @param id the ID of the location.
     * @return an Optional containing the location if found, or empty if not found.
     */
    @Transactional(readOnly = true)
    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    /**
     * Saves a location.
     *
     * @param location the location to save.
     * @return the saved location.
     */
    @Transactional
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    /**
     * Soft deletes a location by setting its deleted flag to true.
     *
     * @param id the ID of the location to soft delete.
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
     * Permanently deletes a location by its ID.
     *
     * @param id the ID of the location to permanently delete.
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
     * Retrieves all deleted locations.
     *
     * @return a list of deleted locations.
     */
    @Transactional(readOnly = true)
    public List<Location> getAllDeletedLocations() {
        return locationRepository.findAllDeleted();
    }

    /**
     * Restores a soft-deleted location by setting its deleted flag to false.
     *
     * @param id the ID of the location to restore.
     */
    @Transactional
    public void restoreLocation(Long id) {
        locationRepository.findById(id).ifPresent(location -> {
            location.setDeleted(false);
            locationRepository.save(location);
            logger.info("Location with ID {} restored.", id);
        });
    }

    /**
     * Filters deleted locations based on the provided criteria.
     *
     * @param cityName the name of the city to filter by (optional).
     * @param postalCode the postal code to filter by (optional).
     * @param latMin the minimum latitude to filter by (optional).
     * @param latMax the maximum latitude to filter by (optional).
     * @return a list of deleted locations that match the criteria.
     */
    public List<Location> filterDeletedLocations(String cityName, String postalCode, Float latMin, Float latMax) {
        List<Location> allDeleted = getAllDeletedLocations();
        return allDeleted.stream()
            .filter(loc -> (cityName == null || loc.getCityName().toLowerCase().contains(cityName.toLowerCase())))
            .filter(loc -> (postalCode == null || loc.getPostalCode().toLowerCase().contains(postalCode.toLowerCase())))
            .filter(loc -> (latMin == null || loc.getLatitude() >= latMin))
            .filter(loc -> (latMax == null || loc.getLatitude() <= latMax))
            .toList();
    }
}
