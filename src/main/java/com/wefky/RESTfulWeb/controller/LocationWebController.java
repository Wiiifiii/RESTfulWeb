package com.wefky.RESTfulWeb.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.service.LocationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

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

    /**
     * Handles GET requests to list locations with optional search filters.
     *
     * @param cityNameSearch      Optional search filter for city name.
     * @param postalCodeSearch    Optional search filter for postal code.
     * @param latMin              Optional search filter for minimum latitude.
     * @param latMax              Optional search filter for maximum latitude.
     * @param request             HttpServletRequest object to get the current URI.
     * @param model               Model object to add attributes to the view.
     * @param redirectAttributes  RedirectAttributes object to add flash attributes for redirection.
     * @return                    The name of the view to be rendered, either "locations" or a redirect to "/web/locations" in case of an error.
     */
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

    /**
     * Handles GET requests to "/new" and returns the form for creating a new location.
     *
     * @param request the HttpServletRequest object that contains the request the client made to the servlet
     * @param model the Model object used to pass attributes to the view
     * @return the name of the view template ("locationForm") to be rendered
     */
    @GetMapping("/new")
    public String newLocationForm(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("location", new Location());
        model.addAttribute("mode", "new");
        return "locationForm"; // returns locationForm.html
    }

    /**
     * Handles the GET request to display the edit location form.
     *
     * @param id the ID of the location to be edited
     * @param request the HttpServletRequest object
     * @param model the Model object to pass attributes to the view
     * @param redirectAttributes the RedirectAttributes object to pass flash attributes
     * @return the name of the view to be rendered
     */
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

    /**
     * Handles the POST request to save a location.
     * If the location ID is present, it checks if the location exists.
     * If the location does not exist, it redirects to the locations page with an error message.
     * If the location is saved successfully, it redirects to the locations page with a success message.
     * If an error occurs during saving, it logs the error and redirects to the appropriate page with an error message.
     *
     * @param location the location object to be saved
     * @param redirectAttributes attributes for flash messages
     * @return the redirect URL
     */
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

  
    /**
     * Handles the soft deletion of a location by its ID.
     *
     * @param id the ID of the location to be deleted
     * @param redirectAttributes attributes for a redirect scenario
     * @return the redirect URL to the locations page
     */
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

    /**
     * Handles GET requests to view the trash (deleted locations).
     * 
     * @param cityNameSearch      Optional search parameter for filtering by city name.
     * @param postalCodeSearch    Optional search parameter for filtering by postal code.
     * @param latMin              Optional search parameter for filtering by minimum latitude.
     * @param latMax              Optional search parameter for filtering by maximum latitude.
     * @param request             The HttpServletRequest object.
     * @param model               The Model object to pass attributes to the view.
     * @param redirectAttributes  The RedirectAttributes object to pass flash attributes.
     * @return                    The name of the view to be rendered, either "locationsTrash" or a redirect to "/web/locations" in case of an error.
     */
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

    /**
     * Restores a location by its ID.
     *
     * @param id the ID of the location to restore
     * @param redirectAttributes attributes for a redirect scenario
     * @return the redirect URL to the trash locations page
     */
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

    /**
     * Permanently deletes a location by its ID.
     * 
     * This method is secured and can only be accessed by users with the "ROLE_ADMIN" authority.
     * It attempts to delete the location permanently and adds a success message to the redirect attributes.
     * If an error occurs during the deletion process, an error message is added to the redirect attributes.
     * 
     * @param id the ID of the location to be permanently deleted
     * @param redirectAttributes the attributes for a redirect scenario
     * @return a redirect URL to the trash locations page
     */
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
