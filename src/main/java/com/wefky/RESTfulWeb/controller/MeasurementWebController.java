package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.service.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/web/measurements")
@RequiredArgsConstructor
public class MeasurementWebController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementWebController.class);

    private final MeasurementService measurementService;

    @GetMapping
    public String listMeasurements(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String cityName,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<Measurement> measurements;
            if ((measurementUnit == null || measurementUnit.isBlank()) &&
                startDate == null && endDate == null &&
                (cityName == null || cityName.isBlank())) {
                measurements = measurementService.getAllActiveMeasurements();
            } else {
                measurements = measurementService.filterMeasurements(measurementUnit, startDate, endDate, cityName);
            }
            model.addAttribute("measurements", measurements);
            model.addAttribute("measurementUnit", measurementUnit);
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
    public String newMeasurementForm(Model model) {
        model.addAttribute("measurement", new Measurement());
        model.addAttribute("mode", "new");
        return "measurementForm";
    }

    @GetMapping("/edit/{id}")
    public String editMeasurementForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Measurement> opt = measurementService.getMeasurementById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Measurement not found.");
                return "redirect:/web/measurements";
            }
            Measurement measurement = opt.get();
            model.addAttribute("measurement", measurement);
            model.addAttribute("mode", "edit");
            return "measurementForm";
        } catch (Exception e) {
            logger.error("Error displaying edit measurement form: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while displaying the form.");
            return "redirect:/web/measurements";
        }
    }

    @PostMapping("/save")
    public String saveMeasurement(
            @ModelAttribute Measurement measurement,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // If editing, ensure the measurement exists
            if (measurement.getMeasurementId() != null) {
                Optional<Measurement> opt = measurementService.getMeasurementById(measurement.getMeasurementId());
                if (opt.isEmpty()) {
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

    @GetMapping("/delete/{id}")
    public String softDeleteMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            measurementService.softDeleteMeasurement(id);
            redirectAttributes.addFlashAttribute("success", "Measurement deleted successfully!");
            return "redirect:/web/measurements";
        } catch (Exception e) {
            logger.error("Error soft deleting measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the measurement.");
            return "redirect:/web/measurements";
        }
    }

    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) String measurementUnit,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String cityName,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<Measurement> deletedMeasurements;
            if ((measurementUnit == null || measurementUnit.isBlank()) &&
                startDate == null && endDate == null &&
                (cityName == null || cityName.isBlank())) {
                deletedMeasurements = measurementService.getAllDeletedMeasurements();
            } else {
                // Filter and ensure only deleted measurements are shown
                deletedMeasurements = measurementService.filterMeasurements(measurementUnit, startDate, endDate, cityName).stream()
                        .filter(Measurement::isDeleted)
                        .toList();
            }
            model.addAttribute("measurements", deletedMeasurements);
            model.addAttribute("measurementUnit", measurementUnit);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("cityName", cityName);
            return "measurementsTrash"; // -> measurementsTrash.html
        } catch (Exception e) {
            logger.error("Error fetching deleted measurements: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted measurements.");
            return "redirect:/web/measurements";
        }
    }

    @PostMapping("/restore/{id}")
    public String restoreMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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
    public String permanentlyDeleteMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            measurementService.permanentlyDeleteMeasurement(id);
            redirectAttributes.addFlashAttribute("success", "Measurement permanently deleted!");
            return "redirect:/"; // Redirect to home page
        } catch (Exception e) {
            logger.error("Error permanently deleting measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the measurement.");
            return "redirect:/"; // Redirect to home page
        }
    }
}
