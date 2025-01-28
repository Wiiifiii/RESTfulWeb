package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImagesWebController {

    private static final Logger logger = LoggerFactory.getLogger(ImagesWebController.class);
    private final ImageService imageService;

    @GetMapping
    public String listImages(
            @RequestParam(required = false) Long idFilter,
            @RequestParam(required = false) String ownerFilter,
            @RequestParam(required = false) String contentTypeFilter,
            HttpServletRequest request,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());

            // If all filters are empty, show all
            boolean noFilters = (idFilter == null) &&
                                (ownerFilter == null || ownerFilter.isBlank()) &&
                                (contentTypeFilter == null || contentTypeFilter.isBlank());

            List<Image> images;
            if (noFilters) {
                images = imageService.getAllActiveImages();
            } else {
                images = imageService.filterImages(
                        idFilter,
                        ownerFilter.isBlank() ? null : ownerFilter,
                        contentTypeFilter.isBlank() ? null : contentTypeFilter
                );
            }
            model.addAttribute("images", images);
            model.addAttribute("idFilter", idFilter);
            model.addAttribute("ownerFilter", ownerFilter);
            model.addAttribute("contentTypeFilter", contentTypeFilter);

            // Provide a small list of possible contentTypes
            model.addAttribute("possibleContentTypes", List.of("", "image/png", "image/jpeg", "application/pdf", "application/msword"));

            return "images"; // -> images.html
        } catch (Exception e) {
            logger.error("Error fetching images: ", e);
            ra.addFlashAttribute("error", "An error occurred while fetching images.");
            return "redirect:/";
        }
    }

    @GetMapping("/trash")
    public String viewTrash(HttpServletRequest request,
                            Model model,
                            RedirectAttributes ra) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());

            List<Image> deletedImages = imageService.getAllDeletedImages();
            model.addAttribute("images", deletedImages);

            return "imagesTrash";
        } catch (Exception e) {
            logger.error("Error fetching deleted images: ", e);
            ra.addFlashAttribute("error", "An error occurred while fetching deleted images.");
            return "redirect:/web/images";
        }
    }

    @GetMapping("/new")
    public String newImageForm(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        return "imageForm";
    }

    @GetMapping("/edit/{id}")
    public String editImageForm(@PathVariable Long id,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes ra) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());
            Optional<Image> opt = imageService.getImageById(id);
            if (opt.isEmpty()) {
                ra.addFlashAttribute("error", "Image not found.");
                return "redirect:/web/images";
            }
            model.addAttribute("image", opt.get());
            model.addAttribute("mode", "edit");
            return "imageForm";
        } catch (Exception e) {
            logger.error("Error displaying edit image form: ", e);
            ra.addFlashAttribute("error", "An error occurred while showing the edit form.");
            return "redirect:/web/images";
        }
    }

    @PostMapping("/save")
    public String saveImage(
            @ModelAttribute Image image,
            RedirectAttributes ra
    ) {
        try {
            // if editing, ensure image exists
            if (image.getImageId() != null) {
                Optional<Image> existing = imageService.getImageById(image.getImageId());
                if (existing.isEmpty()) {
                    ra.addFlashAttribute("error", "Image not found for update.");
                    return "redirect:/web/images";
                }
            }
            imageService.saveImage(image);
            ra.addFlashAttribute("success", "File saved successfully!");
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error saving image: ", e);
            ra.addFlashAttribute("error", "An error occurred while saving.");
            if (image.getImageId() != null) {
                return "redirect:/web/images/edit/" + image.getImageId();
            } else {
                return "redirect:/web/images/new";
            }
        }
    }

    // Soft delete
    @GetMapping("/delete/{id}")
    public String softDeleteImage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.softDeleteImage(id);
            ra.addFlashAttribute("success", "File deleted successfully!");
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error soft deleting image: ", e);
            ra.addFlashAttribute("error", "An error occurred while deleting.");
            return "redirect:/web/images";
        }
    }

    @PostMapping("/restore/{id}")
    public String restoreImage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.restoreImage(id);
            ra.addFlashAttribute("success", "File restored successfully!");
            return "redirect:/web/images/trash";
        } catch (Exception e) {
            logger.error("Error restoring file: ", e);
            ra.addFlashAttribute("error", "An error occurred while restoring.");
            return "redirect:/web/images/trash";
        }
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDeleteImage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.permanentlyDeleteImage(id);
            ra.addFlashAttribute("success", "File permanently deleted!");
            return "redirect:/";
        } catch (Exception e) {
            logger.error("Error permanent-delete image: ", e);
            ra.addFlashAttribute("error", "An error occurred while permanently deleting the file.");
            return "redirect:/";
        }
    }
}
