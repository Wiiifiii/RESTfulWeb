package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.service.LocationService;
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

@Controller
@RequestMapping("/web/locations")
@RequiredArgsConstructor
public class LocationWebController {

    private static final Logger logger = LoggerFactory.getLogger(LocationWebController.class);
    private final LocationService locationService;

    @GetMapping
    public String listLocations(
            @RequestParam(required = false) String cityNameSearch,
            @RequestParam(required = false) String postalCodeSearch,
            @RequestParam(required = false) Float latMin,
            @RequestParam(required = false) Float latMax,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<Location> locations;
            if ((cityNameSearch == null || cityNameSearch.isBlank()) &&
                (postalCodeSearch == null || postalCodeSearch.isBlank()) &&
                latMin == null && latMax == null) {
                locations = locationService.getAllActiveLocations();
            } else {
                locations = locationService.filterLocations(cityNameSearch, postalCodeSearch, latMin, latMax);
            }
            model.addAttribute("locations", locations);
            model.addAttribute("cityNameSearch", cityNameSearch);
            model.addAttribute("postalCodeSearch", postalCodeSearch);
            model.addAttribute("latMin", latMin);
            model.addAttribute("latMax", latMax);
            return "locations";
        } catch (Exception e) {
            logger.error("Error fetching locations: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching locations.");
            return "redirect:/web/locations";
        }
    }

    @GetMapping("/new")
    public String newLocationForm(Model model) {
        model.addAttribute("location", new Location());
        model.addAttribute("mode", "new");
        return "locationForm";
    }

    @GetMapping("/edit/{id}")
    public String editLocationForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Location> opt = locationService.getLocationById(id);
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

    @PostMapping("/save")
    public String saveLocation(
            @ModelAttribute Location location,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (location.getLocationId() != null) {
                Optional<Location> opt = locationService.getLocationById(location.getLocationId());
                if (opt.isEmpty()) {
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

    @GetMapping("/delete/{id}")
    public String softDeleteLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            locationService.softDeleteLocation(id);
            redirectAttributes.addFlashAttribute("success", "Location deleted successfully!");
            return "redirect:/web/locations";
        } catch (Exception e) {
            logger.error("Error soft deleting location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the location.");
            return "redirect:/web/locations";
        }
    }

    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) String cityNameSearch,
            @RequestParam(required = false) String postalCodeSearch,
            @RequestParam(required = false) Float latMin,
            @RequestParam(required = false) Float latMax,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<Location> deletedLocations;
            if ((cityNameSearch == null || cityNameSearch.isBlank()) &&
                (postalCodeSearch == null || postalCodeSearch.isBlank()) &&
                latMin == null && latMax == null) {
                deletedLocations = locationService.getAllDeletedLocations();
            } else {
                deletedLocations = locationService.filterLocations(cityNameSearch, postalCodeSearch, latMin, latMax)
                        .stream()
                        .filter(Location::isDeleted)
                        .toList();
            }
            model.addAttribute("locations", deletedLocations);
            model.addAttribute("cityNameSearch", cityNameSearch);
            model.addAttribute("postalCodeSearch", postalCodeSearch);
            model.addAttribute("latMin", latMin);
            model.addAttribute("latMax", latMax);
            return "locationsTrash";
        } catch (Exception e) {
            logger.error("Error fetching deleted locations: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted locations.");
            return "redirect:/web/locations";
        }
    }

    @PostMapping("/restore/{id}")
    public String restoreLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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
    public String permanentlyDeleteLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            locationService.permanentlyDeleteLocation(id);
            redirectAttributes.addFlashAttribute("success", "Location permanently deleted!");
            return "redirect:/";
        } catch (Exception e) {
            logger.error("Error permanently deleting location: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the location.");
            return "redirect:/";
        }
    }
}
