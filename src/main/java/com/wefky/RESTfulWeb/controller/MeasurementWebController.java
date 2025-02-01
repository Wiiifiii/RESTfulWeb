package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.service.MeasurementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/web/measurements")
@RequiredArgsConstructor
public class MeasurementWebController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementWebController.class);
    private final MeasurementService measurementService;
    private final DateTimeFormatter finnishFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @GetMapping
    public String listMeasurements(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) String startDate, // as String now
            @RequestParam(required = false) String endDate, // as String now
            @RequestParam(required = false) String cityName,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());

            // Parse the date strings into LocalDateTime objects
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            try {
                if (startDate != null && !startDate.isBlank()) {
                    startDateTime = LocalDateTime.parse(startDate, finnishFormatter);
                }
                if (endDate != null && !endDate.isBlank()) {
                    endDateTime = LocalDateTime.parse(endDate, finnishFormatter);
                }
            } catch (DateTimeParseException dtpe) {
                logger.error("Error parsing date filter values: ", dtpe);
                redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use dd/MM/yyyy HH:mm:ss.");
                return "redirect:/web/measurements";
            }

            // Determine if all filters are empty/null
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
                        (cityName == null || cityName.isBlank()) ? null : cityName);
            }

            model.addAttribute("measurements", measurements);
            model.addAttribute("measurementUnit", measurementUnit);
            // Pass the raw string values back to the view for display in the filter fields
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("cityName", cityName);

            return "measurements"; // -> measurements.html
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
        return "measurementForm";
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

    // Changed mapping from GET to POST for soft delete
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
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime endDate,
            @RequestParam(required = false) String cityName,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());

            boolean noFilters = (measurementUnit == null || measurementUnit.isBlank())
                    && startDate == null
                    && endDate == null
                    && (cityName == null || cityName.isBlank());

            List<Measurement> deletedMeasurements;
            if (noFilters) {
                deletedMeasurements = measurementService.getAllDeletedMeasurements();
            } else {
                // We reuse the same filter and then select only the deleted ones.
                deletedMeasurements = measurementService.filterMeasurements(
                        (measurementUnit == null || measurementUnit.isBlank()) ? null : measurementUnit,
                        startDate,
                        endDate,
                        (cityName == null || cityName.isBlank()) ? null : cityName);
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
            // Redirect back to the trash view
            return "redirect:/web/measurements/trash";
        } catch (Exception e) {
            logger.error("Error permanently deleting measurement: ", e);
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while permanently deleting the measurement.");
            return "redirect:/web/measurements/trash";
        }
    }
}
