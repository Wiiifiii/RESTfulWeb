package com.wefky.RESTfulWeb.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImagesWebController {

    private static final Logger logger = LoggerFactory.getLogger(ImagesWebController.class);
    private final ImageService imageService;

    /**
     * Helper method to build the search query parameter.
     * Returns an empty string if the search parameter is null, empty, or equals "null" (ignoring case).
     */
    private String getSearchQuery(String search) {
        if (search == null || search.trim().isEmpty() || search.trim().equalsIgnoreCase("null")) {
            return "";
        }
        return "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
    }

    /**
     * Handles GET requests to list images. Optionally filters images based on a search query.
     *
     * @param search the search query to filter images (optional)
     * @param request the HttpServletRequest object
     * @param model the Model object to pass attributes to the view
     * @param ra the RedirectAttributes object to pass flash attributes
     * @return the name of the view to render
     */
    @GetMapping
    public String listImages(@RequestParam(required = false) String search,
                             HttpServletRequest request,
                             Model model,
                             RedirectAttributes ra) {
        model.addAttribute("currentUri", request.getRequestURI());
        try {
            List<Image> images = imageService.searchImages(search);
            model.addAttribute("images", images);
            model.addAttribute("search", search);
            List<String> possibleContentTypes = imageService.getDistinctContentTypes();
            possibleContentTypes.add(0, "");
            model.addAttribute("possibleContentTypes", possibleContentTypes);
            return "images";
        } catch (Exception e) {
            logger.error("Error fetching images: ", e);
            ra.addFlashAttribute("error", "An error occurred while fetching images: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Handles GET requests to the "/trash" endpoint to view deleted images.
     *
     * @param search Optional search parameter to filter deleted images.
     * @param request HttpServletRequest object to get the current URI.
     * @param model Model object to pass attributes to the view.
     * @param ra RedirectAttributes object to add flash attributes in case of redirection.
     * @return The name of the view to render, either "imagesTrash" or a redirect to "/web/images" in case of an error.
     */
    @GetMapping("/trash")
    public String viewTrash(@RequestParam(required = false) String search,
                            HttpServletRequest request,
                            Model model,
                            RedirectAttributes ra) {
        model.addAttribute("currentUri", request.getRequestURI());
        try {
            List<Image> images = imageService.searchDeletedImages(search);
            model.addAttribute("images", images);
            model.addAttribute("search", search);
            List<String> possibleContentTypes = imageService.getDistinctContentTypes();
            possibleContentTypes.add(0, "");
            model.addAttribute("possibleContentTypes", possibleContentTypes);
            return "imagesTrash";
        } catch (Exception e) {
            logger.error("Error fetching deleted images: ", e);
            ra.addFlashAttribute("error", "An error occurred while fetching deleted images: " + e.getMessage());
            return "redirect:/web/images";
        }
    }

    /**
     * Handles GET requests to the "/new" endpoint to display a form for creating a new image.
     *
     * @param search an optional search parameter to pre-fill the form (can be null)
     * @param model  the model to hold attributes for the view
     * @return the name of the view template to render ("imageForm")
     */
    @GetMapping("/new")
    public String newImageForm(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        model.addAttribute("search", search);
        return "imageForm";
    }

    /**
     * Handles the GET request to show the edit form for an image.
     *
     * @param id the ID of the image to be edited
     * @param search an optional search query parameter
     * @param model the model to pass attributes to the view
     * @param ra the redirect attributes to pass flash attributes
     * @return the name of the view to be rendered
     */
    @GetMapping("/edit/{id}")
    public String editImageForm(@PathVariable Long id,
                                @RequestParam(required = false) String search,
                                Model model,
                                RedirectAttributes ra) {
        try {
            Optional<Image> opt = imageService.getImageById(id);
            if (opt.isEmpty()) {
                ra.addFlashAttribute("error", "File not found.");
                return "redirect:/web/images" + getSearchQuery(search);
            }
            model.addAttribute("image", opt.get());
            model.addAttribute("mode", "edit");
            model.addAttribute("search", search);
            return "imageForm";
        } catch (Exception e) {
            logger.error("Error showing edit form: ", e);
            ra.addFlashAttribute("error", "Cannot show edit form: " + e.getMessage());
            return "redirect:/web/images";
        }
    }

    /**
     * Handles the saving of an image. This method processes the form submission for saving an image,
     * either creating a new image or updating an existing one.
     *
     * @param image          The image object populated from the form.
     * @param bindingResult  The result of binding the form parameters to the image object.
     * @param file           The uploaded file, if any.
     * @param search         The search query string, if any.
     * @param ra             Redirect attributes for passing messages.
     * @param model          The model to hold attributes for the view.
     * @return               The view name to redirect to.
     */
    @PostMapping("/save")
    public String saveImage(@ModelAttribute("image") @Valid Image image,
                            BindingResult bindingResult,
                            @RequestParam(required = false) MultipartFile file,
                            @RequestParam(required = false) String search,
                            RedirectAttributes ra,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", image.getImageId() != null ? "edit" : "new");
            List<String> possibleContentTypes = imageService.getDistinctContentTypes();
            possibleContentTypes.add(0, "");
            model.addAttribute("possibleContentTypes", possibleContentTypes);
            model.addAttribute("search", search);
            return "imageForm";
        }
        try {
            Image img;
            if (image.getImageId() != null) {
                Optional<Image> opt = imageService.getImageById(image.getImageId());
                if (opt.isEmpty()) {
                    ra.addFlashAttribute("error", "File to update not found.");
                    return "redirect:/web/images" + getSearchQuery(search);
                }
                img = opt.get();
            } else {
                img = new Image();
            }
            img.setOwner(image.getOwner());
            img.setTitle(image.getTitle());
            img.setDescription(image.getDescription());
            // Use the selected content type; update it based on the uploaded file if available.
            img.setContentType(image.getContentType());
            if (file != null && !file.isEmpty()) {
                img.setData(file.getBytes());
                if (file.getContentType() != null && !file.getContentType().isEmpty()) {
                    img.setContentType(file.getContentType());
                }
            }
            imageService.saveImage(img);
            ra.addFlashAttribute("success", "File saved successfully!");
            return "redirect:/web/images" + getSearchQuery(search);
        } catch (IOException ex) {
            logger.error("IO error reading file upload", ex);
            ra.addFlashAttribute("error", "Failed to read the file upload: " + ex.getMessage());
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error saving file", e);
            ra.addFlashAttribute("error", "Error saving file: " + e.getMessage());
            if (image.getImageId() != null) {
                return "redirect:/web/images/edit/" + image.getImageId() + getSearchQuery(search);
            } else {
                return "redirect:/web/images/new" + getSearchQuery(search);
            }
        }
    }

    /**
     * Handles the soft deletion of an image by its ID.
     *
     * @param id the ID of the image to be soft deleted
     * @param search an optional search query parameter
     * @param ra RedirectAttributes to add flash attributes for success or error messages
     * @return a redirect URL to the images page with the search query if provided
     */
    @PostMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id,
                             @RequestParam(required = false) String search,
                             RedirectAttributes ra) {
        try {
            imageService.softDeleteImage(id);
            ra.addFlashAttribute("success", "File deleted!");
        } catch (Exception e) {
            logger.error("Error soft deleting file", e);
            ra.addFlashAttribute("error", "Failed to delete file.");
        }
        return "redirect:/web/images" + getSearchQuery(search);
    }

    /**
     * Handles the restoration of an image by its ID.
     *
     * @param id the ID of the image to be restored
     * @param search an optional search query parameter
     * @param ra RedirectAttributes to add flash attributes for success or error messages
     * @return a redirect URL to the trash page with the search query appended
     */
    @PostMapping("/restore/{id}")
    public String restore(@PathVariable Long id,
                          @RequestParam(required = false) String search,
                          RedirectAttributes ra) {
        try {
            imageService.restoreImage(id);
            ra.addFlashAttribute("success", "File restored!");
        } catch (Exception e) {
            logger.error("Error restoring file", e);
            ra.addFlashAttribute("error", "Failed to restore file.");
        }
        return "redirect:/web/images/trash" + getSearchQuery(search);
    }

    /**
     * Permanently deletes an image with the given ID.
     * 
     * This method is secured and requires the user to have the "ROLE_ADMIN" authority.
     * It handles the deletion of the image and adds a success or error message to the
     * RedirectAttributes based on the outcome.
     * 
     * @param id the ID of the image to be permanently deleted
     * @param search an optional search query parameter to be appended to the redirect URL
     * @param ra RedirectAttributes to add flash attributes for success or error messages
     * @return a redirect URL to the trash page with the search query appended if provided
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDelete(@PathVariable Long id,
                                    @RequestParam(required = false) String search,
                                    RedirectAttributes ra) {
        try {
            imageService.permanentlyDeleteImage(id);
            ra.addFlashAttribute("success", "File permanently deleted!");
        } catch (Exception e) {
            logger.error("Error permanently deleting file", e);
            ra.addFlashAttribute("error", "Failed to permanently delete file.");
        }
        return "redirect:/web/images/trash" + getSearchQuery(search);
    }

  
    /**
     * Handles HTTP GET requests to retrieve an image file, including deleted ones, by its ID.
     *
     * @param id the ID of the image to retrieve
     * @return a ResponseEntity containing the image data as a byte array, with appropriate headers and content type,
     *         or a 404 Not Found response if the image does not exist
     */
    @GetMapping("/{id}/file-all")
    public ResponseEntity<byte[]> getFileAll(@PathVariable Long id) {
        Optional<Image> opt = imageService.getImageByIdIncludingDeleted(id);
        if (opt.isPresent()) {
            Image image = opt.get();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + (image.getTitle() != null ? image.getTitle() : "file") + "\"")
                    .body(image.getData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
