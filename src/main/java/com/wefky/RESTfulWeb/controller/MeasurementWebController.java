package com.wefky.RESTfulWeb.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.service.LocationService;
import com.wefky.RESTfulWeb.service.MeasurementService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/measurements")
@RequiredArgsConstructor
public class MeasurementWebController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementWebController.class);
    private final MeasurementService measurementService;
    private final LocationService locationService;

    // Date formatter for dates only (dd/MM/yyyy)
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Handles GET requests to list measurements with optional filters.
     *
     * @param measurementUnit     Optional measurement unit to filter by.
     * @param startDate           Optional start date to filter by (expected format: dd/MM/yyyy).
     * @param endDate             Optional end date to filter by (expected format: dd/MM/yyyy).
     * @param cityName            Optional city name to filter by.
     * @param request             The HttpServletRequest object.
     * @param model               The Model object to pass attributes to the view.
     * @param redirectAttributes  The RedirectAttributes object to pass flash attributes.
     * @return                    The name of the view to render.
     */
    @GetMapping
    public String listMeasurements(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) String startDate, // expecting dd/MM/yyyy
            @RequestParam(required = false) String endDate,   // expecting dd/MM/yyyy
            @RequestParam(required = false) String cityName,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());

            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            try {
                if (startDate != null && !startDate.isBlank()) {
                    LocalDate datePart = LocalDate.parse(startDate, dateFormatter);
                    startDateTime = datePart.atStartOfDay();
                }
                if (endDate != null && !endDate.isBlank()) {
                    LocalDate datePart = LocalDate.parse(endDate, dateFormatter);
                    endDateTime = datePart.atTime(LocalTime.MAX);
                }
            } catch (DateTimeParseException e) {
                logger.error("Error parsing date filter values: ", e);
                redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use dd/MM/yyyy.");
                return "redirect:/web/measurements";
            }

            boolean noFilters = (measurementUnit == null || measurementUnit.isBlank())
                    && startDateTime == null
                    && endDateTime == null
                    && (cityName == null || cityName.isBlank());

            List<Measurement> measurements;
            if (noFilters) {
                measurements = measurementService.getAllActiveMeasurements();
            } else {
                measurements = measurementService.filterMeasurements(
                        (measurementUnit == null || measurementUnit.isBlank()) ? null : measurementUnit,
                        startDateTime,
                        endDateTime,
                        (cityName == null || cityName.isBlank()) ? null : cityName
                );
            }

            model.addAttribute("measurements", measurements);
            model.addAttribute("measurementUnit", measurementUnit);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("cityName", cityName);
            return "measurements";
        } catch (Exception e) {
            logger.error("Error fetching measurements: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching measurements.");
            return "redirect:/web/measurements";
        }
    }

    /**
     * Handles GET requests to "/new" and returns the view for creating a new measurement.
     * 
     * @param request the HttpServletRequest object containing the client's request
     * @param model the Model object used to pass attributes to the view
     * @return the name of the view to be rendered, in this case "measurementForm"
     */
    @GetMapping("/new")
    public String newMeasurementForm(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        Measurement measurement = new Measurement();
      
        measurement.setTimestamp(LocalDateTime.now());
        model.addAttribute("measurement", measurement);
        model.addAttribute("mode", "new");
        model.addAttribute("allLocations", locationService.getAllActiveLocations());
        return "measurementForm";
    }

    /**
     * Handles the GET request to display the edit measurement form.
     *
     * @param id The ID of the measurement to be edited.
     * @param request The HttpServletRequest object.
     * @param model The Model object to pass attributes to the view.
     * @param redirectAttributes The RedirectAttributes object to pass flash attributes.
     * @return The name of the view to be rendered.
     */
    @GetMapping("/edit/{id}")
    public String editMeasurementForm(@PathVariable Long id,
                                      HttpServletRequest request,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());
            Optional<Measurement> opt = measurementService.getMeasurementById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Measurement not found.");
                return "redirect:/web/measurements";
            }
            model.addAttribute("measurement", opt.get());
            model.addAttribute("mode", "edit");
            model.addAttribute("allLocations", locationService.getAllActiveLocations());
            return "measurementForm";
        } catch (Exception e) {
            logger.error("Error displaying edit measurement form: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while displaying the form.");
            return "redirect:/web/measurements";
        }
    }

    /**
     * Handles the saving of a Measurement object. If the measurement's timestamp is null,
     * it sets the current timestamp. If the measurement has an ID, it checks if the measurement
     * exists in the database. If not, it redirects with an error message. Otherwise, it saves
     * the measurement and redirects with a success message.
     *
     * @param measurement The Measurement object to be saved.
     * @param redirectAttributes Attributes for a redirect scenario.
     * @return A redirect URL to the measurements page or the edit/new measurement page in case of an error.
     */
    @PostMapping("/save")
    public String saveMeasurement(@ModelAttribute Measurement measurement,
                                  RedirectAttributes redirectAttributes) {
        try {
           
            if (measurement.getTimestamp() == null) {
                measurement.setTimestamp(LocalDateTime.now());
            }
          
            if (measurement.getMeasurementId() != null) {
                Optional<Measurement> existing = measurementService.getMeasurementById(measurement.getMeasurementId());
                if (existing.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Measurement not found.");
                    return "redirect:/web/measurements";
                }
            }
            measurementService.saveMeasurement(measurement);
            redirectAttributes.addFlashAttribute("success", "Measurement saved successfully!");
            return "redirect:/web/measurements";
        } catch (Exception e) {
            logger.error("Error saving measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while saving the measurement.");
            if (measurement.getMeasurementId() != null) {
                return "redirect:/web/measurements/edit/" + measurement.getMeasurementId();
            } else {
                return "redirect:/web/measurements/new";
            }
        }
    }

    /**
     * Handles the soft deletion of a measurement.
     * 
     * @param id the ID of the measurement to be deleted
     * @param redirectAttributes used to pass flash attributes to the redirected page
     * @return a redirect URL to the measurements page
     */
    @PostMapping("/delete/{id}")
    public String softDeleteMeasurement(@PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        try {
            measurementService.softDeleteMeasurement(id);
            redirectAttributes.addFlashAttribute("success", "Measurement deleted successfully!");
            return "redirect:/web/measurements";
        } catch (Exception e) {
            logger.error("Error deleting measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the measurement.");
            return "redirect:/web/measurements";
        }
    }

    /**
     * Handles GET requests to the "/trash" endpoint to view deleted measurements.
     * 
     * @param measurementUnit   Optional query parameter to filter by measurement unit.
     * @param startDate         Optional query parameter to filter by start date (format: dd/MM/yyyy).
     * @param endDate           Optional query parameter to filter by end date (format: dd/MM/yyyy).
     * @param cityName          Optional query parameter to filter by city name.
     * @param request           HttpServletRequest object to get the current URI.
     * @param model             Model object to pass attributes to the view.
     * @param redirectAttributes RedirectAttributes object to add flash attributes for redirection.
     * @return                  The name of the view to be rendered, or a redirect URL in case of errors.
     */
    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String cityName,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());

            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            try {
                if (startDate != null && !startDate.isBlank()) {
                    LocalDate datePart = LocalDate.parse(startDate, dateFormatter);
                    startDateTime = datePart.atStartOfDay();
                }
                if (endDate != null && !endDate.isBlank()) {
                    LocalDate datePart = LocalDate.parse(endDate, dateFormatter);
                    endDateTime = datePart.atTime(LocalTime.MAX);
                }
            } catch (DateTimeParseException e) {
                logger.error("Error parsing date filter values: ", e);
                redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use dd/MM/yyyy.");
                return "redirect:/web/measurements/trash";
            }

            boolean noFilters = (measurementUnit == null || measurementUnit.isBlank())
                    && startDateTime == null
                    && endDateTime == null
                    && (cityName == null || cityName.isBlank());

            List<Measurement> deletedMeasurements;
            if (noFilters) {
                deletedMeasurements = measurementService.getAllDeletedMeasurements();
            } else {
                deletedMeasurements = measurementService.filterDeletedMeasurements(
                        (measurementUnit == null || measurementUnit.isBlank()) ? null : measurementUnit,
                        startDateTime,
                        endDateTime,
                        (cityName == null || cityName.isBlank()) ? null : cityName
                );
            }

            model.addAttribute("deletedMeasurements", deletedMeasurements);
            model.addAttribute("measurementUnit", measurementUnit);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("cityName", cityName);
            return "measurementsTrash";
        } catch (Exception e) {
            logger.error("Error fetching deleted measurements: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted measurements.");
            return "redirect:/web/measurements";
        }
    }

    /**
     * Handles the restoration of a measurement by its ID.
     *
     * @param id the ID of the measurement to be restored
     * @param redirectAttributes attributes for a redirect scenario
     * @return the redirect URL to the trash page
     */
    @PostMapping("/restore/{id}")
    public String restoreMeasurement(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            measurementService.restoreMeasurement(id);
            redirectAttributes.addFlashAttribute("success", "Measurement restored successfully!");
            return "redirect:/web/measurements/trash";
        } catch (Exception e) {
            logger.error("Error restoring measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while restoring the measurement.");
            return "redirect:/web/measurements/trash";
        }
    }

    /**
     * Permanently deletes a measurement by its ID.
     * 
     * This method is secured and can only be accessed by users with the "ROLE_ADMIN" authority.
     * It attempts to delete the measurement permanently and adds a success message to the redirect attributes.
     * If an error occurs during the deletion process, it logs the error and adds an error message to the redirect attributes.
     * 
     * @param id the ID of the measurement to be permanently deleted
     * @param redirectAttributes the attributes for a redirect scenario
     * @return a redirect URL to the trash view of measurements
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDeleteMeasurement(@PathVariable Long id,
                                                 RedirectAttributes redirectAttributes) {
        try {
            measurementService.permanentlyDeleteMeasurement(id);
            redirectAttributes.addFlashAttribute("success", "Measurement permanently deleted!");
            return "redirect:/web/measurements/trash";
        } catch (Exception e) {
            logger.error("Error permanently deleting measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the measurement.");
            return "redirect:/web/measurements/trash";
        }
    }
}
