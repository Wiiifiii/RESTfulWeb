package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;
import com.wefky.RESTfulWeb.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/web/measurements")
@RequiredArgsConstructor
public class MeasurementWebController {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementWebController.class);

    private final MeasurementRepository measurementRepository;
    private final LocationRepository locationRepository;

    // LIST + FILTER using Projections
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

            boolean isFilterEmpty = (unitSearch == null || unitSearch.isBlank()) &&
                    (startDate == null || startDate.isBlank()) &&
                    (endDate == null || endDate.isBlank()) &&
                    (cityName == null || cityName.isBlank());

            if (isFilterEmpty) {
                measurements = measurementRepository.findAllActive();
                logger.info("Fetching all active measurements without filters.");
            } else {
                // Parse start/end safely
                LocalDateTime start = null;
                LocalDateTime end = null;
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

                try {
                    if (startDate != null && !startDate.isBlank()) {
                        start = LocalDateTime.parse(startDate, dtf);
                        logger.debug("Parsed startDate: {}", start);
                    }
                } catch (Exception e) {
                    logger.error("Invalid startDate format: {}", startDate, e);
                    redirectAttributes.addFlashAttribute("error", "Invalid Start Date format.");
                    return "redirect:/web/measurements";
                }

                try {
                    if (endDate != null && !endDate.isBlank()) {
                        end = LocalDateTime.parse(endDate, dtf);
                        logger.debug("Parsed endDate: {}", end);
                    }
                } catch (Exception e) {
                    logger.error("Invalid endDate format: {}", endDate, e);
                    redirectAttributes.addFlashAttribute("error", "Invalid End Date format.");
                    return "redirect:/web/measurements";
                }

                measurements = measurementRepository.filterMeasurements(
                        (unitSearch == null || unitSearch.isBlank()) ? null : unitSearch,
                        start,
                        end,
                        (cityName == null || cityName.isBlank()) ? null : cityName
                );
                logger.info("Fetched {} measurements with filters.", measurements.size());
            }

            model.addAttribute("measurements", measurements);

            // Add filter parameters to the model to retain form inputs
            model.addAttribute("unitSearch", unitSearch);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("cityName", cityName);

            return "measurements";
        } catch (Exception e) {
            logger.error("Error fetching measurements.", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching measurements.");
            return "redirect:/web/measurements";
        }
    }

    // NEW MEASUREMENT FORM
    @GetMapping("/new")
    public String newMeasurementForm(Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("measurement", new Measurement());
            model.addAttribute("locations", locationRepository.findAllActive());
            model.addAttribute("mode", "new");
            logger.info("Displaying new measurement form.");
            return "measurementForm";
        } catch (Exception e) {
            logger.error("Error displaying new measurement form.", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while displaying the new measurement form.");
            return "redirect:/web/measurements";
        }
    }

    // EDIT MEASUREMENT FORM
    @GetMapping("/edit/{id}")
    public String editMeasurementForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var opt = measurementRepository.findById(id);
            if (opt.isEmpty()) {
                logger.warn("Measurement with ID {} not found for editing.", id);
                redirectAttributes.addFlashAttribute("error", "Measurement not found");
                return "redirect:/web/measurements";
            }
            var meas = opt.get();
            model.addAttribute("measurement", meas);
            model.addAttribute("locations", locationRepository.findAllActive());
            model.addAttribute("mode", "edit");
            logger.info("Displaying edit form for Measurement ID {}.", id);
            return "measurementForm";
        } catch (Exception e) {
            logger.error("Error displaying edit form for Measurement ID {}.", id, e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while editing the measurement.");
            return "redirect:/web/measurements";
        }
    }

    // SAVE MEASUREMENT (NEW OR EDIT)
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
            Measurement m;
            if (measurementId != null && measurementRepository.existsById(measurementId)) {
                m = measurementRepository.findById(measurementId).get();
                logger.info("Updating existing Measurement ID {}.", measurementId);
            } else {
                m = new Measurement();
                logger.info("Creating new Measurement.");
            }
            m.setMeasurementUnit(measurementUnit);
            m.setAmount(amount);

            // Parse timestamp or set now if fails
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                m.setTimestamp(LocalDateTime.parse(timestamp, dtf));
                logger.debug("Parsed timestamp: {}", m.getTimestamp());
            } catch (Exception e) {
                m.setTimestamp(LocalDateTime.now());
                logger.warn("Invalid timestamp format: {}. Set to current time.", timestamp);
            }

            if (locationId != null) {
                var locOpt = locationRepository.findById(locationId);
                if (locOpt.isPresent()) {
                    m.setLocation(locOpt.get());
                    logger.debug("Set Location ID {} for Measurement.", locationId);
                } else {
                    m.setLocation(null);
                    logger.warn("Location ID {} not found. Set to null for Measurement.", locationId);
                }
            } else {
                m.setLocation(null);
                logger.debug("No Location ID provided. Set to null for Measurement.");
            }

            measurementRepository.save(m);
            logger.info("Measurement saved successfully with ID {}.", m.getMeasurementId());
            redirectAttributes.addFlashAttribute("success", "Measurement saved successfully!");
            return "redirect:/web/measurements";
        } catch (Exception e) {
            logger.error("Error saving Measurement.", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while saving the measurement.");
            if (measurementId != null) {
                return "redirect:/web/measurements/edit/" + measurementId;
            } else {
                return "redirect:/web/measurements/new";
            }
        }
    }

    // SOFT DELETE MEASUREMENT
    @GetMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var opt = measurementRepository.findById(id);
            if (opt.isPresent()) {
                Measurement measurement = opt.get();
                measurement.setDeleted(true);
                measurementRepository.save(measurement);
                logger.info("Measurement with ID {} soft-deleted.", id);
                redirectAttributes.addFlashAttribute("success", "Measurement deleted successfully!");
            } else {
                logger.warn("Attempted to delete non-existing Measurement with ID {}.", id);
                redirectAttributes.addFlashAttribute("error", "Measurement not found");
            }
            return "redirect:/web/measurements";
        } catch (Exception e) {
            logger.error("Error soft-deleting Measurement ID {}.", id, e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the measurement.");
            return "redirect:/web/measurements";
        }
    }

    // VIEW TRASH BIN with Filtering
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

            boolean isFilterEmpty = (unitSearch == null || unitSearch.isBlank()) &&
                    (startDate == null || startDate.isBlank()) &&
                    (endDate == null || endDate.isBlank()) &&
                    (cityName == null || cityName.isBlank());

            if (isFilterEmpty) {
                deletedMeasurements = measurementRepository.findAllDeleted();
                logger.info("Fetching all deleted measurements without filters.");
            } else {
                // Parse start/end safely
                LocalDateTime start = null;
                LocalDateTime end = null;
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

                try {
                    if (startDate != null && !startDate.isBlank()) {
                        start = LocalDateTime.parse(startDate, dtf);
                        logger.debug("Parsed startDate for trash: {}", start);
                    }
                } catch (Exception e) {
                    logger.error("Invalid startDate format for trash: {}", startDate, e);
                    redirectAttributes.addFlashAttribute("error", "Invalid Start Date format.");
                    return "redirect:/web/measurements/trash";
                }

                try {
                    if (endDate != null && !endDate.isBlank()) {
                        end = LocalDateTime.parse(endDate, dtf);
                        logger.debug("Parsed endDate for trash: {}", end);
                    }
                } catch (Exception e) {
                    logger.error("Invalid endDate format for trash: {}", endDate, e);
                    redirectAttributes.addFlashAttribute("error", "Invalid End Date format.");
                    return "redirect:/web/measurements/trash";
                }

                deletedMeasurements = measurementRepository.filterDeletedMeasurements(
                        (unitSearch == null || unitSearch.isBlank()) ? null : unitSearch,
                        start,
                        end,
                        (cityName == null || cityName.isBlank()) ? null : cityName
                );
                logger.info("Fetched {} deleted measurements with filters.", deletedMeasurements.size());
            }

            model.addAttribute("deletedMeasurements", deletedMeasurements);

            // Add filter parameters to the model to retain form inputs
            model.addAttribute("unitSearch", unitSearch);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("cityName", cityName);

            return "measurementsTrash";
        } catch (Exception e) {
            logger.error("Error fetching deleted measurements.", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted measurements.");
            return "redirect:/web/measurements/trash";
        }
    }

    // RESTORE MEASUREMENT
    @GetMapping("/restore/{id}")
    public String restoreMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var opt = measurementRepository.findById(id);
            if (opt.isPresent()) {
                Measurement measurement = opt.get();
                measurement.setDeleted(false);
                measurementRepository.save(measurement);
                logger.info("Measurement with ID {} restored.", id);
                redirectAttributes.addFlashAttribute("success", "Measurement restored successfully!");
            } else {
                logger.warn("Attempted to restore non-existing Measurement with ID {}.", id);
                redirectAttributes.addFlashAttribute("error", "Measurement not found");
            }
            return "redirect:/web/measurements/trash";
        } catch (Exception e) {
            logger.error("Error restoring Measurement ID {}.", id, e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while restoring the measurement.");
            return "redirect:/web/measurements/trash";
        }
    }

    // PERMANENTLY DELETE MEASUREMENT (For Admins)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete-permanent/{id}")
    public String deletePermanentMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (measurementRepository.existsById(id)) {
                measurementRepository.deleteById(id);
                logger.info("Measurement with ID {} permanently deleted by admin.", id);
                redirectAttributes.addFlashAttribute("success", "Measurement permanently deleted!");
            } else {
                logger.warn("Attempted to permanently delete non-existing Measurement with ID {}.", id);
                redirectAttributes.addFlashAttribute("error", "Measurement not found");
            }
            return "redirect:/web/measurements/trash";
        } catch (Exception e) {
            logger.error("Error permanently deleting Measurement ID {}.", id, e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the measurement.");
            return "redirect:/web/measurements/trash";
        }
    }
}
