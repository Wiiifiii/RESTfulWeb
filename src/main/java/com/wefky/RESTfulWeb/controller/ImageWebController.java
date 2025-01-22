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
    public String listImages(@RequestParam(required=false) String ownerSearch, Model model) {
        List<ImageMetadata> images;
        if (ownerSearch == null || ownerSearch.isBlank()) {
            images = imageRepository.findAllActive();
        } else {
            images = imageRepository.filterImages(ownerSearch);
        }
        model.addAttribute("images", images);
        return "images";
    }

    // UPLOAD
    @PostMapping("/upload")
public String uploadImage(@RequestParam("owner") String owner,
                          @RequestParam("file") MultipartFile file,
                          RedirectAttributes redirectAttributes) {
    if (file.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "No file selected for upload.");
        return "redirect:/web/images";
    }

    // Validate content type
    if (!isImage(file.getContentType())) {
        redirectAttributes.addFlashAttribute("error", "Invalid file type. Please upload an image.");
        return "redirect:/web/images";
    }

    try {
        Image img = Image.builder()
                .owner(owner)
                .data(file.getBytes()) // Store raw bytes
                .contentType(file.getContentType()) // Store MIME type
                .deleted(false)
                .build();
        imageRepository.save(img);
        redirectAttributes.addFlashAttribute("success", "Image uploaded successfully!");
    } catch (IOException e) {
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Failed to upload image.");
    }
    return "redirect:/web/images";
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


    // Soft delete
    @GetMapping("/delete/{id}")
    public String deleteImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var opt = imageRepository.findById(id);
        opt.ifPresent(i -> {
            i.setDeleted(true);
            imageRepository.save(i);
            redirectAttributes.addFlashAttribute("success", "Image deleted successfully!");
        });
        return "redirect:/web/images";
    }

    // EDIT
    @GetMapping("/edit/{id}")
    public String editImage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var opt = imageRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Image not found.");
            return "redirect:/web/images";
        }
        model.addAttribute("image", opt.get());
        return "imageForm";
    }

    @PostMapping("/save")
    public String saveImage(
            @RequestParam Long imageId,
            @RequestParam String owner,
            @RequestParam(required=false) MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        var opt = imageRepository.findById(imageId);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Image not found.");
            return "redirect:/web/images";
        }
        Image img = opt.get();
        img.setOwner(owner);
        if (file != null && !file.isEmpty()) {
            // Validate content type
            if (!isImage(file.getContentType())) {
                redirectAttributes.addFlashAttribute("error", "Invalid file type. Please upload an image.");
                return "redirect:/web/images/edit/" + imageId;
            }
            try {
                img.setData(file.getBytes());
                img.setContentType(file.getContentType()); // Update MIME type
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Failed to upload new image.");
                return "redirect:/web/images/edit/" + imageId;
            }
        }
        imageRepository.save(img);
        redirectAttributes.addFlashAttribute("success", "Image updated successfully!");
        return "redirect:/web/images";
    }

    // Trash bin using Projections
    @GetMapping("/trash")
    public String viewTrash(Model model) {
        List<ImageMetadata> deletedImages = imageRepository.findAllDeleted();
        model.addAttribute("deletedImages", deletedImages);
        return "imagesTrash";
    }

    @GetMapping("/restore/{id}")
    public String restoreImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var opt = imageRepository.findById(id);
        opt.ifPresent(img -> {
            img.setDeleted(false);
            imageRepository.save(img);
            redirectAttributes.addFlashAttribute("success", "Image restored successfully!");
        });
        return "redirect:/web/images/trash";
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
