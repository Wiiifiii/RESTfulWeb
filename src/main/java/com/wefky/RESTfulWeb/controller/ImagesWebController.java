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

@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImagesWebController {

    private static final Logger logger = LoggerFactory.getLogger(ImagesWebController.class);
    private final ImageService imageService;

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

    @GetMapping("/new")
    public String newImageForm(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        model.addAttribute("search", search);
        return "imageForm";
    }

    @GetMapping("/edit/{id}")
    public String editImageForm(@PathVariable Long id,
                                @RequestParam(required = false) String search,
                                Model model,
                                RedirectAttributes ra) {
        try {
            Optional<Image> opt = imageService.getImageById(id);
            if (opt.isEmpty()) {
                ra.addFlashAttribute("error", "File not found.");
                return "redirect:/web/images" + (search != null && !search.isEmpty() ? "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8) : "");
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
                    return "redirect:/web/images" + (search != null && !search.isEmpty() ? "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8) : "");
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
            }
            imageService.saveImage(img);
            ra.addFlashAttribute("success", "File saved successfully!");
            return "redirect:/web/images" + (search != null && !search.isEmpty() ? "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8) : "");
        } catch (IOException ex) {
            logger.error("IO error reading file upload", ex);
            ra.addFlashAttribute("error", "Failed to read the file upload: " + ex.getMessage());
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error saving file", e);
            ra.addFlashAttribute("error", "Error saving file: " + e.getMessage());
            if (image.getImageId() != null) {
                return "redirect:/web/images/edit/" + image.getImageId() + (search != null && !search.isEmpty() ? "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8) : "");
            } else {
                return "redirect:/web/images/new" + (search != null && !search.isEmpty() ? "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8) : "");
            }
        }
    }

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
        return "redirect:/web/images" + (search != null && !search.trim().isEmpty() ? "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8) : "");
    }

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
        return "redirect:/web/images/trash" + (search != null && !search.isEmpty() ? "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8) : "");
    }

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
        return "redirect:/web/images/trash" + (search != null && !search.isEmpty() ? "?search=" + UriUtils.encode(search, StandardCharsets.UTF_8) : "");
    }
}
