package com.wefky.RESTfulWeb.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.service.ImageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for handling web requests related to images.
 * Provides endpoints for listing, viewing, creating, editing, deleting, and restoring images.
 * 
 * Endpoints:
 * - GET /web/images: List active images, possibly filtered by a search term.
 * - GET /web/images/trash: View deleted images, possibly filtered by a search term.
 * - GET /web/images/new: Show form for creating a new image.
 * - GET /web/images/edit/{id}: Show form for editing an existing image.
 * - POST /web/images/save: Save (create/update) an image.
 * - POST /web/images/delete/{id}: Soft-delete an image.
 * - POST /web/images/restore/{id}: Restore a soft-deleted image.
 * - POST /web/images/delete-permanent/{id}: Permanently delete an image (ADMIN ONLY).
 * 
 * Dependencies:
 * - ImageService: Service for handling image-related operations.
 * - Logger: Logger for logging errors and information.
 * 
 * Annotations:
 * - @Controller: Indicates that this class serves as a web controller.
 * - @RequestMapping("/web/images"): Maps requests to /web/images to this controller.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments (final fields).
 * - @Secured("ROLE_ADMIN"): Restricts access to methods to users with the ROLE_ADMIN authority.
 * 
 * Methods:
 * - listImages: Lists active images, possibly filtered by a search term.
 * - viewTrash: Lists deleted images, possibly filtered by a search term.
 * - newImageForm: Shows the form for creating a new image.
 * - editImageForm: Shows the form for editing an existing image.
 * - saveImage: Saves (creates/updates) an image.
 * - softDelete: Soft-deletes an image.
 * - restore: Restores a soft-deleted image.
 * - permanentlyDelete: Permanently deletes an image (ADMIN ONLY).
 */
