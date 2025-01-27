package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.LocationRepository;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing Measurements.
 */
@Controller
@RequestMapping("/web/measurements")
@RequiredArgsConstructor
public class MeasurementWebController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementWebController.class);

    private final MeasurementRepository measurementRepository;
    private final LocationRepository locationRepository;

    /**
     * LIST Active Measurements with Filters
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime start = null;
            LocalDateTime end = null;

            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDateTime.parse(startDate, formatter);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDateTime.parse(endDate, formatter);
            }

            if ((unitSearch == null || unitSearch.isEmpty()) &&
                (startDate == null || startDate.isEmpty()) &&
                (endDate == null || endDate.isEmpty()) &&
                (cityName == null || cityName.isEmpty())) {
                measurements = measurementRepository.findAllActive();
            } else {
                measurements = measurementRepository.filterMeasurements(unitSearch, start, end, cityName);
            }

            model.addAttribute("measurements", measurements);
            model.addAttribute("unitSearch", unitSearch);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("cityName", cityName);

            return "measurements";
        } catch (DateTimeParseException e) {
            logger.error("Date parsing error: ", e);
            redirectAttributes.addFlashAttribute("error", "Invalid date format.");
            return "redirect:/web/measurements";
        } catch (Exception e) {
            logger.error("Error fetching measurements: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching measurements.");
            return "redirect:/web/measurements";
        }
    }

    /**
     * LIST Deleted Measurements with Filters
     */
    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) String unitSearch,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String cityName,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<Measurement> deletedMeasurements;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime start = null;
            LocalDateTime end = null;

            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDateTime.parse(startDate, formatter);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDateTime.parse(endDate, formatter);
            }

            if ((unitSearch == null || unitSearch.isEmpty()) &&
                (startDate == null || startDate.isEmpty()) &&
                (endDate == null || endDate.isEmpty()) &&
                (cityName == null || cityName.isEmpty())) {
                deletedMeasurements = measurementRepository.findAllDeleted();
            } else {
                deletedMeasurements = measurementRepository.filterDeletedMeasurements(unitSearch, start, end, cityName);
            }

            model.addAttribute("deletedMeasurements", deletedMeasurements);
            model.addAttribute("unitSearch", unitSearch);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("cityName", cityName);

            return "measurementsTrash";
        } catch (DateTimeParseException e) {
            logger.error("Date parsing error: ", e);
            redirectAttributes.addFlashAttribute("error", "Invalid date format.");
            return "redirect:/web/measurements/trash";
        } catch (Exception e) {
            logger.error("Error fetching deleted measurements: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted measurements.");
            return "redirect:/web/measurements/trash";
        }
    }

    /**
     * NEW Measurement Form
     */
    @GetMapping("/new")
    public String newMeasurementForm(Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("measurement", new Measurement());
            model.addAttribute("locations", locationRepository.findAllActive());
            model.addAttribute("mode", "new");
            return "measurementForm";
        } catch (Exception e) {
            logger.error("Error displaying new measurement form: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while displaying the form.");
            return "redirect:/web/measurements";
        }
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
            model.addAttribute("locations", locationRepository.findAllActive());
            model.addAttribute("mode", "edit");
            return "measurementForm";
        } catch (Exception e) {
            logger.error("Error displaying edit measurement form: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while displaying the form.");
            return "redirect:/web/measurements";
        }
    }

    /**
     * SAVE Measurement (New or Edit)
     */
    @PostMapping("/save")
    public String saveMeasurement(
            @RequestParam(required = false) Long measurementId,
            @RequestParam String measurementUnit,
            @RequestParam double amount,
            @RequestParam String timestamp,
            @RequestParam(required = false) Long locationId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Measurement measurement;
            if (measurementId != null && measurementRepository.existsById(measurementId)) {
                measurement = measurementRepository.findById(measurementId).get();
            } else {
                measurement = new Measurement();
            }

            measurement.setMeasurementUnit(measurementUnit);
            measurement.setAmount(amount);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            try {
                LocalDateTime parsedTimestamp = LocalDateTime.parse(timestamp, formatter);
                measurement.setTimestamp(parsedTimestamp);
            } catch (DateTimeParseException e) {
                logger.error("Timestamp parsing error: ", e);
                redirectAttributes.addFlashAttribute("error", "Invalid timestamp format.");
                if (measurementId != null) {
                    return "redirect:/web/measurements/edit/" + measurementId;
                } else {
                    return "redirect:/web/measurements/new";
                }
            }

            if (locationId != null) {
                Optional<Location> locOpt = locationRepository.findById(locationId);
                if (locOpt.isPresent()) {
                    measurement.setLocation(locOpt.get());
                } else {
                    measurement.setLocation(null);
                    logger.warn("Location ID {} not found. Setting location to null.", locationId);
                }
            } else {
                measurement.setLocation(null);
            }

            measurementRepository.save(measurement);
            redirectAttributes.addFlashAttribute("success", "Measurement saved successfully!");
            return "redirect:/web/measurements";
        } catch (Exception e) {
            logger.error("Error saving measurement: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while saving the measurement.");
            if (measurementId != null) {
                return "redirect:/web/measurements/edit/" + measurementId;
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
     * PERMANENTLY DELETE Measurement (Admin Only)
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
