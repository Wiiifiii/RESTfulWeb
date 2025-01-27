package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.projection.ImageMetadata;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImageWebController {

    private final ImageRepository imageRepository;

    // LIST + FILTER using Projections
    @GetMapping
    public String listImages(
            @RequestParam(required = false) String ownerSearch,
            @RequestParam(required = false) Long idSearch,
            @RequestParam(required = false) String contentTypeSearch,
            Model model
    ) {
        List<ImageMetadata> images;
        if ((ownerSearch == null || ownerSearch.isBlank()) &&
            (idSearch == null) &&
            (contentTypeSearch == null || contentTypeSearch.isBlank())) {
            images = imageRepository.findAllActive();
        } else {
            images = imageRepository.filterImages(ownerSearch, idSearch, contentTypeSearch);
        }
        model.addAttribute("images", images);
        model.addAttribute("ownerSearch", ownerSearch);
        model.addAttribute("idSearch", idSearch);
        model.addAttribute("contentTypeSearch", contentTypeSearch);
        return "images"; // -> images.html
    }

    // NEW IMAGE FORM
    @GetMapping("/new")
    public String newImageForm(Model model) {
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        return "imageForm"; // -> imageForm.html
    }

    // EDIT IMAGE FORM
    @GetMapping("/edit/{id}")
    public String editImageForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var opt = imageRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Image not found.");
            return "redirect:/web/images";
        }
        model.addAttribute("image", opt.get());
        model.addAttribute("mode", "edit");
        return "imageForm"; // -> imageForm.html
    }

    // SAVE IMAGE (NEW OR EDIT)
    @PostMapping("/save")
    public String saveImage(
            @RequestParam(required = false) Long imageId,
            @RequestParam String owner,
            @RequestParam(required = false) MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        Image img;
        if (imageId != null) {
            var opt = imageRepository.findById(imageId);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Image not found.");
                return "redirect:/web/images"; // Redirect to images list if image not found
            }
            img = opt.get();
        } else {
            img = new Image();
        }

        img.setOwner(owner);

        if (file != null && !file.isEmpty()) {
            // Validate content type
            if (!isImage(file.getContentType())) {
                if (imageId != null) {
                    redirectAttributes.addFlashAttribute("error", "Invalid file type. Please upload an image.");
                    return "redirect:/web/images/edit/" + imageId; // Redirect back to edit form if file is invalid
                } else {
                    redirectAttributes.addFlashAttribute("error", "Invalid file type. Please upload an image.");
                    return "redirect:/web/images/new"; // Redirect back to new form if file is invalid
                }
            }
            try {
                img.setData(file.getBytes());
                img.setContentType(file.getContentType()); // Update MIME type
            } catch (IOException e) {
                e.printStackTrace();
                if (imageId != null) {
                    redirectAttributes.addFlashAttribute("error", "Failed to upload new image.");
                    return "redirect:/web/images/edit/" + imageId; // Redirect back to edit form if upload fails
                } else {
                    redirectAttributes.addFlashAttribute("error", "Failed to upload new image.");
                    return "redirect:/web/images/new"; // Redirect back to new form if upload fails
                }
            }
        }

        imageRepository.save(img);
        redirectAttributes.addFlashAttribute("success", "Image saved successfully!");
        return "redirect:/web/images"; // Redirect to images list after successful save
    }

    // SOFT DELETE IMAGE
    @GetMapping("/delete/{id}")
    public String softDeleteImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var opt = imageRepository.findById(id);
        if (opt.isPresent()) {
            Image image = opt.get();
            image.setDeleted(true);
            imageRepository.save(image);
            redirectAttributes.addFlashAttribute("success", "Image deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Image not found.");
        }
        return "redirect:/web/images";
    }

    // VIEW TRASH BIN with Filtering
    @GetMapping("/trash")
    public String viewTrash(
            @RequestParam(required = false) String ownerSearch,
            @RequestParam(required = false) Long idSearch,
            @RequestParam(required = false) String contentTypeSearch,
            Model model
    ) {
        List<ImageMetadata> deletedImages;
        if ((ownerSearch == null || ownerSearch.isBlank()) &&
            (idSearch == null) &&
            (contentTypeSearch == null || contentTypeSearch.isBlank())) {
            deletedImages = imageRepository.findAllDeleted();
        } else {
            deletedImages = imageRepository.filterDeletedImages(ownerSearch, idSearch, contentTypeSearch);
        }
        model.addAttribute("deletedImages", deletedImages);
        model.addAttribute("ownerSearch", ownerSearch);
        model.addAttribute("idSearch", idSearch);
        model.addAttribute("contentTypeSearch", contentTypeSearch);
        return "imagesTrash"; // -> imagesTrash.html
    }

    // RESTORE IMAGE
    @GetMapping("/restore/{id}")
    public String restoreImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var opt = imageRepository.findById(id);
        if (opt.isPresent()) {
            Image image = opt.get();
            image.setDeleted(false);
            imageRepository.save(image);
            redirectAttributes.addFlashAttribute("success", "Image restored successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Image not found.");
        }
        return "redirect:/web/images/trash";
    }

    // View raw bytes for thumbnail or full image
    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewImage(@PathVariable Long id) {
        var opt = imageRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Image img = opt.get();
        if (img.isDeleted()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = img.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream"; // Default fallback
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(img.getData());
    }

    // Helper method to validate image MIME types
    private boolean isImage(String contentType) {
        return contentType != null && (contentType.equalsIgnoreCase("image/jpeg") ||
                                       contentType.equalsIgnoreCase("image/png") ||
                                       contentType.equalsIgnoreCase("image/gif") ||
                                       contentType.equalsIgnoreCase("image/bmp") ||
                                       contentType.equalsIgnoreCase("image/webp"));
    }
}
