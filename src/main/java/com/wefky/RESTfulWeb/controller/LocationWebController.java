package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
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
 * LocationWebController is a Spring MVC controller that handles web requests related to locations.
 * It provides methods to list, create, edit, delete, and restore locations.
 * 
 * Annotations:
 * - @Controller: Indicates that this class serves as a controller in the Spring MVC framework.
 * - @RequestMapping("/web/locations"): Maps HTTP requests to handler methods of this controller.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments (final fields).
 * 
 * Methods:
 * - listLocations: Handles GET requests to list locations with optional filters.
 * - newLocationForm: Handles GET requests to display a form for creating a new location.
 * - editLocationForm: Handles GET requests to display a form for editing an existing location.
 * - saveLocation: Handles POST requests to save a new or edited location.
 * - softDeleteLocation: Handles POST requests to soft delete a location.
 * - viewTrash: Handles GET requests to view soft-deleted locations with optional filters.
 * - restoreLocation: Handles POST requests to restore a soft-deleted location.
 * - permanentlyDeleteLocation: Handles POST requests to permanently delete a location (requires admin role).
 * 
 * Each method includes error handling and logging to ensure that any issues are properly recorded and communicated to the user.
 */
@Controller
@RequestMapping("/web/locations")
@RequiredArgsConstructor
public class LocationWebController {

    private static final Logger logger = LoggerFactory.getLogger(LocationWebController.class);
    private final LocationService locationService;

    @GetMapping
    public String listLocations(@RequestParam(required = false) String cityNameSearch,
                                @RequestParam(required = false) String postalCodeSearch,
                                @RequestParam(required = false) Float latMin,
                                @RequestParam(required = false) Float latMax,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());
            List<Location> locations;
            boolean noFilters = (cityNameSearch == null || cityNameSearch.isBlank())
                    && (postalCodeSearch == null || postalCodeSearch.isBlank())
                    && latMin == null
                    && latMax == null;
            if (noFilters) {
                locations = locationService.getAllActiveLocations();
            } else {
                locations = locationService.filterLocations(cityNameSearch, postalCodeSearch, latMin, latMax);
            }
            model.addAttribute("locations", locations);
            model.addAttribute("cityNameSearch", cityNameSearch);
            model.addAttribute("postalCodeSearch", postalCodeSearch);
            model.addAttribute("latMin", latMin);
            model.addAttribute("latMax", latMax);
            return "locations"; // returns locations.html
        } catch (Exception e) {
            logger.error("Error fetching locations: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching locations.");
            return "redirect:/web/locations";
        }
    }

    @GetMapping("/new")
    public String newLocationForm(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("location", new Location());
        model.addAttribute("mode", "new");
        return "locationForm"; // returns locationForm.html
    }

    @GetMapping("/edit/{id}")
    public String editLocationForm(@PathVariable Long id,
                                   HttpServletRequest request,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());
            Optional<Location> opt = locationService.getLocationById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Location not found.");
                return "redirect:/web/locations";
            }
            model.addAttribute("location", opt.get());
            model.addAttribute("mode", "edit");
            return "locationForm"; // returns locationForm.html
        } catch (Exception e) {
            logger.error("Error displaying edit location form: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while displaying the form.");
            return "redirect:/web/locations";
        }
    }

    @PostMapping("/save")
    public String saveLocation(@ModelAttribute Location location,
                               RedirectAttributes redirectAttributes) {
        try {
            if (location.getLocationId() != null) {
                Optional<Location> existing = locationService.getLocationById(location.getLocationId());
                if (existing.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Location not found.");
                    return "redirect:/web/locations";
                }
            }
            locationService.saveLocation(location);
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

    // --- Updated Delete Action as a Standard POST Form Submission ---
    @PostMapping("/delete/{id}")
    public String softDeleteLocation(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            locationService.softDeleteLocation(id);
            redirectAttributes.addFlashAttribute("success", "Location deleted successfully!");
            return "redirect:/web/locations";
        } catch (Exception e) {
            logger.error("Error deleting location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the location.");
            return "redirect:/web/locations";
        }
    }

    @GetMapping("/trash")
    public String viewTrash(@RequestParam(required = false) String cityNameSearch,
                            @RequestParam(required = false) String postalCodeSearch,
                            @RequestParam(required = false) Float latMin,
                            @RequestParam(required = false) Float latMax,
                            HttpServletRequest request,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        model.addAttribute("currentUri", request.getRequestURI());
        try {
            List<Location> locations;
            boolean noFilters = (cityNameSearch == null || cityNameSearch.isBlank())
                    && (postalCodeSearch == null || postalCodeSearch.isBlank())
                    && latMin == null
                    && latMax == null;
            if (noFilters) {
                locations = locationService.getAllDeletedLocations();
            } else {
                locations = locationService.filterDeletedLocations(cityNameSearch, postalCodeSearch, latMin, latMax);
            }
            model.addAttribute("locations", locations);
            model.addAttribute("cityNameSearch", cityNameSearch);
            model.addAttribute("postalCodeSearch", postalCodeSearch);
            model.addAttribute("latMin", latMin);
            model.addAttribute("latMax", latMax);
            return "locationsTrash"; // returns locationsTrash.html
        } catch (Exception e) {
            logger.error("Error fetching deleted locations: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted locations.");
            return "redirect:/web/locations";
        }
    }

    @PostMapping("/restore/{id}")
    public String restoreLocation(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            locationService.restoreLocation(id);
            redirectAttributes.addFlashAttribute("success", "Location restored successfully!");
            return "redirect:/web/locations/trash";
        } catch (Exception e) {
            logger.error("Error restoring location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while restoring the location.");
            return "redirect:/web/locations/trash";
        }
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDeleteLocation(@PathVariable Long id,
                                            RedirectAttributes redirectAttributes) {
        try {
            locationService.permanentlyDeleteLocation(id);
            redirectAttributes.addFlashAttribute("success", "Location permanently deleted!");
            return "redirect:/web/locations/trash";
        } catch (Exception e) {
            logger.error("Error permanently deleting location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the location.");
            return "redirect:/web/locations/trash";
        }
    }
}
