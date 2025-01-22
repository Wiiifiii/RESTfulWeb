package com.wefky.RESTfulWeb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;

@RestController
@RequestMapping("/images")
public class ImageRestController {

    private final ImageRepository imageRepository;

    public ImageRestController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    // 1) GET all
    @GetMapping
    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    // 2) GET by id (for example, just returning the entire entity with the byte[] - in practice you might stream or separate the metadata)
    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        return imageRepository.findById(id)
                .map(image -> ResponseEntity.ok(image))
                .orElse(ResponseEntity.notFound().build());
    }

    // 3) POST (create new - upload)
    @PostMapping
    public ResponseEntity<Image> uploadImage(@RequestBody Image image) {
        // In a real scenario, you'd likely handle MultiPartFile & validations
        Image saved = imageRepository.save(image);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // 4) PUT (update - for example updating the owner or even replacing the data)
    @PutMapping("/{id}")
    public ResponseEntity<Image> updateImage(@PathVariable Long id, @RequestBody Image imageDetails) {
        return imageRepository.findById(id)
                .map(img -> {
                    img.setOwner(imageDetails.getOwner());
                    img.setData(imageDetails.getData());
                    Image updated = imageRepository.save(img);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5) DELETE
    @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
    var imageOpt = imageRepository.findById(id);
    if (imageOpt.isPresent()) {
        imageRepository.delete(imageOpt.get());
        return ResponseEntity.noContent().build();
    } else {
        return ResponseEntity.notFound().build();
    }
    }

    
}
