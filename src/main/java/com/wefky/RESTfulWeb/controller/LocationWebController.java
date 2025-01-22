package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.repository.LocationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/web/locations")
public class LocationWebController {

    private final LocationRepository locationRepository;

    public LocationWebController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    // LIST + FILTER
    @GetMapping
    public String listLocations(
            @RequestParam(required = false) String citySearch,
            @RequestParam(required = false) String postalSearch,
            @RequestParam(required = false) Float latMin,
            @RequestParam(required = false) Float latMax,
            Model model
    ) {
        // If all filter fields are empty => findAllActive
        if ((citySearch == null || citySearch.isBlank()) &&
            (postalSearch == null || postalSearch.isBlank()) &&
            latMin == null && latMax == null) {
            model.addAttribute("locations", locationRepository.findAllActive());
        } else {
            List<Location> filtered = locationRepository.filterLocations(
                    citySearch == null || citySearch.isBlank() ? null : citySearch,
                    postalSearch == null || postalSearch.isBlank() ? null : postalSearch,
                    latMin,
                    latMax
            );
            model.addAttribute("locations", filtered);
        }
        return "locations"; // -> locations.html
    }

    // NEW LOCATION FORM
    @GetMapping("/new")
    public String newLocationForm(Model model) {
        model.addAttribute("location", new Location());
        model.addAttribute("mode", "new");
        return "locationForm";
    }

    // EDIT LOCATION FORM
    @GetMapping("/edit/{id}")
    public String editLocationForm(@PathVariable Long id, Model model) {
        var opt = locationRepository.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/web/locations?error=Location not found";
        }
        model.addAttribute("location", opt.get());
        model.addAttribute("mode", "edit");
        return "locationForm";
    }

    // SAVE LOCATION (NEW OR EDIT)
    @PostMapping("/save")
    public String saveLocation(@ModelAttribute Location location, RedirectAttributes redirectAttributes) {
        locationRepository.save(location);
        redirectAttributes.addFlashAttribute("success", "Location saved successfully!");
        return "redirect:/web/locations";
    }

    // SOFT DELETE LOCATION
    @GetMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var opt = locationRepository.findById(id);
        if (opt.isPresent()) {
            Location location = opt.get();
            location.setDeleted(true);
            locationRepository.save(location);
            redirectAttributes.addFlashAttribute("success", "Location deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Location not found");
        }
        return "redirect:/web/locations";
    }

    // VIEW TRASH BIN
    @GetMapping("/trash")
    public String viewTrash(Model model) {
        model.addAttribute("deletedLocations", locationRepository.findAllDeleted());
        return "locationsTrash";
    }

    // RESTORE LOCATION
    @GetMapping("/restore/{id}")
    public String restoreLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var opt = locationRepository.findById(id);
        if (opt.isPresent()) {
            Location location = opt.get();
            location.setDeleted(false);
            locationRepository.save(location);
            redirectAttributes.addFlashAttribute("success", "Location restored successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Location not found");
        }
        return "redirect:/web/locations/trash";
    }
}