file:///D:/GitHub/RESTfulWeb/src/main/java/com/wefky/RESTfulWeb/service/ImageService.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
uri: file:///D:/GitHub/RESTfulWeb/src/main/java/com/wefky/RESTfulWeb/service/ImageService.java
text:
```scala
package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public List<Image> getAllActiveImages() {
        List<Image> images = imageRepository.findAllActive();
        populateBase64(images);
        return images;
    }

    @Transactional(readOnly = true)
    public List<Image> filterImages(Long id, String owner, String contentType) {
        List<Image> images = imageRepository.filterImages(id, owner, contentType);
        populateBase64(images);
        return images;
    }

    @Transactional(readOnly = true)
    public Optional<Image> getImageById(Long id) {
        Optional<Image> opt = imageRepository.findById(id);
        opt.ifPresent(this::populateBase64);
        return opt;
    }

    public Image saveImage(Image image) {
        // If new, set uploadDate
        if (image.getImageId() == null) {
            image.setUploadDate(LocalDateTime.now());
        }
        // Save to DB
        Image saved = imageRepository.save(image);
        // Populate base64 on the returned object
        populateBase64(saved);
        return saved;
    }

    public void softDeleteImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(true);
            imageRepository.save(image);
            logger.info("Image with ID {} soft deleted.", id);
        });
    }

    public void permanentlyDeleteImage(Long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            logger.info("Image with ID {} permanently deleted.", id);
        } else {
            logger.warn("Attempted to permanently delete non-existent Image with ID {}.", id);
        }
    }

    @Transactional(readOnly = true)
    public List<Image> getAllDeletedImages() {
        List<Image> images = imageRepository.findAllDeleted();
        populateBase64(images);
        return images;
    }

    @Transactional(readOnly = true)
    public List<Image> filterDeletedImages(Long id, String owner, String contentType) {
        List<Image> images = imageRepository.filterDeletedImages(id, owner, contentType);
        populateBase64(images);
        return images;
    }

    public void restoreImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            image.setDeleted(false);
            imageRepository.save(image);
            logger.info("Image with ID {} restored.", id);
        });
    }

    /**
     * Helper to set base64Data on a single Image.
     */
    private void populateBase64(Image img) {
        if (img.getData() != null) {
            String encoded = Base64.getEncoder().encodeToString(img.getData());
            img.setBase64Data(encoded);
        }
    }

    /**
     * Overload for a list of images.
     */
    private void populateBase64(List<Image> images) {
        for (Image img : images) {
            populateBase64(img);
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
	dotty.tools.pc.WithCompilationUnit.<init>(WithCompilationUnit.scala:31)
	dotty.tools.pc.SimpleCollector.<init>(PcCollector.scala:345)
	dotty.tools.pc.PcSemanticTokensProvider$Collector$.<init>(PcSemanticTokensProvider.scala:63)
	dotty.tools.pc.PcSemanticTokensProvider.Collector$lzyINIT1(PcSemanticTokensProvider.scala:63)
	dotty.tools.pc.PcSemanticTokensProvider.Collector(PcSemanticTokensProvider.scala:63)
	dotty.tools.pc.PcSemanticTokensProvider.provide(PcSemanticTokensProvider.scala:88)
	dotty.tools.pc.ScalaPresentationCompiler.semanticTokens$$anonfun$1(ScalaPresentationCompiler.scala:109)
```
#### Short summary: 

java.util.NoSuchElementException: next on empty iterator