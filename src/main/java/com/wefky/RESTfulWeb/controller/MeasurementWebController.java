package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Measurement;
import com.wefky.RESTfulWeb.repository.MeasurementRepository;
import com.wefky.RESTfulWeb.repository.LocationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/web/measurements")
public class MeasurementWebController {

    private final MeasurementRepository measurementRepository;
    private final LocationRepository locationRepository;

    public MeasurementWebController(MeasurementRepository measurementRepository,
                                    LocationRepository locationRepository) {
        this.measurementRepository = measurementRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public String listMeasurements(
            @RequestParam(required = false) String unitSearch,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String cityName,
            Model model
    ) {
        // If all filter fields empty => findAllActive
        if (
            (unitSearch == null || unitSearch.isBlank()) &&
            (startDate == null || startDate.isBlank()) &&
            (endDate == null || endDate.isBlank()) &&
            (cityName == null || cityName.isBlank())
        ) {
            model.addAttribute("measurements", measurementRepository.findAllActive());
        } else {
            // Parse start/end safely
            LocalDateTime start = null;
            LocalDateTime end = null;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            try {
                if (startDate != null && !startDate.isBlank()) {
                    start = LocalDateTime.parse(startDate, dtf);
                }
            } catch (Exception e) {
                // If parse fails, keep start = null
            }
            try {
                if (endDate != null && !endDate.isBlank()) {
                    end = LocalDateTime.parse(endDate, dtf);
                }
            } catch (Exception e) {
                // Keep end = null
            }

            // Do filter
            List<Measurement> filtered = measurementRepository.filterMeasurements(
                unitSearch == null || unitSearch.isBlank() ? null : unitSearch,
                start,
                end,
                cityName == null || cityName.isBlank() ? null : cityName
            );
            model.addAttribute("measurements", filtered);
        }
        return "measurements";
    }

    // NEW MEASUREMENT FORM
    @GetMapping("/new")
    public String newMeasurementForm(Model model) {
        model.addAttribute("measurement", new Measurement());
        model.addAttribute("locations", locationRepository.findAllActive());
        model.addAttribute("mode", "new");
        return "measurementForm";
    }

    // EDIT MEASUREMENT FORM
    @GetMapping("/edit/{id}")
    public String editMeasurementForm(@PathVariable Long id, Model model) {
        var opt = measurementRepository.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/web/measurements?error=Measurement not found";
        }
        var meas = opt.get();
        model.addAttribute("measurement", meas);
        model.addAttribute("locations", locationRepository.findAllActive());
        model.addAttribute("mode", "edit");
        return "measurementForm";
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
        Measurement m;
        if (measurementId != null && measurementRepository.existsById(measurementId)) {
            m = measurementRepository.findById(measurementId).get();
        } else {
            m = new Measurement();
        }
        m.setMeasurementUnit(measurementUnit);
        m.setAmount(amount);

        // Parse timestamp or set now if fails
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            m.setTimestamp(LocalDateTime.parse(timestamp, dtf));
        } catch (Exception e) {
            m.setTimestamp(LocalDateTime.now());
        }

        if (locationId != null) {
            var locOpt = locationRepository.findById(locationId);
            locOpt.ifPresent(m::setLocation);
        } else {
            m.setLocation(null);
        }
        measurementRepository.save(m);
        redirectAttributes.addFlashAttribute("success", "Measurement saved successfully!");
        return "redirect:/web/measurements";
    }

    // SOFT DELETE MEASUREMENT
    @GetMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var opt = measurementRepository.findById(id);
        if (opt.isPresent()) {
            Measurement measurement = opt.get();
            measurement.setDeleted(true);
            measurementRepository.save(measurement);
            redirectAttributes.addFlashAttribute("success", "Measurement deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Measurement not found");
        }
        return "redirect:/web/measurements";
    }

    // VIEW TRASH BIN
    @GetMapping("/trash")
    public String viewTrash(Model model) {
        model.addAttribute("deletedMeasurements", measurementRepository.findAllDeleted());
        return "measurementsTrash";
    }

    // RESTORE MEASUREMENT
    @GetMapping("/restore/{id}")
    public String restoreMeasurement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var opt = measurementRepository.findById(id);
        if (opt.isPresent()) {
            Measurement measurement = opt.get();
            measurement.setDeleted(false);
            measurementRepository.save(measurement);
            redirectAttributes.addFlashAttribute("success", "Measurement restored successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Measurement not found");
        }
        return "redirect:/web/measurements/trash";
    }
}