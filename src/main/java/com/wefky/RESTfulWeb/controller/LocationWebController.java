package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing Locations via web interface.
 */
@Controller
@RequestMapping("/web/locations")
@RequiredArgsConstructor
public class LocationWebController {

    private static final Logger logger = LoggerFactory.getLogger(LocationWebController.class);

    private final LocationRepository locationRepository;

    /**
     * LIST Active Locations with Filters
     */
    @GetMapping
    public String listLocations(
            @RequestParam(required = false) String citySearch,
            @RequestParam(required = false) String postalSearch,
            @RequestParam(required = false) Float latMin,
            @RequestParam(required = false) Float latMax,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<Location> locations;
            if ((citySearch == null || citySearch.isBlank()) &&
                (postalSearch == null || postalSearch.isBlank()) &&
                latMin == null && latMax == null) {
                locations = locationRepository.findAllActive();
            } else {
                locations = locationRepository.filterLocations(
                        citySearch == null || citySearch.isBlank() ? null : citySearch,
                        postalSearch == null || postalSearch.isBlank() ? null : postalSearch,
                        latMin,
                        latMax
                );
            }
            model.addAttribute("locations", locations);
            model.addAttribute("citySearch", citySearch);
            model.addAttribute("postalSearch", postalSearch);
            model.addAttribute("latMin", latMin);
            model.addAttribute("latMax", latMax);
            return "locations"; // -> locations.html
        } catch (Exception e) {
            logger.error("Error fetching locations: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching locations.");
            return "redirect:/web/locations";
        }
    }

    /**
     * NEW LOCATION FORM
     */
    @GetMapping("/new")
    public String newLocationForm(Model model) {
        model.addAttribute("location", new Location());
        model.addAttribute("mode", "new");
        return "locationForm";
    }

    /**
     * EDIT LOCATION FORM
     */
    @GetMapping("/edit/{id}")
    public String editLocationForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Location> opt = locationRepository.findById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Location not found.");
                return "redirect:/web/locations";
            }
            Location location = opt.get();
            model.addAttribute("location", location);
            model.addAttribute("mode", "edit");
            return "locationForm";
        } catch (Exception e) {
            logger.error("Error displaying edit location form: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while displaying the form.");
            return "redirect:/web/locations";
        }
    }

    /**
     * SAVE LOCATION (NEW OR EDIT)
     */
    @PostMapping("/save")
    public String saveLocation(
            @ModelAttribute Location location,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // If editing, ensure the location exists
            if (location.getLocationId() != null) {
                Optional<Location> opt = locationRepository.findById(location.getLocationId());
                if (opt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Location not found.");
                    return "redirect:/web/locations";
                }
            }
            locationRepository.save(location);
            redirectAttributes.addFlashAttribute("success", "Location saved successfully!");
            return "redirect:/web/locations";
        } catch (Exception e) {
            logger.error("Error saving location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while saving the location.");
            if (location.getLocationId() != null) {
                return "redirect:/web/locations/edit/" + location.getLocationId();
            } else {
                return "redirect:/web/locations/new";
            }
        }
    }

    /**
     * SOFT DELETE LOCATION
     */
    @GetMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Location> opt = locationRepository.findById(id);
            if (opt.isPresent()) {
                Location location = opt.get();
                location.setDeleted(true);
                locationRepository.save(location);
                redirectAttributes.addFlashAttribute("success", "Location deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Location not found.");
            }
            return "redirect:/web/locations";
        } catch (Exception e) {
            logger.error("Error soft deleting location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the location.");
            return "redirect:/web/locations";
        }
    }

    /**
     * VIEW TRASH BIN
     */
    @GetMapping("/trash")
    public String viewTrash(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Location> deletedLocations = locationRepository.findAllDeleted();
            model.addAttribute("deletedLocations", deletedLocations);
            return "locationsTrash";
        } catch (Exception e) {
            logger.error("Error fetching deleted locations: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted locations.");
            return "redirect:/web/locations";
        }
    }

    /**
     * RESTORE LOCATION
     */
    @GetMapping("/restore/{id}")
    public String restoreLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Location> opt = locationRepository.findById(id);
            if (opt.isPresent()) {
                Location location = opt.get();
                location.setDeleted(false);
                locationRepository.save(location);
                redirectAttributes.addFlashAttribute("success", "Location restored successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Location not found.");
            }
            return "redirect:/web/locations/trash";
        } catch (Exception e) {
            logger.error("Error restoring location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while restoring the location.");
            return "redirect:/web/locations/trash";
        }
    }

    /**
     * PERMANENTLY DELETE LOCATION (ADMIN ONLY)
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/delete-permanent/{id}")
    public String permanentlyDeleteLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (locationRepository.existsById(id)) {
                locationRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Location permanently deleted!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Location not found.");
            }
            return "redirect:/web/locations/trash";
        } catch (Exception e) {
            logger.error("Error permanently deleting location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the location.");
            return "redirect:/web/locations/trash";
        }
    }
}
