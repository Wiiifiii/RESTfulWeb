package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;
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

    private final MeasurementRepository measurementRepository;

    /**
     * LIST + FILTER Active Measurements
     */
    @GetMapping
    public String listMeasurements(
            @RequestParam(required = false) String unitSearch,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String cityName,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<Measurement> measurements;
            if ((unitSearch == null || unitSearch.isBlank()) &&
                (startDate == null || startDate.isBlank()) &&
                (endDate == null || endDate.isBlank()) &&
                (cityName == null || cityName.isBlank())) {
                measurements = measurementRepository.findAllActive();
            } else {
                LocalDateTime start = null;
                LocalDateTime end = null;
                if (startDate != null && !startDate.isBlank()) {
                    start = LocalDateTime.parse(startDate);
                }
                if (endDate != null && !endDate.isBlank()) {
                    end = LocalDateTime.parse(endDate);
                }
                measurements = measurementRepository.filterMeasurements(
                        unitSearch == null || unitSearch.isBlank() ? null : unitSearch,
                        start,
                        end,
                        cityName == null || cityName.isBlank() ? null : cityName
                );
            }
            model.addAttribute("measurements", measurements);
            model.addAttribute("unitSearch", unitSearch);
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

    /**
     * NEW Measurement Form
     */
    @GetMapping("/new")
    public String newMeasurementForm(Model model) {
        model.addAttribute("measurement", new Measurement());
        model.addAttribute("mode", "new");
        return "measurementForm";
    }

    /**
     * EDIT Measurement Form
     */
    @GetMapping("/edit/{id}")
    public String editMeasurementForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Measurement> opt = measurementRepository.findById(id);
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

    /**
     * SAVE Measurement (NEW OR EDIT)
     */
    @PostMapping("/save")
    public String saveMeasurement(
            @ModelAttribute Measurement measurement,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // If editing, ensure the measurement exists
            if (measurement.getMeasurementId() != null) {
                Optional<Measurement> opt = measurementRepository.findById(measurement.getMeasurementId());
                if (opt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Measurement not found.");
                    return "redirect:/web/measurements";
                }
            }
            measurementRepository.save(measurement);
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
     * SOFT DELETE Measurement
     */
    @GetMapping("/delete/{id}")
    public String softDeleteMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Measurement> opt = measurementRepository.findById(id);
            if (opt.isPresent()) {
                Measurement measurement = opt.get();
                measurement.setDeleted(true);
                measurementRepository.save(measurement);
                redirectAttributes.addFlashAttribute("success", "Measurement deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Measurement not found.");
            }
            return "redirect:/web/measurements";
        } catch (Exception e) {
            logger.error("Error soft deleting measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the measurement.");
            return "redirect:/web/measurements";
        }
    }

    /**
     * VIEW TRASH BIN for Measurements
     */
    @GetMapping("/trash")
    public String viewTrash(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Measurement> deletedMeasurements = measurementRepository.findAllDeleted();
            model.addAttribute("deletedMeasurements", deletedMeasurements);
            return "measurementsTrash";
        } catch (Exception e) {
            logger.error("Error fetching deleted measurements: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted measurements.");
            return "redirect:/web/measurements";
        }
    }

    /**
     * RESTORE Measurement
     */
    @GetMapping("/restore/{id}")
    public String restoreMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Measurement> opt = measurementRepository.findById(id);
            if (opt.isPresent()) {
                Measurement measurement = opt.get();
                measurement.setDeleted(false);
                measurementRepository.save(measurement);
                redirectAttributes.addFlashAttribute("success", "Measurement restored successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Measurement not found.");
            }
            return "redirect:/web/measurements/trash";
        } catch (Exception e) {
            logger.error("Error restoring measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while restoring the measurement.");
            return "redirect:/web/measurements/trash";
        }
    }

    /**
     * PERMANENTLY DELETE Measurement. ADMIN ONLY.
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/delete-permanent/{id}")
    public String permanentlyDeleteMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (measurementRepository.existsById(id)) {
                measurementRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Measurement permanently deleted!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Measurement not found.");
            }
            return "redirect:/web/measurements/trash";
        } catch (Exception e) {
            logger.error("Error permanently deleting measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the measurement.");
            return "redirect:/web/measurements/trash";
        }
    }
}
