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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImagesWebController {

    private static final Logger logger = LoggerFactory.getLogger(ImagesWebController.class);
    private final ImageService imageService;

   /**
     * List active images, possibly filtered.
     */
    @GetMapping
    public String listImages(
            @RequestParam(required = false) Long idFilter,
            @RequestParam(required = false) String ownerFilter,
            @RequestParam(required = false) String contentTypeFilter,
            HttpServletRequest request,
            Model model,
            RedirectAttributes ra
    ) {
        model.addAttribute("currentUri", request.getRequestURI());

        try {
            // Convert empty strings to null
            ownerFilter = (ownerFilter != null && !ownerFilter.isBlank()) ? ownerFilter.trim() : null;
            contentTypeFilter = (contentTypeFilter != null && !contentTypeFilter.isBlank()) ? contentTypeFilter.trim() : null;

            List<Image> images = imageService.filterImages(idFilter, ownerFilter, contentTypeFilter);
            model.addAttribute("images", images);
            model.addAttribute("idFilter", idFilter);
            model.addAttribute("ownerFilter", ownerFilter);
            model.addAttribute("contentTypeFilter", contentTypeFilter);

            // Fetch distinct content types dynamically
            List<String> possibleContentTypes = imageService.getDistinctContentTypes();
            // Optionally, add an empty option for "All"
            possibleContentTypes.add(0, "");
            model.addAttribute("possibleContentTypes", possibleContentTypes);

            return "images"; // -> images.html
        } catch (Exception e) {
            logger.error("Error fetching images: ", e);
            ra.addFlashAttribute("error", "An error occurred while fetching images: " + e.getMessage());

            // Attempt to fetch all active images without filters to display alongside the error
            try {
                List<Image> images = imageService.getAllActiveImages();
                model.addAttribute("images", images);
                model.addAttribute("idFilter", null);
                model.addAttribute("ownerFilter", null);
                model.addAttribute("contentTypeFilter", null);

                List<String> possibleContentTypes = imageService.getDistinctContentTypes();
                possibleContentTypes.add(0, "");
                model.addAttribute("possibleContentTypes", possibleContentTypes);
            } catch (Exception innerEx) {
                logger.error("Error fetching all active images after initial error: ", innerEx);
                // If fetching all images also fails, redirect to home
                return "redirect:/";
            }

            return "images";
        }
    }

    /**
     * View trash (deleted images), possibly filtered.
     */
    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) Long idFilter,
            @RequestParam(required = false) String ownerFilter,
            @RequestParam(required = false) String contentTypeFilter,
            HttpServletRequest request,
            Model model,
            RedirectAttributes ra
    ) {
        model.addAttribute("currentUri", request.getRequestURI());

        try {
            boolean noFilters = (idFilter == null)
                    && (ownerFilter == null || ownerFilter.isBlank())
                    && (contentTypeFilter == null || contentTypeFilter.isBlank());

            List<Image> images;
            if (noFilters) {
                images = imageService.getAllDeletedImages();
            } else {
                images = imageService.filterDeletedImages(
                        idFilter,
                        (ownerFilter != null && !ownerFilter.isBlank()) ? ownerFilter.trim() : null,
                        (contentTypeFilter != null && !contentTypeFilter.isBlank()) ? contentTypeFilter.trim() : null
                );
            }

            model.addAttribute("images", images);
            model.addAttribute("idFilter", idFilter);
            model.addAttribute("ownerFilter", ownerFilter);
            model.addAttribute("contentTypeFilter", contentTypeFilter);

            // Fetch distinct content types dynamically
            List<String> possibleContentTypes = imageService.getDistinctContentTypes();
            // Optionally, add an empty option for "All"
            possibleContentTypes.add(0, "");
            model.addAttribute("possibleContentTypes", possibleContentTypes);

            return "imagesTrash"; // -> imagesTrash.html
        } catch (Exception e) {
            logger.error("Error fetching deleted images: ", e);
            ra.addFlashAttribute("error", "An error occurred while fetching deleted images: " + e.getMessage());

            // Attempt to fetch all deleted images without filters to display alongside the error
            try {
                List<Image> images = imageService.getAllDeletedImages();
                model.addAttribute("images", images);
                model.addAttribute("idFilter", idFilter);
                model.addAttribute("ownerFilter", ownerFilter);
                model.addAttribute("contentTypeFilter", contentTypeFilter);

                List<String> possibleContentTypes = imageService.getDistinctContentTypes();
                possibleContentTypes.add(0, "");
                model.addAttribute("possibleContentTypes", possibleContentTypes);
            } catch (Exception innerEx) {
                logger.error("Error fetching all deleted images after initial error: ", innerEx);
                // If fetching all images also fails, redirect to active images
                return "redirect:/web/images";
            }

            return "imagesTrash";
        }
    }

    /**
     * Show form for creating a new image.
     */
    @GetMapping("/new")
    public String newImageForm(Model model) {
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        return "imageForm"; // -> imageForm.html
    }

    /**
     * Show form for editing an existing image.
     */
    @GetMapping("/edit/{id}")
    public String editImageForm(
            @PathVariable Long id,
            @RequestParam(required = false) Long idFilter,
            @RequestParam(required = false) String ownerFilter,
            @RequestParam(required = false) String contentTypeFilter,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            Optional<Image> opt = imageService.getImageById(id);
            if (opt.isEmpty()) {
                ra.addFlashAttribute("error", "File not found.");
                return "redirect:/web/images";
            }
            model.addAttribute("image", opt.get());
            model.addAttribute("mode", "edit");

            // Preserve filters
            model.addAttribute("idFilter", idFilter);
            model.addAttribute("ownerFilter", ownerFilter);
            model.addAttribute("contentTypeFilter", contentTypeFilter);

            return "imageForm";
        } catch (Exception e) {
            logger.error("Error showing edit form: ", e);
            ra.addFlashAttribute("error", "Cannot show edit form: " + e.getMessage());
            return "redirect:/web/images";
        }
    }

    /**
     * Save (create/update) an image.
     */
    @PostMapping("/save")
    public String saveImage(
            @RequestParam(required = false) Long imageId,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) MultipartFile file,
            // Preserve filter
            @RequestParam(required = false) Long idFilter,
            @RequestParam(required = false) String ownerFilter,
            @RequestParam(required = false) String contentTypeFilter,
            RedirectAttributes ra
    ) {
        try {
            Image img;
            if (imageId != null) {
                // Editing
                Optional<Image> opt = imageService.getImageById(imageId);
                if (opt.isEmpty()) {
                    ra.addFlashAttribute("error", "File to update not found.");
                    return "redirect:/web/images";
                }
                img = opt.get();
            } else {
                // New
                img = new Image();
            }

            img.setOwner(owner);
            img.setTitle(title);
            img.setDescription(description);
            img.setContentType(contentType);

            if (file != null && !file.isEmpty()) {
                byte[] bytes = file.getBytes();
                img.setData(bytes);
            }

            imageService.saveImage(img);
            ra.addFlashAttribute("success", "File saved successfully!");

            // Pass filters in redirect
            ra.addAttribute("idFilter", idFilter);
            ra.addAttribute("ownerFilter", ownerFilter);
            ra.addAttribute("contentTypeFilter", contentTypeFilter);

            return "redirect:/web/images";
        } catch (IOException ex) {
            logger.error("IO error reading file upload", ex);
            ra.addFlashAttribute("error", "Failed to read the file upload: " + ex.getMessage());
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error saving file", e);
            ra.addFlashAttribute("error", "Error saving file: " + e.getMessage());
            if (imageId != null) {
                return "redirect:/web/images/edit/" + imageId
                        + "?idFilter=" + idFilter
                        + "&ownerFilter=" + ownerFilter
                        + "&contentTypeFilter=" + contentTypeFilter;
            } else {
                return "redirect:/web/images/new"
                        + "?idFilter=" + idFilter
                        + "&ownerFilter=" + ownerFilter
                        + "&contentTypeFilter=" + contentTypeFilter;
            }
        }
    }

    /**
     * Soft-delete (GET).
     */
    @GetMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.softDeleteImage(id);
            ra.addFlashAttribute("success", "File deleted!");
        } catch (Exception e) {
            logger.error("Error soft deleting file", e);
            ra.addFlashAttribute("error", "Failed to delete file.");
        }
        return "redirect:/web/images";
    }

    /**
     * Restore from trash (POST).
     */
    @PostMapping("/restore/{id}")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.restoreImage(id);
            ra.addFlashAttribute("success", "File restored!");
        } catch (Exception e) {
            logger.error("Error restoring file", e);
            ra.addFlashAttribute("error", "Failed to restore file.");
        }
        return "redirect:/web/images/trash";
    }

    /**
     * Permanently delete (POST). ADMIN ONLY
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDelete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.permanentlyDeleteImage(id);
            ra.addFlashAttribute("success", "File permanently deleted!");
        } catch (Exception e) {
            logger.error("Error permanently deleting file", e);
            ra.addFlashAttribute("error", "Failed to permanently delete file.");
        }
        return "redirect:/web/images/trash";
    }
}