@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImagesWebController {

    private static final Logger logger = LoggerFactory.getLogger(ImagesWebController.class);
    private final ImageService imageService;

    /**
     * List active images, possibly filtered by a single search term.
     */
    @GetMapping("/web/images")
    public String listImages(
            @RequestParam(required = false) String search,
            HttpServletRequest request,
            Model model,
            RedirectAttributes ra
    ) {
        model.addAttribute("currentUri", request.getRequestURI());

        try {
            List<Image> images = imageService.searchImages(search);
            model.addAttribute("images", images);
            model.addAttribute("search", search);

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
                List<Image> images = imageService.searchImages(null);
                model.addAttribute("images", images);
                model.addAttribute("search", null);

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
     * View trash (deleted images), possibly filtered by a single search term.
     */
    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) String search,
            HttpServletRequest request,
            Model model,
            RedirectAttributes ra
    ) {
        model.addAttribute("currentUri", request.getRequestURI());

        try {
            List<Image> images = imageService.searchDeletedImages(search);
            model.addAttribute("images", images);
            model.addAttribute("search", search);

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
                List<Image> images = imageService.searchDeletedImages(null);
                model.addAttribute("images", images);
                model.addAttribute("search", null);

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
    public String newImageForm(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        model.addAttribute("search", search);
        return "imageForm"; // -> imageForm.html
    }

    /**
     * Show form for editing an existing image.
     */
    @GetMapping("/edit/{id}")
    public String editImageForm(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            Optional<Image> opt = imageService.getImageById(id);

            if (opt.isEmpty()) {
                ra.addFlashAttribute("error", "File not found.");
                String redirectUrl = "/web/images";
                if (search != null && !search.isEmpty()) {
                    redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
                }
                return "redirect:" + redirectUrl;
            }
            model.addAttribute("image", opt.get());
            model.addAttribute("mode", "edit");

            // Preserve search
            model.addAttribute("search", search);

            return "imageForm";
        } catch (Exception e) {
            logger.error("Error showing edit form: ", e);
            ra.addFlashAttribute("error", "Cannot show edit form: " + e.getMessage());
            String redirectUrl = "/web/images";
            if (search != null && !search.isEmpty()) {
                redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
            }
            return "redirect:" + redirectUrl;
        }
    }

    /**
     * Save (create/update) an image.
     */
    @PostMapping("/save")
    public String saveImage(
            @ModelAttribute("image") @Valid Image image,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile file,
            // Preserve search
            @RequestParam(required = false) String search,
            RedirectAttributes ra,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", image.getImageId() != null ? "edit" : "new");
            // Fetch distinct content types
            List<String> possibleContentTypes = imageService.getDistinctContentTypes();
            possibleContentTypes.add(0, "");
            model.addAttribute("possibleContentTypes", possibleContentTypes);
            model.addAttribute("search", search);
            return "imageForm";
        }

        // Existing save logic
        try {
            Image img;
            if (image.getImageId() != null) {
                // Editing
                Optional<Image> opt = imageService.getImageById(image.getImageId());
                if (opt.isEmpty()) {
                    ra.addFlashAttribute("error", "File to update not found.");
                    String redirectUrl = "/web/images";
                    if (search != null && !search.isEmpty()) {
                        redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
                    }
                    return "redirect:" + redirectUrl;
                }
                img = opt.get();
            } else {
                // New
                img = new Image();
            }

            img.setOwner(image.getOwner());
            img.setTitle(image.getTitle());
            img.setDescription(image.getDescription());
            img.setContentType(image.getContentType());

            if (file != null && !file.isEmpty()) {
                byte[] bytes = file.getBytes();
                img.setData(bytes);
            }

            imageService.saveImage(img);
            ra.addFlashAttribute("success", "File saved successfully!");

            // Pass search in redirect
            String redirectUrl = "/web/images";
            if (search != null && !search.isEmpty()) {
                redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
            }

            return "redirect:" + redirectUrl;
        } catch (IOException ex) {
            logger.error("IO error reading file upload", ex);
            ra.addFlashAttribute("error", "Failed to read the file upload: " + ex.getMessage());
            String redirectUrl = "/web/images";
            if (search != null && !search.isEmpty()) {
                redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
            }
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            logger.error("Error saving file", e);
            ra.addFlashAttribute("error", "Error saving file: " + e.getMessage());
            if (image.getImageId() != null) {
                String redirectUrl = "/web/images/edit/" + image.getImageId();
                if (search != null && !search.isEmpty()) {
                    redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
                }
                return "redirect:" + redirectUrl;
            } else {
                String redirectUrl = "/web/images/new";
                if (search != null && !search.isEmpty()) {
                    redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
                }
                return "redirect:" + redirectUrl;
            }
        }
    }

    /**
     * Soft-delete an image.
     * Changed from @GetMapping to @PostMapping for better security.
     */
  
    @PostMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id, @RequestParam(required = false) String search, RedirectAttributes ra) {
        try {
            imageService.softDeleteImage(id);
            ra.addFlashAttribute("success", "File deleted!");
        } catch (Exception e) {
            logger.error("Error soft deleting file", e);
            ra.addFlashAttribute("error", "Failed to delete file.");
        }

        String redirectUrl = "/web/images";
        if (search != null && !search.trim().isEmpty() && !"null".equalsIgnoreCase(search.trim())) {
            redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
        }
        return "redirect:" + redirectUrl;
    }

    /**
     * Restore a soft-deleted image.
     * Accessible to users with ROLE_ADMIN.
     */
   
    @PostMapping("/restore/{id}")
    public String restore(@PathVariable Long id, @RequestParam(required = false) String search, RedirectAttributes ra) {
        try {
            imageService.restoreImage(id);
            ra.addFlashAttribute("success", "File restored!");
        } catch (Exception e) {
            logger.error("Error restoring file", e);
            ra.addFlashAttribute("error", "Failed to restore file.");
        }

        String redirectUrl = "/web/images/trash";
        if (search != null && !search.isEmpty()) {
            redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
        }
        return "redirect:" + redirectUrl;
    }

    /**
     * Permanently delete an image. ADMIN ONLY
     */

    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDelete(@PathVariable Long id, @RequestParam(required = false) String search, RedirectAttributes ra) {
        try {
            imageService.permanentlyDeleteImage(id);
            ra.addFlashAttribute("success", "File permanently deleted!");
        } catch (Exception e) {
            logger.error("Error permanently deleting file", e);
            ra.addFlashAttribute("error", "Failed to permanently delete file.");
        }

        String redirectUrl = "/web/images/trash";
        if (search != null && !search.isEmpty()) {
            redirectUrl += "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
        }
        return "redirect:" + redirectUrl;
    }
}
