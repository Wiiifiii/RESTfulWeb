package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
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

@Controller
@RequestMapping("/web/images")
@RequiredArgsConstructor
public class ImageWebController {

    private static final Logger logger = LoggerFactory.getLogger(ImageWebController.class);

    private final ImageRepository imageRepository;

    /**
     * LIST + FILTER Active Images
     */
    @GetMapping
    public String listImages(
            @RequestParam(required = false) String ownerSearch,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<Image> images;
            if (ownerSearch == null || ownerSearch.isBlank()) {
                images = imageRepository.findAllActive();
            } else {
                images = imageRepository.filterImages(ownerSearch);
            }
            model.addAttribute("images", images);
            model.addAttribute("ownerSearch", ownerSearch);
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
    public String newImageForm(Model model) {
        model.addAttribute("image", new Image());
        model.addAttribute("mode", "new");
        return "imageForm";
    }

    /**
     * EDIT Image Form
     */
    @GetMapping("/edit/{id}")
    public String editImageForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Image> opt = imageRepository.findById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Image not found.");
                return "redirect:/web/images";
            }
            Image image = opt.get();
            model.addAttribute("image", image);
            model.addAttribute("mode", "edit");
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
            RedirectAttributes redirectAttributes
    ) {
        try {
            // If editing, ensure the image exists
            if (image.getImageId() != null) {
                Optional<Image> opt = imageRepository.findById(image.getImageId());
                if (opt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Image not found.");
                    return "redirect:/web/images";
                }
            }
            imageRepository.save(image);
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
            Optional<Image> opt = imageRepository.findById(id);
            if (opt.isPresent()) {
                Image image = opt.get();
                image.setDeleted(true);
                imageRepository.save(image);
                redirectAttributes.addFlashAttribute("success", "Image deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Image not found.");
            }
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
    public String viewTrash(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Image> deletedImages = imageRepository.findAllDeleted();
            model.addAttribute("deletedImages", deletedImages);
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
    @GetMapping("/restore/{id}")
    public String restoreImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Image> opt = imageRepository.findById(id);
            if (opt.isPresent()) {
                Image image = opt.get();
                image.setDeleted(false);
                imageRepository.save(image);
                redirectAttributes.addFlashAttribute("success", "Image restored successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Image not found.");
            }
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
    @GetMapping("/delete-permanent/{id}")
    public String permanentlyDeleteImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (imageRepository.existsById(id)) {
                imageRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Image permanently deleted!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Image not found.");
            }
            return "redirect:/web/images/trash";
        } catch (Exception e) {
            logger.error("Error permanently deleting image: ", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while permanently deleting the image.");
            return "redirect:/web/images/trash";
        }
    }
}
