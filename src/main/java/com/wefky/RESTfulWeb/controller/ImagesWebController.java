package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * A controller that serves Thymeleaf views for Images.
 * If you prefer to use an ImageService, swap that in place of ImageRepository.
 */
@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImagesWebController {

    private static final Logger logger = LoggerFactory.getLogger(ImagesWebController.class);

    private final ImageRepository imageRepository; // or an ImageService

    @GetMapping
    public String listImages(HttpServletRequest request,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            // Provide currentUri for the navbar active logic
            model.addAttribute("currentUri", request.getRequestURI());

            // Query all active images
            List<Image> images = imageRepository.findAllActive();
            model.addAttribute("images", images);

            return "images"; // -> images.html
        } catch (Exception e) {
            logger.error("Error fetching images: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching images.");
            return "redirect:/";
        }
    }

    @GetMapping("/trash")
    public String viewTrash(HttpServletRequest request,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());

            // Query deleted images
            List<Image> deletedImages = imageRepository.findAllDeleted();
            model.addAttribute("images", deletedImages);

            return "imagesTrash"; // -> imagesTrash.html
        } catch (Exception e) {
            logger.error("Error fetching deleted images: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted images.");
            return "redirect:/web/images";
        }
    }

    @PostMapping("/restore/{id}")
    public String restoreImage(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Image> opt = imageRepository.findById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Image not found.");
                return "redirect:/web/images/trash";
            }
            Image img = opt.get();
            img.setDeleted(false);
            imageRepository.save(img);

            redirectAttributes.addFlashAttribute("success", "Image restored successfully!");
            return "redirect:/web/images/trash";
        } catch (Exception e) {
            logger.error("Error restoring image: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while restoring the image.");
            return "redirect:/web/images/trash";
        }
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDeleteImage(@PathVariable Long id,
                                         RedirectAttributes redirectAttributes) {
        try {
            Optional<Image> opt = imageRepository.findById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Image not found.");
                return "redirect:/web/images/trash";
            }
            imageRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("success", "Image permanently deleted!");
            return "redirect:/";
        } catch (Exception e) {
            logger.error("Error permanently deleting image: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the image.");
            return "redirect:/web/images/trash";
        }
    }
}
