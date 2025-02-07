package com.wefky.RESTfulWeb.controller;

import java.util.List;
import java.util.Optional;

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

@Controller
@RequestMapping("/web/locations")
@RequiredArgsConstructor
public class LocationWebController {

    private final LocationService locationService;

    @GetMapping
    public String listLocations(@RequestParam(required = false) String cityNameSearch,
                                @RequestParam(required = false) String postalCodeSearch,
                                @RequestParam(required = false) Float latMin,
                                @RequestParam(required = false) Float latMax,
                                HttpServletRequest request,
                                Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        boolean noFilters = (cityNameSearch == null || cityNameSearch.isBlank())
                && (postalCodeSearch == null || postalCodeSearch.isBlank())
                && latMin == null
                && latMax == null;
        List<Location> locations;
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
        return "locations";  // renders locations.html
    }

    @GetMapping("/new")
    public String newLocationForm(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("location", new Location());
        model.addAttribute("mode", "new");
        return "locationForm";  // renders locationForm.html
    }

    @GetMapping("/edit/{id}")
    public String editLocationForm(@PathVariable Long id,
                                   HttpServletRequest request,
                                   Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        Optional<Location> opt = locationService.getLocationById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Location not found.");
        }
        model.addAttribute("location", opt.get());
        model.addAttribute("mode", "edit");
        return "locationForm";  // renders locationForm.html
    }

    @PostMapping("/save")
    public String saveLocation(@ModelAttribute Location location,
                               RedirectAttributes redirectAttributes) {
        if (location.getLocationId() != null) {
            Optional<Location> existing = locationService.getLocationById(location.getLocationId());
            if (existing.isEmpty()) {
                throw new RuntimeException("Location not found.");
            }
        }
        locationService.saveLocation(location);
        redirectAttributes.addFlashAttribute("success", "Location saved successfully!");
        return "redirect:/web/locations";
    }

    @PostMapping("/delete/{id}")
    public String softDeleteLocation(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        locationService.softDeleteLocation(id);
        redirectAttributes.addFlashAttribute("success", "Location deleted successfully!");
        return "redirect:/web/locations";
    }

    @GetMapping("/trash")
    public String viewTrash(@RequestParam(required = false) String cityNameSearch,
                            @RequestParam(required = false) String postalCodeSearch,
                            @RequestParam(required = false) Float latMin,
                            @RequestParam(required = false) Float latMax,
                            HttpServletRequest request,
                            Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        boolean noFilters = (cityNameSearch == null || cityNameSearch.isBlank())
                && (postalCodeSearch == null || postalCodeSearch.isBlank())
                && latMin == null
                && latMax == null;
        List<Location> locations;
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
        return "locationsTrash";  // renders locationsTrash.html
    }

    @PostMapping("/restore/{id}")
    public String restoreLocation(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        locationService.restoreLocation(id);
        redirectAttributes.addFlashAttribute("success", "Location restored successfully!");
        return "redirect:/web/locations/trash";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDeleteLocation(@PathVariable Long id,
                                            RedirectAttributes redirectAttributes) {
        locationService.permanentlyDeleteLocation(id);
        redirectAttributes.addFlashAttribute("success", "Location permanently deleted!");
        return "redirect:/web/locations/trash";
    }
}
