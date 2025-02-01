package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.service.LocationService;
import com.wefky.RESTfulWeb.service.MeasurementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * MeasurementWebController is a Spring MVC controller that handles web requests related to measurements.
 * It provides endpoints for listing, creating, editing, deleting, and restoring measurements.
 * 
 * Endpoints:
 * - GET /web/measurements: List all active measurements with optional filters.
 * - GET /web/measurements/new: Display form for creating a new measurement.
 * - GET /web/measurements/edit/{id}: Display form for editing an existing measurement.
 * - POST /web/measurements/save: Save a new or edited measurement.
 * - POST /web/measurements/delete/{id}: Soft delete a measurement.
 * - GET /web/measurements/trash: List all soft-deleted measurements with optional filters.
 * - POST /web/measurements/restore/{id}: Restore a soft-deleted measurement.
 * - POST /web/measurements/delete-permanent/{id}: Permanently delete a measurement (requires ROLE_ADMIN).
 * 
 * Dependencies:
 * - MeasurementService: Service for handling measurement-related operations.
 * - LocationService: Service for handling location-related operations.
 * 
 * Date-Time Formatters:
 * - dateTimeFormatter: Formatter for full date-time input (pattern: "dd/MM/yyyy HH:mm").
 * - dateFormatter: Formatter for date-only input (pattern: "dd/MM/yyyy").
 * 
 * Error Handling:
 * - Catches and logs exceptions, and sets error messages in redirect attributes.
 * 
 * Logging:
 * - Uses SLF4J Logger for logging errors and important actions.
 * 
 * Annotations:
 * - @Controller: Marks this class as a Spring MVC controller.
 * - @RequestMapping("/web/measurements"): Maps requests to /web/measurements to this controller.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments (final fields).
 * - @Secured("ROLE_ADMIN"): Secures the permanently delete endpoint to users with ROLE_ADMIN.
 */
@Controller
@RequestMapping("/web/measurements")
@RequiredArgsConstructor
public class MeasurementWebController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementWebController.class);
    private final MeasurementService measurementService;
    private final LocationService locationService;

    // Formatters for date-time input (both full date-time and date-only)
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping
    public String listMeasurements(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) String startDate, // raw string input
            @RequestParam(required = false) String endDate,   // raw string input
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
                    if (startDate.trim().length() == 10) {
                        LocalDate datePart = LocalDate.parse(startDate, dateFormatter);
                        startDateTime = datePart.atStartOfDay();
                    } else {
                        startDateTime = LocalDateTime.parse(startDate, dateTimeFormatter);
                    }
                }
                if (endDate != null && !endDate.isBlank()) {
                    if (endDate.trim().length() == 10) {
                        LocalDate datePart = LocalDate.parse(endDate, dateFormatter);
                        endDateTime = datePart.atTime(LocalTime.MAX);
                    } else {
                        endDateTime = LocalDateTime.parse(endDate, dateTimeFormatter);
                    }
                }
            } catch (DateTimeParseException dtpe) {
                logger.error("Error parsing date filter values: ", dtpe);
                redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use dd/MM/yyyy or dd/MM/yyyy HH:mm.");
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
            return "measurements"; // returns measurements.html
        } catch (Exception e) {
            logger.error("Error fetching measurements: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching measurements.");
            return "redirect:/web/measurements";
        }
    }

    @GetMapping("/new")
    public String newMeasurementForm(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("measurement", new Measurement());
        model.addAttribute("mode", "new");
        model.addAttribute("allLocations", locationService.getAllActiveLocations());
        return "measurementForm"; // returns measurementForm.html
    }

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

    @PostMapping("/save")
    public String saveMeasurement(@ModelAttribute Measurement measurement,
                                  RedirectAttributes redirectAttributes) {
        try {
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

    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) String startDate, // raw string input
            @RequestParam(required = false) String endDate,   // raw string input
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
                    if (startDate.trim().length() == 10) {
                        LocalDate datePart = LocalDate.parse(startDate, dateFormatter);
                        startDateTime = datePart.atStartOfDay();
                    } else {
                        startDateTime = LocalDateTime.parse(startDate, dateTimeFormatter);
                    }
                }
                if (endDate != null && !endDate.isBlank()) {
                    if (endDate.trim().length() == 10) {
                        LocalDate datePart = LocalDate.parse(endDate, dateFormatter);
                        endDateTime = datePart.atTime(LocalTime.MAX);
                    } else {
                        endDateTime = LocalDateTime.parse(endDate, dateTimeFormatter);
                    }
                }
            } catch (DateTimeParseException dtpe) {
                logger.error("Error parsing date filter values: ", dtpe);
                redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use dd/MM/yyyy or dd/MM/yyyy HH:mm.");
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
                deletedMeasurements = measurementService.filterMeasurements(
                        (measurementUnit == null || measurementUnit.isBlank()) ? null : measurementUnit,
                        startDateTime,
                        endDateTime,
                        (cityName == null || cityName.isBlank()) ? null : cityName
                );
                // Keep only deleted measurements.
                deletedMeasurements = deletedMeasurements.stream().filter(Measurement::isDeleted).toList();
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
