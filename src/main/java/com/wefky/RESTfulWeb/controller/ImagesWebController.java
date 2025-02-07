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
     */
    private String getSearchQuery(String search) {
        if (search == null || search.trim().isEmpty() || search.trim().equalsIgnoreCase("null")) {
            return "";
        }
        return "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
    }

    /**
     * Handles GET requests to list images.
     */
    @GetMapping
    public String listImages(@RequestParam(required = false) String search,
                             HttpServletRequest request,
                             Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        List<Image> images = imageService.searchImages(search);
        model.addAttribute("images", images);
        model.addAttribute("search", search);
        List<String> possibleContentTypes = imageService.getDistinctContentTypes();
        possibleContentTypes.add(0, "");
        model.addAttribute("possibleContentTypes", possibleContentTypes);
        return "images";
    }

    /**
     * Handles GET requests to view deleted images.
     */
    @GetMapping("/trash")
    public String viewTrash(@RequestParam(required = false) String search,
                            HttpServletRequest request,
                            Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        List<Image> images = imageService.searchDeletedImages(search);
        model.addAttribute("images", images);
        model.addAttribute("search", search);
        List<String> possibleContentTypes = imageService.getDistinctContentTypes();
        possibleContentTypes.add(0, "");
        model.addAttribute("possibleContentTypes", possibleContentTypes);
        return "imagesTrash";
    }

    /**
     * Displays the form for creating a new image.
     */
    @GetMapping("/new")
    public String newImageForm(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        model.addAttribute("search", search);
        return "imageForm";
    }

    /**
     * Displays the edit form for an image.
     */
    @GetMapping("/edit/{id}")
    public String editImageForm(@PathVariable Long id,
                                @RequestParam(required = false) String search,
                                Model model) {
        Optional<Image> opt = imageService.getImageById(id);
        if (opt.isEmpty()) {
            // Propagate error to GlobalExceptionHandler
            throw new RuntimeException("File not found.");
        }
        model.addAttribute("image", opt.get());
        model.addAttribute("mode", "edit");
        model.addAttribute("search", search);
        return "imageForm";
    }

    /**
     * Handles saving an image.
     */
    @PostMapping("/save")
    public String saveImage(@ModelAttribute("image") @Valid Image image,
                            BindingResult bindingResult,
                            @RequestParam(required = false) MultipartFile file,
                            @RequestParam(required = false) String search,
                            RedirectAttributes ra,
                            Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", image.getImageId() != null ? "edit" : "new");
            List<String> possibleContentTypes = imageService.getDistinctContentTypes();
            possibleContentTypes.add(0, "");
            model.addAttribute("possibleContentTypes", possibleContentTypes);
            model.addAttribute("search", search);
            return "imageForm";
        }
        Image img;
        if (image.getImageId() != null) {
            Optional<Image> opt = imageService.getImageById(image.getImageId());
            if (opt.isEmpty()) {
                throw new RuntimeException("File to update not found.");
            }
            img = opt.get();
        } else {
            img = new Image();
        }
        img.setOwner(image.getOwner());
        img.setTitle(image.getTitle());
        img.setDescription(image.getDescription());
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
    }

    /**
     * Soft-deletes an image.
     */
    @PostMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id,
                             @RequestParam(required = false) String search,
                             RedirectAttributes ra) {
        imageService.softDeleteImage(id);
        ra.addFlashAttribute("success", "File deleted!");
        return "redirect:/web/images" + getSearchQuery(search);
    }

    /**
     * Restores a soft-deleted image.
     */
    @PostMapping("/restore/{id}")
    public String restore(@PathVariable Long id,
                          @RequestParam(required = false) String search,
                          RedirectAttributes ra) {
        imageService.restoreImage(id);
        ra.addFlashAttribute("success", "File restored!");
        return "redirect:/web/images/trash" + getSearchQuery(search);
    }

    /**
     * Permanently deletes an image (admin only).
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDelete(@PathVariable Long id,
                                    @RequestParam(required = false) String search,
                                    RedirectAttributes ra) {
        imageService.permanentlyDeleteImage(id);
        ra.addFlashAttribute("success", "File permanently deleted!");
        return "redirect:/web/images/trash" + getSearchQuery(search);
    }

    /**
     * Retrieves an image file (including deleted ones) by its ID.
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
            throw new RuntimeException("Image not found.");
        }
    }
}
