file:///D:/GitHub/RESTfulWeb/src/main/java/com/wefky/RESTfulWeb/controller/ImagesWebController.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 1085
uri: file:///D:/GitHub/RESTfulWeb/src/main/java/com/wefky/RESTfulWeb/controller/ImagesWebController.java
text:
```scala
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
     * Show table with possible filter by ID, owner, contentType
     */
    @GetMapping
    public String listImages(@@
            @RequestParam(required = false) Long idFilter,
            @RequestParam(required = false) String ownerFilter,
            @RequestParam(required = false) String contentTypeFilter,
            HttpServletRequest request,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());

            boolean noFilters = (idFilter == null)
                                && (ownerFilter == null || ownerFilter.isBlank())
                                && (contentTypeFilter == null || contentTypeFilter.isBlank());

            List<Image> images;
            if (noFilters) {
                images = imageService.getAllActiveImages();
            } else {
                images = imageService.filterImages(
                        idFilter,
                        (ownerFilter != null && !ownerFilter.isBlank()) ? ownerFilter : null,
                        (contentTypeFilter != null && !contentTypeFilter.isBlank()) ? contentTypeFilter : null
                );
            }
            model.addAttribute("images", images);
            model.addAttribute("idFilter", idFilter);
            model.addAttribute("ownerFilter", ownerFilter);
            model.addAttribute("contentTypeFilter", contentTypeFilter);

            // Some pre-defined contentTypes
            model.addAttribute("possibleContentTypes", List.of("", "image/png", "image/jpeg", "application/pdf", "application/msword"));

            return "images"; // -> images.html
        } catch (Exception e) {
            logger.error("Error fetching images: ", e);
            ra.addFlashAttribute("error", "An error occurred while fetching images.");
            return "redirect:/";
        }
    }

    @GetMapping("/trash")
    public String viewTrash(HttpServletRequest request, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("images", imageService.getAllDeletedImages());
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
        Image img = new Image();
        model.addAttribute("image", img);
        model.addAttribute("mode", "new");
        return "imageForm";
    }

    @GetMapping("/edit/{id}")
    public String editImageForm(@PathVariable Long id, HttpServletRequest request, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("currentUri", request.getRequestURI());
            Optional<Image> opt = imageService.getImageById(id);
            if (opt.isEmpty()) {
                ra.addFlashAttribute("error", "File not found.");
                return "redirect:/web/images";
            }
            model.addAttribute("image", opt.get());
            model.addAttribute("mode", "edit");
            return "imageForm";
        } catch (Exception e) {
            logger.error("Error showing edit form: ", e);
            ra.addFlashAttribute("error", "Cannot show edit form right now.");
            return "redirect:/web/images";
        }
    }

    /**
     * Example 'save' that reads an uploaded file from the form.
     */
    @PostMapping("/save")
    public String saveImage(@RequestParam(required = false) Long imageId,
                            @RequestParam(required = false) String owner,
                            @RequestParam(required = false) String title,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) String contentType,
                            @RequestParam(required = false) MultipartFile file, // file from <input type="file">
                            RedirectAttributes ra) {
        try {
            Image img;
            if (imageId != null) {
                // edit
                Optional<Image> opt = imageService.getImageById(imageId);
                if (opt.isEmpty()) {
                    ra.addFlashAttribute("error", "File to update not found.");
                    return "redirect:/web/images";
                }
                img = opt.get();
            } else {
                // new
                img = new Image();
            }

            img.setOwner(owner);
            img.setTitle(title);
            img.setDescription(description);
            img.setContentType(contentType);

            if (file != null && !file.isEmpty()) {
                // If user uploaded a new file, set data
                byte[] bytes = file.getBytes();
                img.setData(bytes);
            }

            // If new => uploadDate is set in service if imageId is null. Otherwise it doesn't change
            imageService.saveImage(img);

            ra.addFlashAttribute("success", "File saved successfully!");
            return "redirect:/web/images";
        } catch (IOException ex) {
            logger.error("IO error reading file upload", ex);
            ra.addFlashAttribute("error", "Failed to read the file upload. " + ex.getMessage());
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error saving file", e);
            ra.addFlashAttribute("error", "Error saving file: " + e.getMessage());
            if (imageId != null) {
                return "redirect:/web/images/edit/" + imageId;
            } else {
                return "redirect:/web/images/new";
            }
        }
    }

    @GetMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.softDeleteImage(id);
            ra.addFlashAttribute("success", "File deleted!");
            return "redirect:/web/images";
        } catch (Exception e) {
            logger.error("Error soft deleting file", e);
            ra.addFlashAttribute("error", "Failed to delete file.");
            return "redirect:/web/images";
        }
    }

    @PostMapping("/restore/{id}")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.restoreImage(id);
            ra.addFlashAttribute("success", "File restored!");
            return "redirect:/web/images/trash";
        } catch (Exception e) {
            logger.error("Error restoring file", e);
            ra.addFlashAttribute("error", "Failed to restore file.");
            return "redirect:/web/images/trash";
        }
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/delete-permanent/{id}")
    public String permanentlyDelete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.permanentlyDeleteImage(id);
            ra.addFlashAttribute("success", "File permanently deleted!");
            return "redirect:/";
        } catch (Exception e) {
            logger.error("Error permanently deleting file", e);
            ra.addFlashAttribute("error", "Failed to permanently delete file.");
            return "redirect:/";
        }
    }
}

```



#### Error stacktrace:

```
scala.collection.Iterator$$anon$19.next(Iterator.scala:973)
	scala.collection.Iterator$$anon$19.next(Iterator.scala:971)
	scala.collection.mutable.MutationTracker$CheckedIterator.next(MutationTracker.scala:76)
	scala.collection.IterableOps.head(Iterable.scala:222)
	scala.collection.IterableOps.head$(Iterable.scala:222)
	scala.collection.AbstractIterable.head(Iterable.scala:935)
	dotty.tools.dotc.interactive.InteractiveDriver.run(InteractiveDriver.scala:164)
	dotty.tools.pc.MetalsDriver.run(MetalsDriver.scala:45)
	dotty.tools.pc.HoverProvider$.hover(HoverProvider.scala:40)
	dotty.tools.pc.ScalaPresentationCompiler.hover$$anonfun$1(ScalaPresentationCompiler.scala:376)
```
#### Short summary: 

java.util.NoSuchElementException: next on empty iterator