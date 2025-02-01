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

    @Transactional(readOnly = true)
    public List<Location> getAllActiveLocations() {
        return locationRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public List<Location> filterLocations(String cityName, String postalCode, Float latMin, Float latMax) {
        return locationRepository.filterLocations(cityName, postalCode, latMin, latMax);
    }

    @Transactional(readOnly = true)
    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    @Transactional
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    @Transactional
    public void softDeleteLocation(Long id) {
        locationRepository.findById(id).ifPresent(location -> {
            location.setDeleted(true);
            locationRepository.save(location);
            logger.info("Location with ID {} soft deleted.", id);
        });
    }

    @Transactional
    public void permanentlyDeleteLocation(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            logger.info("Location with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Location with ID {}.", id);
        }
    }

    @Transactional(readOnly = true)
    public List<Location> getAllDeletedLocations() {
        return locationRepository.findAllDeleted();
    }

    @Transactional
    public void restoreLocation(Long id) {
        locationRepository.findById(id).ifPresent(location -> {
            location.setDeleted(false);
            locationRepository.save(location);
            logger.info("Location with ID {} restored.", id);
        });
    }


    public List<Location> filterDeletedLocations(String cityName, String postalCode, Float latMin, Float latMax) {
        // Either call a dedicated repository method or filter the list from getAllDeletedLocations()
        // For example, using Java Streams:
        List<Location> allDeleted = getAllDeletedLocations();
        return allDeleted.stream()
            .filter(loc -> (cityName == null || loc.getCityName().toLowerCase().contains(cityName.toLowerCase())))
            .filter(loc -> (postalCode == null || loc.getPostalCode().toLowerCase().contains(postalCode.toLowerCase())))
            .filter(loc -> (latMin == null || loc.getLatitude() >= latMin))
            .filter(loc -> (latMax == null || loc.getLatitude() <= latMax))
            .toList();
    }
}
