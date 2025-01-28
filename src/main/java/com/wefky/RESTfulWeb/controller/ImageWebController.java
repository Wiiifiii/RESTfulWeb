// File: src/main/java/com/wefky/RESTfulWeb/controller/ImageWebController.java

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
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImageWebController {

    private static final Logger logger = LoggerFactory.getLogger(ImageWebController.class);

    private final ImageService imageService;

    /**
     * LIST + FILTER Active Images
     */
    @GetMapping
    public String listImages(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String contentTypeSearch,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            String currentURI = request.getRequestURI();
            List<Image> images;
            if (ownerId == null && (contentTypeSearch == null || contentTypeSearch.isBlank())) {
                images = imageService.getAllActiveImages();
            } else {
                images = imageService.filterImages(ownerId, contentTypeSearch, "active");
            }
            model.addAttribute("images", images);
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("contentTypeSearch", contentTypeSearch);
            model.addAttribute("contentTypes", getContentTypes());
            model.addAttribute("currentURI", currentURI);
            return "images"; // -> images.html
        } catch (Exception e) {
            logger.error("Error fetching images: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching images.");
            return "redirect:/web/images";
        }
    }

    /**
     * NEW Image Form
     */
    @GetMapping("/new")
    public String newImageForm(Model model, HttpServletRequest request) {
        String currentURI = request.getRequestURI();
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        model.addAttribute("contentTypes", getContentTypes());
        model.addAttribute("currentURI", currentURI);
        return "imageForm";
    }

    /**
     * EDIT Image Form
     */
    @GetMapping("/edit/{id}")
    public String editImageForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            Optional<Image> opt = imageService.getImageById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Image not found.");
                return "redirect:/web/images";
            }
            Image image = opt.get();
            String currentURI = request.getRequestURI();
            model.addAttribute("image", image);
            model.addAttribute("mode", "edit");
            model.addAttribute("contentTypes", getContentTypes());
            model.addAttribute("currentURI", currentURI);
            return "imageForm";
        } catch (Exception e) {
            logger.error("Error displaying edit image form: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while displaying the form.");
            return "redirect:/web/images";
        }
    }

    /**
     * SAVE Image (NEW OR EDIT)
     */
    @PostMapping("/save")
    public String saveImage(
            @ModelAttribute Image image,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            String currentURI = request.getRequestURI();
            // If editing, ensure the image exists
            if (image.getImageId() != null) {
                Optional<Image> opt = imageService.getImageById(image.getImageId());
                if (opt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Image not found.");
                    return "redirect:/web/images";
                }
            }

            // Handle file upload
            if (file != null && !file.isEmpty()) {
                image.setData(file.getBytes());
                image.setContentType(file.getContentType());
            } else {
                if (image.getImageId() == null) { // New Image: File upload is required
                    redirectAttributes.addFlashAttribute("error", "Please upload an image file.");
                    return "redirect:/web/images/new";
                }
                // Edit Mode: Retain existing image data if no new file is uploaded
                Optional<Image> existingImageOpt = imageService.getImageById(image.getImageId());
                if (existingImageOpt.isPresent()) {
                    Image existingImage = existingImageOpt.get();
                    image.setData(existingImage.getData());
                    image.setContentType(existingImage.getContentType());
                }
            }

            imageService.saveImage(image);
            redirectAttributes.addFlashAttribute("success", "Image saved successfully!");
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error saving image: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while saving the image.");
            if (image.getImageId() != null) {
                return "redirect:/web/images/edit/" + image.getImageId();
            } else {
                return "redirect:/web/images/new";
            }
        }
    }

    /**
     * SOFT DELETE Image
     */
    @GetMapping("/delete/{id}")
    public String softDeleteImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            imageService.softDeleteImage(id);
            redirectAttributes.addFlashAttribute("success", "Image deleted successfully!");
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error soft deleting image: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the image.");
            return "redirect:/web/images";
        }
    }

    /**
     * VIEW TRASH BIN for Images
     */
    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String contentTypeSearch,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            String currentURI = request.getRequestURI();
            List<Image> deletedImages;
            if (ownerId == null && (contentTypeSearch == null || contentTypeSearch.isBlank())) {
                deletedImages = imageService.getAllDeletedImages();
            } else {
                // For trash, filter by owner and content type
                deletedImages = imageService.filterImages(ownerId, contentTypeSearch, "deleted").stream()
                        .filter(Image::isDeleted)
                        .toList();
            }
            model.addAttribute("images", deletedImages);
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("contentTypeSearch", contentTypeSearch);
            model.addAttribute("contentTypes", getContentTypes());
            model.addAttribute("currentURI", currentURI);
            return "imagesTrash";
        } catch (Exception e) {
            logger.error("Error fetching deleted images: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while fetching deleted images.");
            return "redirect:/web/images";
        }
    }

    /**
     * RESTORE Image
     */
    @PostMapping("/restore/{id}")
    public String restoreImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            imageService.restoreImage(id);
            redirectAttributes.addFlashAttribute("success", "Image restored successfully!");
            return "redirect:/web/images/trash";
        } catch (Exception e) {
            logger.error("Error restoring image: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while restoring the image.");
            return "redirect:/web/images/trash";
        }
    }

    /**
     * PERMANENTLY DELETE Image. ADMIN ONLY.
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDeleteImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            imageService.permanentlyDeleteImage(id);
            redirectAttributes.addFlashAttribute("success", "Image permanently deleted!");
            return "redirect:/"; // Redirect to home page
        } catch (Exception e) {
            logger.error("Error permanently deleting image: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the image.");
            return "redirect:/"; // Redirect to home page
        }
    }

    /**
     * Helper method to provide content type options.
     */
    private List<String> getContentTypes() {
        return List.of("Image", "Document", "PDF", "Video", "Audio"); // Add more types as needed
    }
}
